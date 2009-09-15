
(defn re-fn
  "Construct a regular expression from string.
  Calling a regular expression with no arguments returns a Pattern.
  Calling a regular expression with a string argument
  returns nil if no matches, otherwise the equivalent of (re-seq re string)."
  [string]
  (let [pp (re-pattern string)]
    (fn re
      ([] pp)
      ([s] (let [groups (re-seq pp s)]
             (if (first groups)
               groups
               nil))))))

