(ns timothypratley.robots.client
  (:use timothypratley.messenger timothypratley.extensions
     timothypratley.logging))

(def local-world {:robots (ref nil)
                  :tasks (ref nil)})

(defmulti state-client-protocol :id)
(defmethod state-client-protocol :default
  [message connection]
  (log :info "CLIENT Bad message received: " message " from "
       (:socket connection))
  nil)
(defmethod state-client-protocol :login-result
  [message connection]
  (if (:successful message)
    (do (set-connection-name connection (:name message))
      (log :info "CLIENT Successfully logged in as " (:name message)))
    (log :info "CLIENT Login failed"))
  nil)
(defmethod state-client-protocol :status
  [message connection]
  (log :info "CLIENT " message)
  (dosync (alter (:robots local-world)
                 assoc (:name message) (:data message)))
  (log :info "CLIENT local-world=" local-world)
  nil)
(defmethod state-client-protocol :add-task
  [message connection]
  (dosync (alter (:tasks local-world)
                 assoc (:id message) (:task message)))
  (log :info "CLIENT local-world=" local-world)
  nil)

(defn connect-to-state-server
  [host port]
  (log-format)
  (log-level :finest)
  (log-capture
    (connect host port state-client-protocol "CLIENT")))

