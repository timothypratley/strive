(ns timothypratley.test-state-server
  (:use clojure.test
     timothypratley.state-server timothypratley.state-client
     timothypratley.messenger timothypratley.logging))

(deftest can-send-status
         (let [host "localhost"
               port 8888
               server (run-state-server port)
               m {:message-id :status
                  :label "hello"
                  :data "world"}
               clients [(connect-to-state-server host port)
                        (connect-to-state-server host port)
                        (connect-to-state-server host port)]]
           (send-message (first clients)
                         {:message-id :subscribe
                          :query "hello"})
           (doseq [c clients]
             (send-message c m))
           (Thread/sleep 1000)
           ; TODO: why do we need to explicitly close them?
           (doseq [c clients]
             (send-message c :bye))
           ; and sleep - this is broken
           (Thread/sleep 1000)
           (.close server)))


(comment
  (send-message "S01" {:x 3700.0, :y 3600.0, :v 4.3, :i 0.5})
  (send-message "S02" {:x 3800.0, :y 3780.0, :v 2.3, :i 0.1})
  (send-message "S03" {:x 3600.0, :y 3900.0, :v 3.3, :i 1.1})
  (send-message "S04" {:x 3900.0, :y 3800.0, :v 1.3, :i -0.3})
  (send-message "PT1" {:lt 4200.0, :ct 12.0, :h 5.0})
  (send-message "PT2" {:lt 4000.0, :ct 10.0, :h 15.0}))

(run-tests)
(shutdown-agents)

