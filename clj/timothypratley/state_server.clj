(ns timothypratley.state-server
  (:use timothypratley.messenger timothypratley.extensions
     timothypratley.logging))

(def world {:robots (ref {})
            :tasks (ref {})})

; subscriptions is a map,
; where keys are connections
; and values are sets of data queries
(def subscriptions (ref {}))

(defn inform-subscribers
  [ks v]
  (doseq [[connection queries] @subscriptions]
    (if (queries ks)
      (send-message connection {:id :status
                                :label ks
                                :data v}))))

(defn watched-assoc
  [m ks v]
  (inform-subscribers ks v)
  (assoc m ks v))

(defn assert-logged-in
  [connection]
  (assert (:name @(:state connection))))
(defn assert-is-controller
  [connection]
  (assert (= "controller" (:name @(:state connection)))))

(defmulti state-server-protocol :id)
(defmethod state-server-protocol :default
  [message connection]
  (log :info "SERVER Bad message received: " message " from "
       (get-connection-name connection))
  :bye)
(defmethod state-server-protocol :login
  [message connection]
  (dosync (alter (:state connection)
                 assoc :name (:name message)))
  (log :info "SERVER login accepted: " (:name message))
  {:id :login-result
   :successful true
   :name (:name message)})
(defmethod state-server-protocol :status
  [message connection]
  (assert-logged-in connection)
  (dosync (alter (:robots world)
                 watched-assoc
                 (:name (:state connection))
                 (:data message)))
  (log :info "SERVER status received: " (:data message))
  nil)
(defmethod state-server-protocol :subscribe
  [message connection]
  (assert-logged-in connection)
  (dosync (alter subscriptions
                 assoc-conj connection (:query message) #{}))
  (log :info "SERVER subscription added: " (:query message))
  nil)
(defmethod state-server-protocol :add-task
  [message connection]
  (assert-is-controller connection)
  (dosync (alter (:tasks @world)
                 watched-assoc
                 (:id message)
                 (:task message)))
  (log :info "SERVER new task: " (:task message))
  nil)

(defn run-state-server
  [port]
  (log-format)
  (log-level :fine)
  (listen port state-server-protocol))

