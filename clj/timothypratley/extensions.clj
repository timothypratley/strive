(ns timothypratley.extensions)

(defn repeat-n
  "Creates a lazy sequence of n values."
  [value n]
  (take n (repeat value)))

(defmacro ifnn
  "If not nil."
  [value then else]
  `(if (not (nil? ~value)) ~then ~else))

(defmacro aget2
  "For faster lookup.
  array is a 1d array being treated as a 2d array.
  array should be already type-hinted,
  and width already coorced to int."
  [array x y width]
  `(aget ~array (+ (int ~x) (* (int ~y) ~width))))

(defmacro aset2
  "For faster array setting.
  array is a 1d array being treated as a 2d array.
  array should be already type-hinted,
  and width already coorced to int."
  [array x y width value]
  `(aset ~array (+ (int ~x) (* (int ~y) ~width)) ~value))

(defn assoc-conj
  "Add to a collection in a map."
  [m k v default]
  (assoc m k (conj (m k default) v)))

(defn sign
  "Returns 1 if x is positive, -1 if x is negative, else 0"
  [x]
  (cond
    (pos? x) 1
    (neg? x) -1
    :else 0))

(defn <?
  "Returns true if arguments are in increasing order by compare,
  otherwise false."
  ([x] true)
  ([x y] (neg? (compare x y)))
  ([x y & more]
   (if (<? x y)
     (if (next more)
       (recur y (first more) (next more))
       (<? y (first more)))
     false)))

(defn <=?
  "Returns true if arguments are in increasing order by compare,
  otherwise false."
  ([x] true)
  ([x y] (neg? (compare x y)))
  ([x y & more]
   (if (<=? x y)
     (if (next more)
       (recur y (first more) (next more))
       (<=? y (first more)))
     false)))

(defn >?
  "Returns true if arguments are in decreasing order by compare,
  otherwise false."
  ([x] true)
  ([x y] (pos? (compare x y)))
  ([x y & more]
   (if (>? x y)
     (if (next more)
       (recur y (first more) (next more))
       (>? y (first more)))
     false)))

(defn >=?
  "Returns true if arguments are in decreasing order by compare,
  otherwise false."
  ([x] true)
  ([x y] (pos? (compare x y)))
  ([x y & more]
   (if (>=? x y)
     (if (next more)
       (recur y (first more) (next more))
       (>=? y (first more)))
     false)))

(defmacro while-let
  "while with a binding"
  [[v cnd] & body]
  `(loop[]
     (let [~v ~cnd]
       (when ~v
         ~@body
         (recur)))))

(defmacro while-let-pred
  "while with a binding and predicate"
  [[v cnd] pred & body]
  `(loop[]
     (let [~v ~cnd]
       (when (~pred ~v)
         ~@body
         (recur)))))

; Chas Emerick     	
(defmacro let-map
   "Equivalent of (let [a 5 b (+ a 5)] {:a a :b b})."
   [kvs]
   (let [keys (keys (apply hash-map kvs))
         keyword-symbols (mapcat #(vector (keyword (str %)) %) keys)]
   `(let [~@kvs]
      (hash-map ~@keyword-symbols))))

; Laurent Petit     	
(defmacro mset!
  "Multiple set! calls on the same instance.
   inst-expr is an expression creating the instance
   on which the set!s will be applied to.
   field-sym-exprs is a succession of field symbols and exprs
   to be assigned to each field.
   Returns the instance.
   Example: (mset! (SomeBean.) field1 val1 field2 val2) => (SomeBean.)"
  [inst-expr & field-sym-exprs]
  (let [to-sym (gensym)
        one-set!-fn (fn [[f e]] (list 'set! (list '. to-sym f) e))]
    (concat (list 'let [to-sym inst-expr])
            (map one-set!-fn (partition 2 field-sym-exprs))
            (list to-sym))))

