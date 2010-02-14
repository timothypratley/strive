(ns timothypratley.extensions)

(defn repeat-n
  "Creates a lazy sequence of n values."
  [value n]
  (take n (repeat value)))

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

; Christophe Grand
;http://groups.google.com.au/group/clojure/browse_thread/thread/9f77163a9f29fe79
(defmacro while-let
  "Makes it easy to continue processing an expression as long as it is true"
  [binding & forms]
   `(loop []
      (when-let ~binding
        ~@forms
        (recur))))

; Chas Emerick
;http://groups.google.com.au/group/clojure/browse_thread/thread/a4e1dac88ab8becb
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

; deprecated - core now provides these
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
; ((re-fn "2.") "12324251")
; = ("23" "24" "25")"


(defmacro amap-in-place
  [a idx ret expr]
  `(let [a# ~a]
     (loop [~idx (int 0)]
       (when (< ~idx (alength a#))
         (aset a# ~idx ~expr)
         (recur (unchecked-inc ~idx))))
     a#))

; Meikel Brandmeyer
;http://groups.google.com.au/group/clojure/browse_thread/thread/373846d81d528a9e
(defn extend-tuple
  "Lazily creates tuples from given colls, filling the ones that are
  empty before the rest with the default value."
  [default & lists]
  (lazy-seq
    (let [seqs (map seq lists)]
      (when (some identity seqs)
        (cons (map (fnil first [default]) seqs)
              (apply extend-tupel default (map rest seqs)))))))

; Nicholas Buduroi
;http://groups.google.com.au/group/clojure/browse_thread/thread/373846d81d528a9e
(defn map-pad
  "Like map but if the collections aren't the same size, the smaller ones
  are padded with the given default value."
  [f default & colls]
  (map #(apply f %) (apply extend-tuple default colls)))

; Rich Hickey
;http://groups.google.com.au/group/clojure/browse_thread/thread/99cc4a6bfe665a6e
#_(defn fnil
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

; I prefer the function to come last
(defn fnil 
  "Creates a new function that if passed nil as an argument,
  operates with supplied arguments instead. The function must be
  passed as the last argument."
  [& more]
  (fn [& args]
    (let [f (last more)
          defaults (butlast more)]
      (apply f (map ; if arg is nil use default
                    #(if (nil? %1) %2 %1)
                    args
                    ; preserve arguments that have no default
                    (concat defaults (drop (count defaults) args)))))))

(defn conj-in
  "Returns the resultant map of appending an item to a vector in map m
  identified by keys ks which is a vector of keys for nested maps,
  similar to update-in. If there is no existing vector, one is created."
  [m ks item]
  (update-in m ks (fnil [] conj) item))
; (conj-in {} [:a] 1)
; = {:a [1]}

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

(defn subseq2
  [coll start end]
  (take (- end start) (drop start coll)))
; (subseq2 [0 1 2 3 4] 1 3)
; = (1 2)

; Constantine Vetoshev
;http://groups.google.com.au/group/clojure/browse_thread/thread/d6b5fa21073541c1
(defmacro let-kw
  "Adds flexible keyword handling to any form which has a parameter
   list: fn, defn, defmethod, letfn, and others. Keywords may be
   passed to the surrounding form as & rest arguments, lists, or
   maps. Lists or maps must be used for functions with multiple
   arities if more than one arity has keyword parameters. Keywords are
   bound inside let-kw as symbols, with default values either
   specified in the keyword spec or nil. Keyword specs may consist of
   just the bare keyword symbol, which defaults to nil, or may have
   the general form [keyword-name keyword-default-value*
   keyword-supplied?*].  keyword-supplied?  is an optional symbol
   bound to true if the keyword was supplied, and to false otherwise."
  [kw-spec-raw kw-args & body]
  (let [kw-spec  (map #(if (sequential? %) % [%]) kw-spec-raw)
        symbols  (map first kw-spec)
        keywords (map (comp keyword name) symbols)
        defaults (map second kw-spec)
        destrmap {:keys (vec symbols) :or (zipmap symbols defaults)}
        supplied (reduce
                  (fn [m [k v]] (assoc m k v)) (sorted-map)
                  (remove (fn [[_ val]] (nil? val))
                          (partition 2 (interleave
                                        keywords
                                        (map (comp second rest)
                                             kw-spec)))))
        kw-args-map (gensym)]
    `(let [kw-args# ~kw-args
           ~kw-args-map (if (map? kw-args#)
                            kw-args#
                            (apply hash-map kw-args#))
           ~destrmap ~kw-args-map]
       ~@(if (empty? supplied)
             body
             `((apply (fn [~@(vals supplied)]
                        ~@body)
                      (map (fn [x#] (contains? ~kw-args-map x#))
                           [~@(keys supplied)])))))))

; Stuart Halloway
;http://github.com/stuarthalloway/circumspec/blob/master/src/circumspec/utils.clj
(defmacro wtf
  "'What the form' is going on? Convenience for macroexpand."
  [form]
  `(pprint (macroexpand-1 '~form)))

; Arthur Ulfeldt
;http://stackoverflow.com/questions/2022911/idiomatic-clojure-for-progress-reporting
(defn seq-counter 
  "Calls callback after every nth entry in sequence is evaluated. 
  Optionally takes another callback to call once the seq is fully evaluated."
  ([coll finished-callback]
   (drop-last (lazy-cat coll
                        [(finished-callback)])))
  ([coll n callback]
     (map (fn [index value]
            (when (= (rem index n) 0)
              (callback index))
            value)
          (iterate inc 1) coll))
  ([coll n callback finished-callback]
     (drop-last (concat (seq-counter coll n callback) 
                  (lazy-seq [(finished-callback)])))))

; NB: the output is a bit strange...
;(seq-counter [1 2 3 4 5] #(println "DONE"))
;(seq-counter [1 2 3 4 5] 2 #(println \# %))
;(seq-counter [1 2 3 4 5] 2 #(println \# %) #(println "DONE"))
;(map (fn [x] (Thread/sleep 1000) x) (seq-counter [1 2 3 4 5] 2 #(println \# %) #(println "DONE")))
;(map (fn [x] (Thread/sleep 1000) (.flush System/out) x) [1 2 3 4 5])

; Sean Devlin
;http://groups.google.com/group/clojure/browse_thread/thread/ed567fa0b9db548c
(defn every-pred?
   "Mimics AND"
   [& preds]
   (fn [& args] (every? #(apply % args) preds)))

; Sean Devlin
;http://groups.google.com/group/clojure/browse_thread/thread/ed567fa0b9db548c
(defn any-pred?
   "Mimics OR"
   [& preds]
   (fn [& args] (some #(apply % args) preds)))

; Sean Devlin
;http://groups.google.com/group/clojure-dev/browse_thread/thread/51982e4091e3614d
(defn fkey
  "Returns a function that applies f to the key of a mapentry"
  [f] (fn [[k v]] [(f k) v]))

; Sean Devlin
;http://groups.google.com/group/clojure-dev/browse_thread/thread/51982e4091e3614d
(defn fval
  "Returns a function that applies f to the val of a mapentry"
  [f] (fn [[k v]] [k (f v)]))

; Sean Devlin: same, see http://groups.google.com/group/clojure-dev/browse_thread/thread/51982e4091e3614d

(let [limit (.availableProcessors (Runtime/getRuntime))
      sem (java.util.concurrent.Semaphore. limit)]
  (defn submit-future-call
    "Takes a function of no args and yields a future object that will
    invoke the function in another thread, and will cache the result and
    return it on all subsequent calls to deref/@. If the computation has
    not yet finished, calls to deref/@ will block. 
    If n futures have already been submitted, then submit-future blocks
    until the completion of another future, where n is the number of
    available processors."  
    [#^Callable task]
    ; take a slot (or block until a slot is free)
    (.acquire sem)
    (try
      ; create a future that will free a slot on completion
      (future (try (task) (finally (.release sem))))
      (catch java.util.concurrent.RejectedExecutionException e
        ; no task was actually submitted
        (.release sem)
        (throw e)))))

(defmacro submit-future
  "Takes a body of expressions and yields a future object that will
  invoke the body in another thread, and will cache the result and
  return it on all subsequent calls to deref/@. If the computation has
  not yet finished, calls to deref/@ will block.
  If n futures have already been submitted, then submit-future blocks
  until the completion of another future, where n is the number of
  available processors."  
  [& body] `(submit-future-call (fn [] ~@body)))
#_(example
    user=> (submit-future (reduce + (range 100000000)))
    #<core$future_call$reify__5782@6c69d02b: :pending>
    user=> (submit-future (reduce + (range 100000000)))
    #<core$future_call$reify__5782@38827968: :pending>
    user=> (submit-future (reduce + (range 100000000)))
    ;; blocks at this point for a 2 processor PC until the previous
    ;; two futures complete
    #<core$future_call$reify__5782@214c4ac9: :pending>)

; Mark Triggs
;http://dishevelled.net/Generating-Clojure-import-lines-using-SLIME.html
(defn find-classes [regex]
  (let [search-path-properities ["java.class.path" "sun.boot.class.path"]]
    (for [search-path search-path-properities
          jar (filter #(.endsWith % ".jar")
                      (.split (System/getProperty search-path)
                              (System/getProperty "path.separator")))
          entry (try (filter #(.endsWith (.getName %) ".class")
                             (enumeration-seq
                               (.entries (new java.util.jar.JarFile jar))))
                     (catch Exception _))
          name [(.. entry getName (replaceAll "\\.class$" ""))]
          :when (re-find regex name)]
      name)))

; Rich Hickey
;http://paste.lisp.org/display/67182
(defn jcall [obj name & args]
  (clojure.lang.Reflector/invokeInstanceMethod obj (str name)
    (if args (to-array args) clojure.lang.RT/EMPTY_ARRAY)))
(defn jfn [name]
  #(apply jcall %1 name %&))
#_(example ((jfn 'substring) "fred" 2 3))
#_(example ((jfn 'toUpperCase) "fred"))
;joubert
(defmacro getFnFromMethodName
  "Given a 'methodName', returns a function that invokes that method."
  [methodName] `(fn [target# val#]
		  (. target# ~(symbol methodName) val#)))

(defmacro with-timeout
  "Attempt to get the result of body within ms milliseconds."
  [ms & body]
  `(let [f# (future ~@body)]
     (.get f# ~ms java.util.concurrent.TimeUnit/MILLISECONDS)))
#_(example user=> (with-timeout 1 (reduce * (range 1 100001)))
           java.util.concurrent.TimeoutException (NO_SOURCE_FILE:0))
#_(example user=> (with-timeout 1 (reduce * (range 1 11)))
           3628800)

(defmacro japply
  [& args]
  (concat (butlast args) (last args)))
