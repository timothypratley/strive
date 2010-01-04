(ns #^{:doc "Utilities for working with examples"
       :author "Timothy Pratley"
       :created "Dec 2009"
       :license "EPL"}
  cljeg.util
  (:use clojure.contrib.duck-streams)
  (:use clojure.test)
  (:use cljeg.example))

(defn gen-examples
  [filename]
  (with-open [w (writer "examples.txt")]
    (.write w (get-all-examples))))

; Using a separate map as an alternative to meta-data
;   * Allows non-resolvable keywords
;   * Completely external to core
; These are both positives and negatives...
; For now I think using meta-data is better,
; just because that's how docstrings work.
; However I've included this as an alternative.

(def examples (ref {}))

(defmacro add-example
  [k form result desc]
  (deftest foo-example-test (is (eval form) result))
  (dosync (alter examples conj-in k
                 {:form form, :result result, :desc desc})))

(defn get-first-example-from-map
  [k]
  (if-let [es (@examples k)]
    (example-str (first es))))

(defn get-all-examples-from-map
  [k]
  (if-let [es (@examples k)]
    (str (interleave (map example-str es) (constantly \newline)))))

(defn load-examples
  [filename]
  (with-open [r (java.io.PushbackReader. (reader filename))]
    (read r)))

(defn save-examples-from-map
  [examples filename]
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

