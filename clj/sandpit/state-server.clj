(ns state-server
  (:use message-server))

(def world (ref {}))

(defn inform-subscribers
  [ks m]
  (println ks m))

(defn watched-assoc-in
  [m ks v]
  (inform-subscribers ks v)
  (assoc-in m ks v))

(defn process-status
  [label data]
  (dosync (alter world watched-assoc-in [label] data)))

(defn state-protocol
  [message]
  (condp #(= % (:message_id message))
    :status (process-status (:system_id message) (:data message))))

(listen 8888 state-protocol)

(send-status "S01" {:x 3700.0, :y 3750.0, :v 4.3, :i 0.5})
(send-status "S02" {:x 3800.0, :y 3780.0, :v 2.3, :i 0.1})
(send-status "PT1" {:lt 4200.0, :ct 12.0, :h 5.0})

