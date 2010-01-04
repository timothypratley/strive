(ns cljeg.examples
  (:use cljeg.example))

; Note - I'd prefer not to quote the expected result
; Also, where a description is not required we could leave it off,
; but that would break the arity selection
(example ->
         (macroexpand-1 '(-> true (if "clojure")))
         = '(if true "clojure")
         "This is the threading operator.")

(example ->
         (-> "abc" .toUpperCase (.replace "B" "-"))
         = "A-C"
         "Calls toUpperCase then uses the result in the replacement.")

(example ->
         (-> true (if inc dec) (map [1 2 3]))
         = '(2 3 4)
         "-> works with functions and macros")

;; help - how do I avoid the quote quoth the raven
#_(example ->
         (do
           (import '(java.net URL) '(java.util.zip ZipInputStream))
           '(-> "http://clojure.googlecode.com/files/clojure_20081217.zip"
             URL. .openStream ZipInputStream. .getNextEntry bean :name))
         = "clojure/"
         "Here is an example that should make every devoted Programmer
         fall in love with Lisp, if he compares it with equivalent Java code.
         Reading the name of the first entry in a zip file.")

(example cond
         (do
           (defn fib [n]
             (cond
               (== 0 n) 0 
               (== 1 n) 1
               (< 1 n) (+ (fib (- n 1)) (fib (- n 2))))) 
           (fib 4))
         = 3
         "A Fibonacci number function based on conditions.")

(example condp
         (condp = 5
             1 "hi"
             5 "fun")
         = "fun"
         "Checks (partial = a) against the listed conditions")

(example condp
         (let [a 7]
           (condp = a
             1 "hi"
             5 "fun"
             "no match"))
         = "no match"
         "condp throws an java.lang.IllegalArgumentException if no clauses
         are matched in the above example. condp also allows to add a single
         default expression after the clauses whose value will be returned
         if no clauses matches.")

; I don't find this example compelling.
; The only interesting usage for :>> I've see is
; http://groups.google.com/group/clojure/msg/d9ef152e19f5416b
; which is too complex as an example.
(example condp
         (condp + 5
           2 :>> inc)
         = 8
         "When using :>> the first true match gets passed to a function.")

; I don't find this example compelling because
; :alan is not transformed in any way there was no need to pass it
(example condp
         (condp get :alan
           #{:bill :ben :alex} :>> (fn [x] (println "Favorite person: " x))
           #{:derek :alan}     :>> (fn [x] (println "Lunch buddy: " x))
           (println "Don't like " p)) 
         = "Lunch buddy: :alan"
         "")
(example str
         (str [1 2])
         = "[1 2]"
         "The vector represented as a string.")

(example str
         (str 1 2)
         = "12"
         "Multiple arguments are strung together.")

;; help, why does zip/zipper get evaluated?
#_(example require
         (do
           (require '[clojure.zip :as zip])
           'zip/zipper)
         fn?
         "")

(example rem
         (rem 5 2)
         = 1
         "")

(example test
         (do
           (defn #^{:test (fn [] (assert (= 4 (myadd 2 2))))} myadd
             [a b]
             (+ a b))
           (test #'myadd))
         = :ok
         "")

; this throws a npe
#_(example union
         (clojure.set/union #{1 2 3} #{1 4 7})
         = #{1 2 3 4 7}
         "")

; this throws a npe
#_(example vector-zip
         (do
           (use 'clojure.zip)
           (-> (vector-zip [[1 2] 3 [[4 5] 7 8]])
             down right right down down right
             (edit inc)
             root))
         = [[1 2] 3 [[4 6] 7 8]]
         "A zipper is a way to modify a tree in a functional way.
         To do this conveniently, there needs to be a way for you to
         drill down a branch in the tree and perform a local edit,
         without having to tediously rebuild the upper areas of the
         tree again. In this example, the vector-zip function converts
         nested arrays into a zipper. Then we drill down the zipper
         with down and right. Once we've reached the node we want to edit,
         we do so with edit, in this case, incrementing the number. Then
         we can call root to convert the zipper back into nested arrays.
         Note that we were able to efficiently make a pinpoint change in
         the tree in a very elegant manner.")

(example zipmap
         (let [ks [1 3 4]] (zipmap ks (map inc ks)))
         = {4 5, 3 4, 1 2}
         "")


