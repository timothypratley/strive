(ns message-client)

(defn connect
  "Connect to a message server."
  [host port protocol]
  (let [s (new java.net.Socket host port)]
    (println "Connecting to " s)
    (.connect s)
    (future (while true (protocol (.read s))))))

(defn send-message
  "Send a message to a client.
  A message is a map containing a :message-id and :words"
  [client message]
  (.put (:out client)
        (apply str (:message-id message) (:words message))))

(defn- new-connection
  "Setup a new connection"
  [socket protocol]
  (println "Connecting to " socket)
  (let [in (new java.util.concurrent.LinkedBlockingQueue)
       out (new java.util.concurrent.LinkedBlockingQueue)
       connection {:in in :out out}]
    (future (read-text-messages socket in))
    (future (write-text-messages socket out))
    (future (process-messages connection protocol))
    (add-connection connection)))

(defn- add-connection
  [connection]
  (dosync (commute connections conj connection)))

(defn- remove-connection
  [connection]
  (dosync (commute connections disj connection)))

(defn- process-messages
  "Deal with messages that are ready for processing"
  [connection protocol]
  (loop []
    (let [r (protocol (.take (:in connection)))]
      (if r (send-message connection r))
      (if (not= r :bye) (recur))))
  (println "Closing connection " connection)
  (remove-connection connection))

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


