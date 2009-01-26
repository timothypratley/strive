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
  (defn shutdown-futures []
    (.shutdown pool)
    (.shutdown solo)))

(println "solo" (fwait (send-off-future #(+ 1 2))))
(println "pool" (fwait (send-future #(+ 1 2))))
(shutdown-futures)


