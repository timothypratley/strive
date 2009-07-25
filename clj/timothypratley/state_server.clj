(ns timothypratley.state-server
  (:use timothypratley.messenger timothypratley.extensions
     timothypratley.logging))

(def world (ref {}))

; subscriptions is a map,
; where keys are connections
; and values are sets of data queries
(def subscriptions (ref {}))

(defn inform-subscribers
  [ks m]
  (log :info ks m))

(defn watched-assoc-in
  [m ks v]
  (inform-subscribers ks v)
  (assoc-in m ks v))

(defn assoc-conj
  "Add to a collection in a map."
  [m k v]
  (assoc m k (conj (m k) v)))

(defmulti state-server-protocol :message-id)
(defmethod state-server-protocol :default
  [message connection]
  (log :info connection "Bad message received: " message)
  nil)
(defmethod state-server-protocol :status
  [message connection]
  (dosync (alter world watched-assoc-in (:label message) (:data message)))
  nil)
(defmethod state-server-protocol :subscribe
  [message connection]
  (dosync (alter subscriptions assoc-conj connection (:query message)))
  nil)

(defn run-state-server
  [port]
  (log-format)
  (log-level :finest)
  (listen port state-server-protocol))


