(set! *warn-on-reflection* true)
(ns timothypratley.futures)

(let [pool (java.util.concurrent.Executors/newFixedThreadPool
             (.availableProcessors (java.lang.Runtime/getRuntime)))
      solo (java.util.concurrent.Executors/newCachedThreadPool)]
  (defn send-future [#^Callable func]
    (.submit pool func))
    ;(.submit clojure.lang.Agent/pooledExecutor func))
  (defn send-off-future [#^Callable func]
    (.submit solo func))
    ;(.submit clojure.lang.Agent/soloExecutor func))
  (defn fwait [#^java.util.concurrent.Future fut]
    (.get fut))
  (defn fcancel [#^java.util.concurrent.Future fut]
    (.cancel fut true))
  (defn shutdown-futures []
    (.shutdown pool)
    (.shutdown solo)))

(println "solo" (fwait (send-off-future #(+ 1 2))))
(println "pool" (fwait (send-future #(+ 1 2))))
(defn long-calc []
  (dotimes [i 1000]
    (reduce * (range 2 1000))))

(println "Start long-calc")
(time (fcancel (send-future long-calc)))
(println "Cancelled")
(println "Start long-calc")
(time (fwait (send-future long-calc)))
(println "Done")


(shutdown-futures)


