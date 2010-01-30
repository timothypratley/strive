
(let [limit (.availableProcessors (Runtime/getRuntime))
      sem (java.util.concurrent.Semaphore. limit)]
  (defn submit-future-call
    "Takes a function of no args and yields a future object that will
    invoke the function in another thread, and will cache the result and
    return it on all subsequent calls to deref/@. If the computation has
    not yet finished, calls to deref/@ will block. 
    If n futures have already been submitted, then submit-future blocks
    until the completion of another future, where n is the number of
    available processors."  
    [#^Callable task]
    ; take a slot (or block until a slot is free)
    (.acquire sem)
    (try
      ; create a future that will free a slot on completion
      (future (try (task) (finally (.release sem))))
      (catch java.util.concurrent.RejectedExecutionException e
        ; no task was actually submitted
        (.release sem)
        (throw e)))))

(defmacro submit-future
  "Takes a body of expressions and yields a future object that will
  invoke the body in another thread, and will cache the result and
  return it on all subsequent calls to deref/@. If the computation has
  not yet finished, calls to deref/@ will block.
  If n futures have already been submitted, then submit-future blocks
  until the completion of another future, where n is the number of
  available processors."  
  [& body] `(submit-future-call (fn [] ~@body)))

#_(example
    user=> (submit-future (reduce + (range 100000000)))
    #<core$future_call$reify__5782@6c69d02b: :pending>
    user=> (submit-future (reduce + (range 100000000)))
    #<core$future_call$reify__5782@38827968: :pending>
    user=> (submit-future (reduce + (range 100000000)))
    ;; blocks at this point for a 2 processor PC until the previous
    ;; two futures complete
    #<core$future_call$reify__5782@214c4ac9: :pending>)

