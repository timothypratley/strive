(ns cljeg.util
  (:use clojure.test))

(defn conj-in
  [m k item]
  (assoc m k (conj (get m k []) item)))

(defmacro example-str
  [e]
  (let [form (:form e)
        result (:result e)
        desc (:desc e)]
    (str form \newline
         " -> " (pr-str result) \newline
         (when desc (str desc \newline)))))

; Using meta-data
(defn example
  [k form result desc]
  (deftest foo-example-test (is (eval form) result))
  (alter-meta! (var k) conj-in :example
               {:form form, :result result, :desc desc}))

(defn example-str
  [e]
  (let [form (:form e)
        result (:result e)
        desc (:desc e)]
    (str form \newline
         " -> " (pr-str result) \newline
         (when desc (str desc \newline)))))

(defn get-first-example
  [k]
  (if-let [es (:examples (meta (var k)))]
    (example-str (first es))))

(defn get-all-examples
  [k]
  (if-let [es (:examples (meta (var k)))]
    (str (interleave (map example-str es) (constantly \newline)))))


; Using a separate map
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


(example defn
         '(defn foo [] 1)
         nil
         "Declares a new function foo with no arguments that returns 1")


