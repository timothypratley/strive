(ns message-server)

(declare new-client add-client read-messages write-messages)

(defn listen
  "Sets up a message server. The server listens on port for clients.
  When a client connects it interprets messages which are put onto a
  processing queue.
  protocol is a user supplied function which will be passed a message.
  protocol should take a message, which is a map containing a key :message_id
  to dispatch processing upon.
  protocol can return :bye to end the connection"
  [port protocol]
  (let [listener (new java.net.ServerSocket port)]
    (println "Listening on " listener)
    (future (while true (new-client (.accept listener) protocol)))))

(defn send-message
  "Send a message to a client.
  A message is a map containing a :message-id and :words"
  [client message]
  (.put (:out client)
        (apply str (:message-id message) (:words message))))

(def clients (ref #{}))

(defn- new-client
  "Setup and add a new client connection"
  [socket protocol]
  (println "New connection from " socket)
  (let [in (new java.util.concurrent.LinkedBlockingQueue)
       out (new java.util.concurrent.LinkedBlockingQueue)
       client {:in in :out out}]
    (future (read-text-messages socket in))
    (future (write-text-messages socket out))
    (future (process-messages client protocol))
    (add-client client)))

(defn- add-client
  [client]
  (dosync (commute clients conj client)))

(defn- remove-client
  [client]
  (dosync (commute clients disj client)))

(defn- process-messages
  "Deal with messages that are ready for processing"
  [client protocol]
  (loop []
    (let [r (protocol (.take (:in client)))]
      (if r (send-message client r))
      (if (not= r :bye) (recur))))
  (println "Closing client " client)
  (remove-client client))

(defn- read-text-messages
  "Put incomming messages onto a queue to be processed"
  [socket queue]
  (println "Reader started for " socket)
  (let [input (new java.io.DataInputStream (.getInputStream socket))]
    (while-let [msg (.readLine input)]
      (println "Read " socket msg)
      (let [words (.split msg " ")]
        (.put queue {:message_id (first words) :words (rest words)}))))
  (.put queue nil)
  (println "Reader closed for " socket))

(defn- write-text-messages
  "Write outgoing messages from a queue"
  [socket queue]
  (println "Writer started for " socket)
  (let [output (new java.io.PrintStream (.getOutputStream socket))]
    (while-let [msg (.take queue)]
      (.println output (apply str (:message_id msg) (:words msg)))))
  (println "Writer closed for " socket))


