(ns timothypratley.messenger
  (:use timothypratley.extensions timothypratley.logging))

(declare new-connection add-connection
         read-messages write-messages process-messages)


; Public interface

(defn connect
  "Connect to a message server.
  protocol is a user supplied function which will be passed a message.
  protocol should take a message, which is a map containing a key :message_id
  to dispatch processing upon.
  protocol can return :bye to end the connection"
  [host port protocol]
  (log :info "Connecting to " host ":" port)
  (let [s (java.net.Socket. host port)]
    (new-connection s protocol)))

(defn send-message
  "Send a message.
  A message is a map containing an :id and :data"
  [connection message]
  (.put (:out connection) message))

(defn listen
  "Sets up a message server. The server listens on port for clients.
  When a client connects it interprets messages which are put onto a
  processing queue using protocol (see connect)."
  [port protocol]
  (let [listener (java.net.ServerSocket. port)]
    (log :info "Listening on " listener)
    (future (try
              (while-let [s (.accept listener)]
                (log :info "Accepted connection from " s)
                (new-connection s protocol))
              (catch java.io.IOException io
                (log :info "Listener " listener " closed." ))))
    listener))

(def connections (ref #{}))


; An alternate encoding implementation

(defn read-text-message
  "Custom text deserializer.
  Uses one line per message. First word is id. Rest is data."
  [input]
  (let [words (.split (.readLine input) " ")]
    {:id (first words) :data (rest words)}))

(defn write-text-message
  "Custom test serializer.
  Uses one line per message. First word is id. Rest is data."
  [output message]
  (.println output (apply str (:id message) " " (:data message))))


; Private implementation

(defn- new-connection
  "Setup a new connection.
  Optionally provide the serialization/deserialization functions,
  and the associated streams.
  Default is to use clojure reader/printer."
  ([socket protocol]
   (new-connection socket protocol
                   read (java.io.PushbackReader.
                          (java.io.InputStreamReader.
                            (.getInputStream socket)))
                   #(.print %1 %2) (java.io.PrintStream.
                           (.getOutputStream socket))))
  ([socket protocol read-message read-stream write-message write-stream]
   (let [in (java.util.concurrent.LinkedBlockingQueue.)
        out (java.util.concurrent.LinkedBlockingQueue.)
        connection {:in in :out out :socket socket}]
     (add-connection connection)
     (future (read-messages connection read-stream in read-message))
     (future (write-messages connection write-stream out write-message))
     (future (process-messages connection protocol))
     connection)))

(defn- add-connection
  "Add connection to the set of active connections."
  [connection]
  (dosync (commute connections conj connection)))

(defn- remove-connection
  "Remove connection from the set of active connections."
  [connection]
  (if-let [s (:socket connection)]
    (try
      (.close s)
      (catch java.io.IOException io)))
  (.put (:in connection) :bye)
  (.put (:out connection) :bye)
  (dosync (commute connections disj connection)))

(defn- process-messages
  "Deal with messages that are ready for processing."
  [connection protocol]
  (log :finest "Processor started for " (:socket connection))
  (while-let-pred [message (.take (:in connection))] (partial not= :bye)
    (if-let [result (protocol message)]
      (.put (:out connection) result)))
  (log :finest "Processor closed for " (:socket connection))
  (remove-connection connection))

(defn- read-messages
  "Put incomming messages onto a queue to be processed.
  read-message is a function which reads a message from an input stream."
  [connection input queue read-message]
  (log :finest "Reader started for " (:socket connection))
  (try
    (while-let-pred [message (read-message input)] (partial not= :bye)
      (log :fine "Read " (:socket connection) ":" message)
      (.put queue message))
    (catch java.net.SocketException se))
  (log :finest "Reader closed for " (:socket connection))
  (remove-connection connection))

(defn- write-messages
  "Write outgoing messages from a queue.
  write-message is a function which writes a message to an output stream."
  [connection output queue write-message]
  (log :finest "Writer started for " (:socket connection))
  (try
    (while-let-pred [msg (.take queue)] (partial not= :bye)
      (log :fine "Write " (:socket connection) ":" msg)
      (write-message output msg))
    (write-message output :bye)
    (catch java.net.SocketException se))
  (log :finest "Writer closed for " (:socket connection))
  (remove-connection connection))

