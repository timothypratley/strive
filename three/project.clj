(defproject three "1.0.0-SNAPSHOT"
            :description "three game engine"
            :main three
            :dependencies [[org.clojure/clojure "1.2.0-master-SNAPSHOT"]
                           [org.clojure/clojure-contrib "1.2.0-master-SNAPSHOT"]
                           [native-deps "1.0.0"]
                           [tjpext "1.0.0-SNAPSHOT"]]
            :native-dependencies [[j3d "1.5.2"]]
            :dev-dependencies [[leiningen-run "0.2"]])
