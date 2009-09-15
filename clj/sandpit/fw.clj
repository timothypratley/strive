; Floyd-Warshall
; http://en.wikipedia.org/wiki/Floyd-Warshall_algorithm

(ns timothypratley.fw
  (:require [clojure.contrib.pprint :as pp]))

(declare fw-step fw-walk construct-path add-paths min-path init-table)
(def Inf Double/POSITIVE_INFINITY)


(defn floyd-warshall
  "Constructs all shortest paths of a graph"
  [nodes links]
  (reduce fw-step (init-table links) (fw-walk nodes)))

(defn get-path
  "Returns the shortest path between two nodes"
  [fw-table from to]
  (construct-path fw-table from to
                  [{:node from,
                    :step-cost 0,
                    :remaining-cost (:cost (fw-table [from to]))}]))


(defn- fw-walk
  "Create a lazy sequence of the k i j combinations"
  [nodes]
  (for [k nodes, i nodes, j nodes] [k i j]))

(defn- fw-step
  "From a map of existing shortest paths, produce the next iteration"
  [fw-table [k i j]]
  (let [ij (fw-table [i j])
        ik (fw-table [i k])
        kj (fw-table [k j])
        ikj (add-paths ik kj)]
    (assoc fw-table [i j] (min-path ij ikj))))

(defn- construct-path
  "Builds a path from the shortest paths table"
  [fw-table from to path]
  (if-let [step (fw-table [from to])]
    (let [n (:next step)
          sc (:cost (fw-table [from n]))
          rc (- (:cost step) sc)
          np (concat path [{:node n, :step-cost sc, :remaining-cost rc}])]
      (if (= n to)
        np
        (recur fw-table n to np)))))

(defn- add-paths
  "Combine two paths to make one larger path"
  [p1 p2]
  (if (and p1 p2)
    {:cost (+ (:cost p1) (:cost p2)), :next (:next p1)}))

(defn- min-path
  "Choose the least cost path"
  [p1 p2]
  (cond
    (and p1 p2) (if (< (:cost p1) (:cost p2)) p1 p2)
    p1 p1
    p2 p2))

(defn- init-table
  "Prepares the internal table format from an external set of links"
  [links]
  (reduce (fn [m [k v]] (assoc m k {:cost v, :next (second k)}))
          (sorted-map) links))


; Example

(let [G {:nodes (range 0 9)
         :links (sorted-map
                  [0 1] 1
                  [0 3] 1
                  [1 2] 1
                  [2 0] 1
                  [3 0] 1
                  [3 4] 1
                  [3 6] 1
                  [4 5] 1
                  [5 3] 1
                  [6 3] 1
                  [6 7] 1
                  [7 8] 1
                  [8 6] 1)}
      shortest-paths (floyd-warshall (:nodes G) (:links G))]
  (println "shortest-paths:")
  (pp/pprint shortest-paths)
  (println)
  (println "shortest-path 7 2:")
  (pp/pprint (get-path shortest-paths 7 2)))

