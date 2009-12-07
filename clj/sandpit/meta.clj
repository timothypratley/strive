(ns metatest
  (:use clojure.test))

(def cascade-type-to-virtual-folder {:view "view"
                                     :action "action"
                                     :willow "willow"})


(defmacro path-to-function
  "Calculates the path to a given view or action function. The result is a path string, relative to the context root. If the function defines :path meta-data, that it used, otherwise an appropriate path is constructed within the virtual /view or /action folder."
  [function]
  `(let [fn-meta# (meta (var ~function))
        type# (get fn-meta# :cascade-type)
        folder# (get cascade-type-to-virtual-folder type#)
        fn-path# (get fn-meta# :path)]
    (println "function " ~function " meta" fn-meta#)
    (when (or (nil? type#) (nil? folder#))
      (println "Function is neither a view function nor an action function."))
    (cond
      ;; TODO: The user-supplied path may need some doctoring. It should not start with or end
      ;; with a slash.
      (not (nil? fn-path#)) fn-path#

      ;; Go from type to folder

      true (str folder# "/" (ns-name (fn-meta# :ns)) "/" (name (fn-meta# :name)))))) 

(defn valid-view-fn {:cascade-type :view} [])
(defn valid-action-fn {:cascade-type :action} [])
(defn pathed-action-fn {:cascade-type :action :path "do/something"} [])
(defn pathed-view-fn {:cacade-type :view :path "show/something"} [])
(defn unknown-type-fn {:cascade-type :willow}[])
(defn no-cascade-type-fn [])

(println (path-to-function valid-view-fn))

#_(
(deftest test-path-to-function
  (are [f path] (= (path-to-function f) path)
    valid-view-fn "view/cascade.test-path-map/valid-view-fn"
    valid-action-fn "action/cascade.test-path-map/valid-action-fn"
    pathed-action-fn "do/something"
    pathed-view-fn "show/something")) 

(run-tests)
  )

