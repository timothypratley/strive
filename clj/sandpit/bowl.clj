(defn map-frames
  [f coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (cons (f (take (balls-toscore s) s))
        (map f (drop (frame-advance s) s)))))
