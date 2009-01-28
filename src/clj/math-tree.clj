(set! *warn-on-reflection* true)
(ns timothypratley.equation-tree
  (:require [clojure.zip :as zip]))

(defn group [a b] (list a b))
(defn expt [base pow] (Math/pow base pow))

(defn last-child
  [loc]
  "Find the most right and deepest child, from which new items will be added"
  (if (zip/branch? loc)
    (last
      (take-while identity (iterate zip/right (zip/down loc))))
    loc))

(defn insert-parent
  [loc n]
  "unused play function"
  (let [u (zip/up loc)]
    (if (nil? u)
      (zip/append-child (zip/seq-zip (list n)) (zip/node loc))
      (zip/insert-child u n))))

(defn usurp
  [loc n]
  "n takes loc, making the previous node its child"
  (zip/insert-child (zip/edit loc list) n))

(defn find-nest
  "Searches up the tree for the correct operator location by precedence"
  [loc op]
  (let [parent (zip/up loc)]
    (if parent
      (let [precedence {\+ 1, \* 2, \^ 3, group 4}
            op-p (precedence op)
            loc-p (if (zip/branch? loc)
                    (precedence (first (zip/node loc)))
                    (precedence (first (zip/node parent))))]
        (cond
          (> op-p loc-p) (usurp loc op)
          (< op-p loc-p) (find-nest (zip/up loc) op)
          :else loc))
      ; no parent, op becomes the root
      (if (or (number? (zip/node loc)) (not= (first (zip/node loc)) op))
        (usurp loc op)
        ; else op was already the root, so just keep it
        loc))))

(defn build-expression
  "Adds an item to a zipper based upon precedence"
  [loc item]
  (if (nil? loc)
    ; first item begins a tree containing only itself
    (zip/seq-zip item)
    (if (number? item)
      ; numbers are just appended at current location
      (zip/append-child loc item)
      ; operators may create a nesting
      (find-nest (last-child loc) item))))

(def t [1 \+ 2 \* 3 \^ 4 \+ 5 \* 6])
(println "Test expression" t)
(println (zip/root (reduce build-expression nil t)))


