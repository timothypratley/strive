{println
 [{:example (println "hi"), :result nil, :vote-ups 0, :vote-downs 0}],
 let [{:example (let [a 1] a), :result 1, :vote-ups 0, :vote-downs 0}],
 defn [{:example (defn foo [] foo), :result foo, :votes 0}]}
