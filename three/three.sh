#!/bin/sh

java -cp ./resources:/home/tpratley/projects/clojure/clojure.jar:/home/tpratley/projects/clojure-contrib/clojure-contrib.jar:./java3d/lib/ext/j3dcore.jar:./java3d/lib/ext/j3dutils.jar:./java3d/lib/ext/vecmath.jar:./md3loader.jar:./lib/tjpext-1.0.0-SNAPSHOT.jar:./nwn-0.7.jar:./src -Djava.library.path=./java3d/lib/amd64 clojure.main ./src/three.clj

