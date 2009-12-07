(ns net.projecteuler.problem31)

(def paths (ref #{}))

; apply fun to all elements of coll for which pred-fun returns true
(defn apply-if [pred-fun fun coll]
  (map fun (filter pred-fun coll)))

(defn make-combination-counter [coin-values]
  (fn recurse
    ([sum] (recurse sum 0 '()))
    ([max-sum current-sum coin-path]
      (if (= max-sum current-sum)
          ; if we've recursed to the bottom, add current path to paths
          (dosync (ref-set paths (conj @paths (sort coin-path))))
          ; else go on recursing
          (apply-if (fn check-max [x] (<= (+ current-sum x) max-sum))
              (fn go-recursing [x] (recurse max-sum (+ x current-sum) (cons x coin-path)))
              coin-values)))))

(def count-currency-combinations (make-combination-counter '(1 2 5 10 20 50 100 200)))
(println (count-currency-combinations 200))

