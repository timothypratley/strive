(def a "12234567890qwertyuiopasdfghjklzxcvbnm")
(defn sign [x] (cond (pos? x) 1, (neg? x) -1, :else 0))
(def cmp (cons 0 (map (comp sign compare) a (rest a))))
(def depths (loop [depth 0, remaining cmp, built []]
              (if (first remaining)
                (recur (+ depth (first remaining)) (rest remaining) (conj built depth))
                built)))
(let [top (apply min depths), bottom (apply max depths)]
  (doseq [line (range top (inc bottom)), col (range 0 (count a))]
    (if (= line (depths col))
      (print (get a col))
      (print \space))
    (if (= col (dec (count a)))
      (print \newline))))
    
  


