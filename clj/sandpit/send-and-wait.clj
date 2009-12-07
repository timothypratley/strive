

(defn send-off-and-wait
  [a f & args]
  (let [blocker (java.util.concurrent.LinkedBlockingQueue.)
        done-fn #(.add blocker (apply f % args))]
    (send-off a done-fn)
    (.take blocker)))

(def a (agent 100))
(defn f [x] (reduce * (range 2 x)))
(send-off-and-wait a f)
