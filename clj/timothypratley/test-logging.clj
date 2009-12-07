(ns test-logging
  (:use clojure.test timothypratley.logging))


(deftest output-captured
  (let [out-orig *out*
        err-orig *err*]
    (log-format)
    (log-level :finest)
    (log-capture
      (is (not= *out* out-orig))
      (is (not= *err* err-orig))
      (println "Hello from println")
      (.println (System/out) "Hello from stdout")
      (.println *err* "Hello from *err*")
      (.println (System/err) "Hello from stderr"))
    (log :finest "Hello from :finest")
    (debug (+ 1 2))
    @(future (debug *out*))
    (logged-future (/ 1 0))
    ; wait without forcing the future
    (Thread/sleep 1000)
    (is (= *out* out-orig))
    (is (= *err* err-orig))))

(run-tests)
(shutdown-agents)

