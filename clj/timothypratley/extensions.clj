(ns timothypratley.extensions)

(defmacro while-let
  "while with a binding"
  [[v cnd] & body]
  `(loop[]
     (let [~v ~cnd]
       (when ~v
         ~@body
         (recur)))))
(defmacro while-let-pred
  "while with a binding and predicate"
  [[v cnd] pred & body]
  `(loop[]
     (let [~v ~cnd]
       (when (~pred ~v)
         ~@body
         (recur)))))

