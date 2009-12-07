(ns test-test
  (:use clojure.test))

@(future (/ 1 0))
(deftest testathon
  (let [f1 (future (is (= 1 2)))]
    @f1))

(run-tests)
;(shutdown-agents)

