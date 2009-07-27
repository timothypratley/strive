(ns timothypratley.extensions)

(defn assoc-conj
  "Add to a collection in a map."
  [m k v default]
  (assoc m k (conj (m k default) v)))

(defmacro switch
  [v & body]
  `(condp (partial = ~v)
     ~@body))

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
