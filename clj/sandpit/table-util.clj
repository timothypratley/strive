(defn dimensionalize
  [aggregate dims]
  (let [axis (for [dim (range dims)]
               (reduce conj #{} (map #(nth % dim) (keys aggregate))))]
    (for [a axis] a))
  ;recur?
 )

(for [r rows]
  (for [c cols]))

