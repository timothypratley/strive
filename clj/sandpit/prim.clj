(ns prim
  (:use clojure.test))

(deftest test-embedded-constants
  (is (eval `(make-array ~Boolean/TYPE 2)) "Boolean")
  (is (eval `(make-array ~Byte/TYPE 2)) "Byte")
  (is (eval `(make-array ~Character/TYPE 2)) "Character")
  (is (eval `(make-array ~Double/TYPE 2)) "Double")
  (is (eval `(make-array ~Float/TYPE 2)) "Float")
  (is (eval `(make-array ~Integer/TYPE 2)) "Integer")
  (is (eval `(make-array ~Long/TYPE 2)) "Long")
  (is (eval `(make-array ~Short/TYPE 2))"Short"))

(run-tests)

