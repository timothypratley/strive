#!/bin/sh

java -cp ./three-standalone.jar:./j3d/lib/ext/j3dcore.jar:./j3d/lib/ext/j3dutils.jar:./j3d/lib/ext/vecmath.jar:./md3loader.jar:./src -Djava.library.path=./j3d/lib/amd64 clojure.main ./src/three.clj

