(ns timothypratley.robots.server
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
      (send-message connection {:message-id :status
                                :label ks
                                :data v}))))

(defn watched-assoc
  [m ks v]
  (inform-subscribers ks v)
  (assoc m ks v))

(defmacro assert-logged-in []
  `(assert (:name (:state connection))))
(defmacro assert-is-controller []
  `(assert (= "controller" (:name (:state connection)))))

(defmulti state-server-protocol :message-id)
(defmethod state-server-protocol :default
  [message connection]
  (log :info "SERVER Bad message received: " message " from "
       (:socket connection))
  :bye)
(defmethod state-server-protocol :login
  [message connection]
  (dosync (alter (:state connection)
                 assoc :name (:name message))))
(defmethod state-server-protocol :status
  [message connection]
  (assert-logged-in)
  (dosync (alter (:robots world)
                 watched-assoc
                 (:name (:state connection))
                 (:data message)))
  (log :info "SERVER world=" @world)
  nil)
(defmethod state-server-protocol :subscribe
  [message connection]
  (assert-logged-in)
  (dosync (alter subscriptions
                 assoc-conj connection (:query message) #{}))
  nil)
(defmethod state-server-protocol :add-task
  [message connection]
  (assert-is-controller)
  (dosync (alter (:tasks @world)
                 watched-assoc
                 (:id message)
                 (:task message))))

(defn run-state-server
  [port]
  (log-format)
  (log-level :fine)
  (listen port state-server-protocol))

