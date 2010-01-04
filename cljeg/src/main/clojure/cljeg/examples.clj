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
#_(example condp
         (condp + 5
           2 :>> inc)
         = 8
         "When using :>> the first true match gets passed to a function.")

; I don't find this example compelling because
; :alan is not transformed in any way there was no need to pass it
#_(example condp
         (condp get :alan
           #{:bill :ben :alex} :>> (fn [x] (println "Favorite person: " x))
           #{:derek :alan}     :>> (fn [x] (println "Lunch buddy: " x))
           (println "Don't like " :alan)) 
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

;;; help - this doesn't work
#_(example clojure.set/union
         (clojure.set/union #{1 2 3} #{1 4 7})
         = #{1 2 3 4 7}
         "")

;;; help - this doesn't work
#_(example clojure.zip/vector-zip
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


;; do macros

; is it bad form for a test to print output?
(example doseq
         (doseq [i [1 2 3 4]] (print i))
         = nil
         "Useful for side-effects like printing")

; I don't find this example very useful. Too complex.
; This is an example of using destructuring to pair an index with
; each element of a seq.
#_(doseq [[index word] (map vector 
                          (iterate inc 0) 
                          ["one" "two" "three"])]
  (prn (str index " " word)))


(example doall
         (let [a (atom 0)]
           (doall (map (partial swap! a +) [1 2 3]))
           @a)
         = 6
         "Forces the lazy-seq for when they perform actions.")

