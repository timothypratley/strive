#!/bin/sh

java -cp ./resources:/home/tpratley/clojure/clojure.jar:/home/tpratley/clojure-contrib/clojure-contrib.jar:./java3d/lib/ext/j3dcore.jar:./java3d/lib/ext/j3dutils.jar:./java3d/lib/ext/vecmath.jar:./md3loader.jar:./nwn-0.7.jar:./src -Djava.library.path=./java3d/lib/amd64 clojure.main ./src/three.clj

