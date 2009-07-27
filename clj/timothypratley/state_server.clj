(ns timothypratley.state-server
  (:use timothypratley.messenger timothypratley.extensions
     timothypratley.logging))

(def world (ref {}))

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

(defmulti state-server-protocol :message-id)
(defmethod state-server-protocol :default
  [message connection]
  (log :info "SERVER Bad message received: " message " from "
       (:socket connection))
  :bye)
(defmethod state-server-protocol :status
  [message connection]
  (dosync (alter world watched-assoc (:label message) (:data message)))
  (log :info "SERVER world=" @world)
  nil)
(defmethod state-server-protocol :subscribe
  [message connection]
  (dosync (alter subscriptions assoc-conj connection (:query message) #{}))
  nil)

(defn run-state-server
  [port]
  (log-format)
  (log-level :fine)
  (listen port state-server-protocol))

