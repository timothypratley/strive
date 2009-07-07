(ns test-messenger
  (:use clojure.test timothypratley.messenger timothypratley.logging))

(log-format)
(log-level :finest)

(deftest sends-and-receives-messages
  (let [message {:id 404 :data "hello world"}
        result (ref nil)
        server-protocol (fn [m]
                          (log :info "Server received: " m)
                          ; NB: this goes outside the test scope
                          ; (is (= m message))
                          ; so instead save the result in a ref
                          (dosync (ref-set result m))
                          :bye)
        client-protocol (fn [m]
                          (log :info "Client recieved: " m)
                          :bye)
        listener (listen 8888 server-protocol)
        conn (connect "localhost" 8888 client-protocol)]
    (send-message conn message)
    (.close listener)
    ; allow the threads some time to work
    (Thread/sleep 1000)
    (is (= @result message))))

(run-tests)
(shutdown-agents)

