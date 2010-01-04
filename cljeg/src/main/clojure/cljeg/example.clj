(ns #^{:doc "Utilities for creating testable documented examples."
       :author "Timothy Pratley"
       :created "Dec 2009"
       :license "EPL"}
  cljeg.example
  (:use clojure.test))

; Note: cannot (declare conj-in) because macro requires it at compiled time

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


(defmacro example
  "Declare an example which will be attached to the meta-data of a var,
  and create a test that checks the example returns the correct result.
  sym must resolve to a valid var.
  form is the example demonstrating the use of whatever sym is.
  result is the expected result of evaluating form.
  desc is a string describing the example."
  ([sym form comparator result desc]
   (example sym form `(partial ~comparator ~result) desc))
  ([sym form pred desc]
   `(do
     (alter-meta! ~(resolve sym) conj-in [:examples]
                  {:form '~form, :result '~pred, :desc ~desc})
     (deftest ~(symbol (str sym "-example-"
                            (inc (count (:examples (meta (resolve sym)))))
                            "-test"))
              (is (~pred ~form))))))

; here is an example of inc
(example inc
         (inc 1)
         = 2
         "Returns the increment of 1.")

; here is an example of example itself
(example example
         (example inc (inc -1) = 0 "-1 incremented is 0.")
         var?
         "Creates an example of inc. If inc already has an example,
         this example will just be appended (you can have multiple examples).
         Note the test is returned which is a function.")

; and some examples of the helper functions used by the example macro
(example fnil
         ((fnil 0 inc) nil)
         = 1
         "inc normally does fails if passed nil. Here we use fnil
         to operate on some default value instead of nil, in this case 0.")
(example fnil
         (reduce #(update-in %1 [%2] (fnil 0 inc)) {} [:a :a :b :c :a :b])
         = {:a 3, :b 2, :c 1}
         "Here we count up the frequency of unique items in a vector.
         Note that update-in will attempt to call its function on nil
         if there is no existing value for a key, and that inc
         would fail if passed nil.")
(example fnil
         ((fnil 1 +) nil 2 3)
         = 6
         "")
(example fnil
         ((fnil 1 2 +) 0 nil)
         = 2
         "")

(example conj-in
         (conj-in {:a [3 2]} [:a] 1)
         = {:a [3 2 1]}
         "Appends 1 to the vector identified by :a")

(defn example-str
  "Converts an example to human readable text."
  [e]
  (let [form (:form e)
        result (:result e)
        desc (:desc e)]
    (str form \newline
         " -> " (pr-str result) \newline
         (when desc (str desc \newline)))))
(example example-str
         (example-str (first (:examples (meta inc))))
         string?
         "Converts the first example of inc to a string.")

(defmacro get-first-example
  "Get the first example associated with sym as a human readable string."
  [sym]
  `(if-let [es# (:examples (meta ~(resolve sym)))]
    (example-str (first es#))))
; hmmm this is just a string formatting exercise. Gah!
; in the docs if string? passes we should put the actual return result
(example get-first-example
         (get-first-example inc)
         string?
         "Gets the first example associated with inc.")

(defmacro get-examples
  "Get all the examples associated with sym as a human readable string."
  [sym]
  `(if-let [es# (:examples (meta ~(resolve sym)))]
    (apply str (interleave (map (fn [x#] (str "Example " x# \: \newline))
                                (iterate inc 1))
                           (map example-str es#)
                           (repeat \newline)))))
(example get-examples
         (get-examples inc)
         seq
         "Retrieves all the examples associated with inc.")

(defmacro run-example
  "Obtain the result of executing the example for sym.
  Note that you would normally just execute the test using (run-tests),
  however run-example is included because checking the result
  is slightly different to getting the result. In some cases the example
  will check that a result is a string, but you might also want
  to see the string itself."
  [index sym]
  (if-let [es# (:examples (meta (resolve sym)))]
     (:form (nth es# index))))
(example run-example
         (run-example 0 inc)
         = 2
         "We are running the 0th example of inc, which returns 2")

; If you wanted to execute the test for an example at declaration,
; just wrap it in another set of ()
; ((example inc (inc 1) 2 "Increment of 1 is 2"))
; Though I prefer to run the tests as part of the test-suite
; (run-tests)

(defn get-ns-examples
  [ns]
  (mapcat #(map example-str (:examples %))
          (remove :private
                  (sort-by :name (map meta (vals (ns-interns ns)))))))

(defn get-all-examples
  []
  (map get-ns-examples (sort-by #(. % name) (all-ns))))