; Stuart Sierra
(defmacro defn-with-doc
  "Like defn but accepts a procedurally generated string."
  [fun doc-str & body]
  `(let [f# (defn ~fun ~@body)]
     (alter-meta! (var ~fun) assoc :doc ~doc-str)
     f#))

(defmacro defmacro-with-doc
  "Like defmacro but accepts a procedurally generated string."
  [macro doc-str & body]
  `(let [m# (defmacro ~macro ~@body)]
     (alter-meta! (var ~macro) assoc :doc ~doc-str)
     m#))
(defmacro defn-with-args
  "Like defn but takes non-literal string doc,
  and a map of arguments instead of a vector.
  args should be a map of argument names to descriptions."
  [fun doc-str args & body]
  `(let [f# (defn ~fun ~(vec (map symbol (keys args)))
              ~@body)]
     (alter-meta! (var ~fun) assoc :doc
                  ~(str doc-str \newline "  "
                       args))
     (alter-meta! (var ~fun) assoc :argdescs
                  ~(vec (vals args)))
     f#))

(def *debug* false)
(defmacro check-preconditions
  "Ensure preconditions are met."
  [m]
  (if *debug*
    `(assert (apply = true (map #((val %) (key %)) ~m)))))
(defmacro defn-by-contract
  "Like defn but takes non-literal string doc,
  and a map of arguments instead of a vector.
  args should be a map of strings to preconditions."
  [fun doc-str args & body]
  `(let [f# (defn ~fun ~(vec (map symbol (keys args)))
              (check-preconditions args)
              ~@body)]
     (alter-meta! (var ~fun) assoc :doc
                  ~(str doc-str \newline "  "
                       args))
     (alter-meta! (var ~fun) assoc :argdescs
                  ~(vec (vals args)))
     f#))

;(defmacro defnc
  ;[name &fdesc]
  ;(defn ~name
    ;~(if *debug*
      ;(assert p a))))

(defn re-fn
  "Construct a regular expression from string.
  Calling a regular expression with no arguments returns a Pattern.
  Calling a regular expression with a string argument returns nil
  if no matches, otherwise the equivalent of (re-seq restring).
  eg: ((re-fn \"2.\") \"12324251\") -> (\"23\" \"24\" \"25\")"
  [#^String string]
  (let [pp (re-pattern string)]
    (fn re
      ([] pp)
      ([s] (let [groups (re-seq pp s)]
             (if (first groups)
               groups
               nil))))))

(defmacro amap-in-place
  [a idx ret expr]
  `(let [a# ~a]
     (loop [~idx (int 0)]
       (when (< ~idx (alength a#))
         (aset a# ~idx ~expr)
         (recur (unchecked-inc ~idx))))
     a#))

(defn extend-tuple
  [default & lists]
  (lazy-seq
    (let [seqs (map seq lists)]
      (when (some identity seqs)
        (cons (map (fnil first [default]) seqs)
              (apply extend-tupel default (map rest seqs)))))))

; Rich Hickey
(defn fnil
  "Takes a function f, and returns a function that calls f, replacing
  a nil first argument to f with the supplied value x. Higher arity
  versions can replace arguments in the second and third
  positions (y, z). Note that the function f can take any number of
  arguments, not just the one(s) being nil-patched."
  ([f x]
     (fn
       ([a] (f (if (nil? a) x a)))
       ([a b] (f (if (nil? a) x a) b))
       ([a b c] (f (if (nil? a) x a) b c))
       ([a b c & ds] (apply f (if (nil? a) x a) b c ds))))
  ([f x y]
     (fn
       ([a b] (f (if (nil? a) x a) (if (nil? b) y b)))
       ([a b c] (f (if (nil? a) x a) (if (nil? b) y b) c))
       ([a b c & ds] (apply f (if (nil? a) x a) (if (nil? b) y b) c ds))))
  ([f x y z]
     (fn
       ([a b] (f (if (nil? a) x a) (if (nil? b) y b)))
       ([a b c] (f (if (nil? a) x a) (if (nil? b) y b) (if (nil? c) z c)))
       ([a b c & ds] (apply f (if (nil? a) x a) (if (nil? b) y b) (if (nil? c) z c) ds)))))

(defn conj-in
  "Returns the resultant map
  of appending an item to a vector in map m with key k"
  [m k item]
  (update-in m ks (fnil conj []) item))
;(conj-in {} [:a] 1)
;-> {:a [1]}

