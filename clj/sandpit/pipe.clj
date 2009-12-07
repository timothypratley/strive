(defn pipe
  [x & fs]
  (loop [ret ((first fs) x), fs (next fs)]
    (if fs
      (recur ((first fs) ret) (next fs))
      ret)))

;user=> (pipe 2 inc #(/ % 2))
;3/2