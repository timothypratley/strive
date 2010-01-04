(defn wiki-doc [v]
 (println (str "[[#" (.replace (str (:name ^v)) "!" "") "]] "))
 (println "----")
 (if (:arglists ^v)
   (doseq [args (:arglists ^v)]
       (print (str "===(//" (:name ^v) "// "))
     (apply print args)
     (println ")==="))
   (println (str "===//" (:name ^v) "//===")))
 (when (:macro ^v)
   (println "====Macro===="))
 (println (.replaceAll (.matcher #"(?<!\n)\n(?!\n)" (:doc ^v)) "")))

(defn wiki-docs []
 (println "[[toc]]")
 (println "=API=")
 (newline)
 (println "This is documentation for all of Clojure's functions and macros, arranged alphabetically within namespace. It is generated from the source and reflects the current SVN version. All namespaces are loaded by the runtime except clojure.inspector, clojure.parallel

Note: The special forms def, let etc are documented on the [[special_forms|special forms]] page, not here. Please consult that page before using Clojure.
")
 (doseq [ns (sort-by #(. % name) (all-ns))]
   (let [vs (for [v (sort-by (comp :name meta) (vals (ns-interns ns)))
                :when (and (:doc ^v) (not (:private ^v)))] v)]
     (when vs
       (println (str "==" (. ns name) "=="))
       (doseq [v vs] (wiki-doc v))))))

(wiki-docs)

