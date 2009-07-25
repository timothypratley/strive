(ns timothypratley.state-client
  (:use timothypratley.messenger timothypratley.extensions
     timothypratley.logging))

(defmulti state-client-protocol :message-id)
(defmethod state-client-protocol :default
  [message connection]
  (log :info connection "Bad message received: " message)
  nil)
(defmethod state-client-protocol :status
  [message connection]
  (log :info connection message)
  nil)

(defn connect-to-state-server
  [host port]
  (log-format)
  (log-level :finest)
  (log-capture
    (connect host port state-client-protocol)))

