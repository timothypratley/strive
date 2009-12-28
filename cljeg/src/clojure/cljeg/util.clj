; Timothy Pratley Dec 2009
; EPL
; Clojure example utilities

(ns cljeg.util
  (:use clojure.test))


; TODO: better as (update-in m ks [] conj item))?
(defn conj-in
  "Returns the resultant map
  of appending an item to a vector in map m with key k"
  [m k item]
  (assoc m k (conj (get m k []) item)))
;(conj-in {} :a 1)
;-> {:a [1]}

(defn example-str
  "Converts an example to human readable text"
  [e]
  (let [form (:form e)
        result (:result e)
        desc (:desc e)]
    (str form \newline
         " -> " (pr-str result) \newline
         (when desc (str desc \newline)))))
;(example-str {:form '(inc 1), :result 2, :desc "Returns the increment of 1"})
;-> "(inc 1)\n -> 2\nReturns the increment of 1\n"

; Using meta-data
(defmacro example
  "Declare an example"
  [k form result desc]
  `(do
    (alter-meta! ~(resolve k) conj-in :examples
                 {:form '~form, :result ~result, :desc ~desc})
    (deftest ~(symbol (str k "-example-" (inc (count (:examples (meta (resolve k))))) "-test"))
             (is (= ~form ~result)))))
;((example inc (inc 1) 2 "Returns the increment of 1"))
;-> nil
; Now we could declare an example of example, but that would in turn create an example
; when tested... which might not be desirable
;(example example ((example inc (inc 1) 2 "Returns the increment of 1")) nil
;         "Creates an example for inc which returns 2")

(defmacro get-first-example
  "Get the first example associated with some iref k as a human readable string"
  [k]
  `(if-let [es# (:examples (meta ~(resolve k)))]
    (example-str (first es#))))
;(get-first-example inc)
; Ok we could write an example for get-first-example...
; but hmmm its just a string formatting exercise. Gah!
; Alternatively just print the output.
(example get-first-example
         (print (get-first-example inc))
         nil
         "Prints the first example associated with inc")
; This is a less useful test and has side-effect (printing)...
; not sure if that's such a great idea.

(defmacro get-all-examples
  "Get all the examples associated with some iref k as a human readable string"
  [k]
  `(if-let [es# (:examples (meta ~(resolve k)))]
    (apply str (interleave (map (fn [x#] (str "Example " x# \: \newline)) (iterate inc 1))
                           (map example-str es#)
                           (repeat \newline)))))
;(get-all-examples inc)
(example get-all-examples
         (print (get-all-examples inc))
         nil
         "Prints all the examples associated with inc")

; If you wanted to execute the test for an example at declaration,
; just wrap it in another set of ()
; ((example inc (inc 1) 2 "Increment of 1 is 2"))
; Though I prefer to run the tests as part of the test-suite
; (run-tests)

(defn save-examples
  [examples]
  (with-open [w (writer "examples.clj")]
    (.write w (str "(ns examples (:use cljeg.util))" \newline \newline))
    (doseq [ekv examples]
      (let [k (key ekv)
            es (val ekv)]
        (doseq [e es]
          (.write w (str "(example " k \newline
                         "         " (:form e) \newline
                         "         " (:result e) \newline
                         "         " (:desc e) \newline)))))))

; Using a separate map as an alternative to meta-data
;   * Allows non-resolvable keywords
;   * Completely external to core
;   These are both positives and negatives...
;   For now I think using meta-data is better,
;   just because that's how docstrings work.

#_(

(def examples (ref {}))

(defmacro example-map
  [k form result desc]
  (deftest foo-example-test (is (eval form) result))
  (dosync (alter examples conj-in k
                 {:form form, :result result, :desc desc})))

(defn get-first-example-map
  [k]
  (if-let [es (@examples k)]
    (example-str (first es))))

(defn get-all-examples-map
  [k]
  (if-let [es (@examples k)]
    (str (interleave (map example-str es) (constantly \newline)))))

)

