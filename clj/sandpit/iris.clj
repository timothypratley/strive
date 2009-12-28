(ns timothypratley.iris)

(def parser (org.deri.iris.compiler.Parser.))
(.parse parser
  "man('homer').
  woman('marge').
  hasSon('homer','bart').
  isMale(?x) :- man(?x).
  isFemale(?x) :- woman(?x).
  isMale(?y) :- hasSon(?x,?y).
  ?-isMale(?x).")
(def kb (org.deri.iris.KnowledgeBaseFactory/createKnowledgeBase 
          (.getFacts parser) (.getRules parser)))
(doseq [q (.getQueries parser)]
  (println (str q " => " (.execute kb q))))


(let [tf (org.deri.iris.factory.Factory/TERM)
      bf (org.deri.iris.factory.Factory/BASIC)
      string-term (fn [#^String s]
                    (.createString tf s))
      variable (fn [#^String vname]
                 (.createVariable tf vname))
      tuple (fn [st]
              (.createTuple st))
      predicate (fn [#^String s #^Integer arity]
                  (.createPredicate tf s arity))
      atom (fn [pred tup]
             (.createAtom bf pred tup))
      literal (fn [atom]
                (.createLiteral bf true atom))
      rule (fn [head body]
             (.createRule bf head body))
      simple-rule (fn [#^String pred-name #^String term]
                    (rule
                      (literal
                        (atom
                          (predicate pred-name 1)
                          (tuple (string-term term))))))
      query (fn [lit]
              (.createQuery bf lit))])

; ok, actually using these primitives requires some deeper understanding
;
; man (predicate "man" 1)
; fact (simple-rule "man" "tim")
; facts {pred relation, pred relation}
              
; kb (org.deri.iris.KnowledgeBaseFactory/createKnowledgeBase 
;      facts rules)
; (.execute kb query)

