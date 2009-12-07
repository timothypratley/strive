(ns test-extensions
  (:use timothypratley.extensions clojure.test))

(deftest defn-helpers
  (let [m {:one 1, :two 2}]
    (defn-with-args foo1
      "hi1"
      {"in1" "must be a string", "in2" (str "must be one of: " (keys m))}
      (println in1 (m in2))
      (m in2))
    (defn-by-contract foo2
      "hi2"
      {"in1" "string?", "in2" "(m in2)"}
      (println in1 (m in2))
      (m in2)))
  (is (instance? clojure.lang.IFn foo1))
  (is (instance? clojure.lang.IFn foo2))
  (doc foo1)
  (doc foo2)
  (println :argdescs)
  (println (:argdescs ^(var foo1)))
  (println (:argdescs ^(var foo2)))
  (is (= 1 (foo1 "hi" :one)))
  (is (= 1 (foo2 "hi" :one))))

(deftest fun
         (defnc foo3
                "hi3"
                {:arglist [in1 string?
                           in2 !(str "one of " (keys m)) m]
                 :argdesc ["in1desc" (str "one of " (keys m))]
                 :argpred [string? m]}



(run-tests)