(example doall
         (doall (map #(println "hi" %) ["mum" "dad" "sister"]))
         = '(nil nil nil)
         "")

(example dorun
         (dorun (map #(println "hi" %) ["mum" "dad" "sister"]))
         = nil
         "Note dorun discards the results")

(example doto
         (doto (new java.util.HashMap)
           (.put "a" 1)
           (.put "b" 2))
         isa? java.util.HashMap
         "")

; this is no good as it requires Java3D
#_(example doto
         (.addChild *scene-graph*
                    (doto (KeyNavigatorBehavior.
                            (-> *universe*
                              .getViewingPlatform .getViewPlatformTransform))
                      (setSchedulingBounds 
                        (BoundingSphere. (Point3d.) 10000.0))))
         = nil
         "")


;; functions

(example fn
         (map (fn [a] (str "hi " a)) ["mum" "dad" "sister"])
         = ("hi mum" "hi dad" "hi sister")
         "")

(example fn
         (map #(str "hi " %) ["mum" "dad" "sister"])
         = '("hi mum" "hi dad" "hi sister")
         "Alternate fn syntax is to use #() and % as argument, or %1 %2 etc.")

(example fn
         (map #(class %) [1 "asd"])      
         = '(java.lang.Integer java.lang.String)
         "#() is shorthand for (fn [x]) and is sometimes called a lambda
         See the reader page
         (Macro characters -> Dispatch -> Anonymous function literal)
         for an explanation of the '%' and other characters used to refer
         to function arguments.")


;; hash-maps

(example get
         (get {:a 1, :b 2} :a)
         = 1
         "")

(example get
         (get {:a 1, :b 2} :e 0) 
         = 0
         "Get also accepts an optional third argument, which is returned
         if key is not found.")
         
(example get
         ({:a 1, :b 2, :c 3} :a) 
         = 1
         "Hash-maps are functions of their keys, they delegate to get.")

(example get
         (:b {:a 1, :b 2} 99)
         = 2
         "Keywords are functions which delegate to get.")

(example assoc-in
         (let [nested {:level 0, 
                       :nested1 {:level 1, 
                                 :nested2 {:level 2, 
                                           :final-data "initial data"}}}]
           (assoc-in nested
                     [:nested1 :nested2 :final-data]
                     "new data"))
         = {:level 0,
            :nested1 {:level 1,
                      :nested2 {:level 2,
                                :final-data "new data"}}}
         "")


;; java interop

(example memfn
         (map (memfn charAt i) ["fred" "ethel" "lucy"] [1 2 3])
         = (\r \h \y)
         "")

(example proxy
         (defn rev-vector-seq
           [v]
           (when (< 0 (count v))
             (proxy [clojure.lang.ISeq] []
               (seq   [] this)
               (first [] (peek v))
               (rest  [] (rev-vector-seq (pop v))))))
         fn?
         "")

;;; help - this doesn't work
#_(example proxy
         (doto (javax.swing.JFrame.)
           (addKeyListener
             (proxy [java.awt.event.KeyListener] []
               (keyPressed [e] (println (.getKeyChar e) " key pressed"))
               (keyReleased [e] (println (.getKeyChar e) " key released"))
               (keyTyped [e] (println (.getKeyChar e) " key typed"))))
           (setVisible true))
         (partial not nil?)
         "")

(example into-array
         (into-array [1 2 3])
         (partial not nil?)
         "#<Integer[] [Ljava.lang.Integer;@15fadcf>")

; doesn't add anything
;user=> (into-array (map double-array [[1.0] [2.0]])) 
;#<double[][] [[D@1fa1bb6> 


;;; gen-class example not suited to example macro
;expmeth/ClassA.java: 
;package expmeth; 
;public class ClassA { 
;    public void hello() { 
;        System.err.println("hello from Java!"); 
;    } 
;    public void hello(int x) { 
;        System.err.println("hello from Java " + x); 
;    } 
;} 

;expmeth/TestMe.clj: 
;(ns expmeth.TestMe 
;  (:gen-class 
;   :extends expmeth.ClassA 
;   :exposes-methods {hello helloSuper})) 
;(defn -hello 
;  ([this] 
;     (.helloSuper this) 
;     (println "hello from clojure!")) 
;  ([this x] 
;     (.helloSuper this x) 
;     (println "hello from clojure..." x))) 
;testing: 
;(.hello (expmeth.TestMe.) 17) 
;(.hello (expmeth.TestMe.) )


;; mapping operators

(example map
         (map + [1 2 3 4] [1 2 3 4])
         = '(2 4 6 8)
         "")

(example reduce
         (reduce * [2 3 4])
         = 24
         "")

(example reduce
         (reduce + (filter odd? (range 100)))
         = 2500
         "Sum the odd numbers.")

(example apply
         (apply str [1 2])
         = "12"
         "")

(example apply
         (apply * (range 2 11))
         = 3628800
         "Factorial 10.")  


;; multimethods

(example defmulti
         (do
           (defmulti rand-str (fn [] (> (rand) 0.5)))
           (defmethod rand-str true [] "true")
           (defmethod rand-str false [] "false")
           (for [x (range 5)] (rand-str)))
         = '("false" "false" "true" "true" "false")
         "Using multi-methods without using a hierarchy.")

(example defmulti
         (do
           (defmulti area :Shape)
           (defn rect [wd ht] {:Shape :Rect :wd wd :ht ht})
           (defn circle [radius] {:Shape :Circle :radius radius})
           (defmethod area :Rect [r]
             (* (:wd r) (:ht r)))
           (defmethod area :Circle [c]
             (* (. Math PI) (* (:radius c) (:radius c))))
           (defmethod area :default [x] :oops)
           (let [r (rect 4 13)
                 c (circle 12)]
             [(area r) (area c) (area {})]))
         = [52 452.3893421169302 :oops]
         "")

(example fib
         (do
           (defmulti fib int) 
           (defmethod fib 0 [_] 1) 
           (defmethod fib 1 [_] 1) 
           (defmethod fib :default [n] (+ (fib (- n 2)) (fib (- n 1)))) 
           (map fib (range 10)))
         = '(1 1 2 3 5 8 13 21 34 55) 
         "")


;; predicates

(example filter
         (filter nil? [:a :b nil nil :a])
         = '(nil nil)
         "")

(example filter
         (filter (fn [x] (= x :b)) [:a :b nil nil :a])
         = '(:b)
         "")

(example remove
         (remove nil? [:a :b nil nil :a]) 
         = '(:a :b :a)
         "")

(example every?
         (every? string? ["hi" 1 2])
         = false
         "")

(example some
         (some string? [1 2 "hi" 3])
         = "hi"
         "")


;; recursion

; note this should be cross-referenced with recur
(example loop
         (loop [cnt 5, acc 1]
           (if (zero? cnt)
             acc
             (recur (dec cnt) (* acc cnt))))
         = 120
         "Computes the factorial of 5, establishes two 'variables' cnt and acc.
         cnt is decremented every call until it reaches 0.
         acc stores the result of multiplying each value cnt took.")

; TODO
;(example lazy-seq)


;; refs

;See ref-set (cross-reference?)
;(example ref)

(example ref-set
         (let [foo (ref 0)]
           (dosync (ref-set foo 1))
           @foo)
         = 1
         "")

(example deref
         (deref (atom 1))
         = 1
         "")

(example deref
         (@(atom 1))
         = 1
         "@ is the same as deref and works on atoms, refs, agents,
         futures, and local-vars.")

(example with-local-vars
         (with-local-vars [x 5] @x)
         = 5
         "")

(example delay
         (delay (inc 1))
         delay?
         "Inc 1 is not computed.")

(example force
         (force (delay (inc 1)))
         = 1
         "Inc 1 is computed.")

;;; does not translate to examle macro
;(def df (delay (println "hello")))
;(println "world")
;--> world
;(force df)
;--> hello

(example commute
         (let [employee-records (ref #{})]
           (dosync (commute employee-records conj "employee"))
           @employee-records)
         = #{"employee"}
         "")

               
;; sequence building

(example conj
         (conj [1 2 3] 4)
         = [1 2 3 4]
         "")

(example conj
         (conj '(:a :b :c) \d)
         = (\d :a :b :c)
         "")

(example concat
         (concat [1 2] [3 4])
         = '(1 2 3 4)
         "")

; too complex
#_(defn poly-expand
  poly-expand [points]
  (loop [aa (first points) remaining (rest points) built (empty points)]
    (if (empty? remaining)
      (concat built [aa (first points)])
      (recur (first remaining) (rest remaining)
		     (concat built [aa (first remaining)])))))
;(poly-expand '[a b c d])
;-> [a b b c c d d a]

(example merge 
         (merge {:a 1} {:b 2})
         = {:a 1, :b 2}
         "Combine two maps")

(example merge-with
         (merge-with + {:a 1} {:a 2, :b 3})
         = {:a 3 :b 3}
         "")


;; sequence operators

(example interpose
         (apply str (interpose "|" ["hi" "mum" "and" "dad"]))
         = "hi|mum|and|dad"
         "")

(example interleave
         (interleave [1 2 3] [:a :b :c])
         = (1 :a 2 :b 3 :c)
         "")

(example reverse 
         (apply str (interpose " " (reverse (.split "I am cold" " "))))
         = "cold am I"
         "")

(example butlast 
         (butlast "hello")
         = (\h \e \l \l)
         "")

(example replace 
         (apply str (replace {\l ""} "hello world"))
         = "heo word"
         "")

;; structures

;;; too interactive
;(example accessor)
;user=> (defstruct employee :name :id)                                        
;#'user/employee
;user=> (def e (struct employee "John" 123))
;#'user/e
;user=> e
;{:name "John", :id 123}
;user=> ("name" e) ; FAIL: string not an accessor
;java.lang.ClassCastException: java.lang.String cannot be cast to clojure.lang.IFn (NO_SOURCE_FILE:0)
;user=> (:name e)                                                         
;"John"
;user=> (def employee-name (accessor employee :name))  ; bind accessor to e-name
;#'user/employee-name
;user=> (employee-name e) ; use accessor
;"John"

;;;;; too interactive
;#_(example defstruct
;user=> (defstruct employee :name :id)
;#'user/employee
;user=> (struct employee "Mr. X" 10)
;{:name "Mr. X", :id 10}
;user=> (struct-map employee :id 20 :name "Mr. Y")
;{:name "Mr. Y", :id 20}
;user=> (def a (struct-map employee :id 20 :name "Mr. Y"))
;#'user/a
;user=> (def b (struct employee "Mr. X" 10))
;#'user/b
;user=> (:name a) ; observe that :name is an accessor
;"Mr. Y"
;user=> (:id b)   ; same with :id
;10
;user=> (b :id)
;10
;user=> (b :name)
;"Mr. X"
;user=> (assoc a :name "New Name")
;{:name "New Name", :id 20}
;user=> a                   ; note that 'a' is immutable and did not change
;{:name "Mr. Y", :id 20}
;user=> (def a1 (assoc a :name "Another New Name")) ; bind to a1
;#'user/a1
;user=> a1
;{:name "Another New Name", :id 20}

