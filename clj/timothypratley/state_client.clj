(ns timothypratley.state-client
  (:require [timothypratley.messenger :as msg])
  (:use timothypratley.extensions timothypratley.logging))

(def local-world {:robots (ref nil)
                  :tasks (ref nil)})

(defmulti protocol :id)
(defmethod protocol :default
  [message connection]
  (log :info "CLIENT Bad message received: " message " from "
       (:socket connection))
  nil)
(defmethod protocol :login-result
  [message connection]
  (if (:successful message)
    (do (msg/set-connection-name connection (:name message))
      (log :info "CLIENT Successfully logged in as " (:name message)))
    (log :info "CLIENT Login failed"))
  nil)
(defmethod protocol :status
  [message connection]
  (log :info "CLIENT " message)
  (dosync (alter (:robots local-world)
                 assoc (:name message) (:data message)))
  (log :info "CLIENT local-world=" local-world)
  nil)
(defmethod protocol :add-task
  [message connection]
  (dosync (alter (:tasks local-world)
                 assoc (:id message) (:task message)))
  (log :info "CLIENT local-world=" local-world)
  nil)

(defn connect
  [host port]
  (log-format)
  (log-level :finest)
  (log-capture
    (msg/connect host port state-client-protocol "CLIENT")))

