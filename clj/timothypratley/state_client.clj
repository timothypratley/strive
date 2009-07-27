(ns timothypratley.state-client
  (:use timothypratley.messenger timothypratley.extensions
     timothypratley.logging))

(def local-world (ref {}))

(defmulti state-client-protocol :message-id)
(defmethod state-client-protocol :default
  [message connection]
  (log :info "CLIENT Bad message received: " message " from "
       (:socket connection))
  nil)
(defmethod state-client-protocol :status
  [message connection]
  (log :info "CLIENT " message)
  (dosync (alter local-world assoc (:label message) (:data message)))
  (log :info "CLIENT local-world=" @local-world)
  nil)

(defn connect-to-state-server
  [host port]
  (log-format)
  (log-level :finest)
  (log-capture
    (connect host port state-client-protocol)))

