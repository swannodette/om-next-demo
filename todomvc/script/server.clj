(require '[todomvc.core :as todomvc])

(todomvc/dev-start)
(println (str "Started server on port " (:web-port todomvc/dev-config)))

(.addShutdownHook (Runtime/getRuntime)
  (Thread. #(do (todomvc/stop)
                (println "Server stopped"))))
;; lein trampoline run -m clojure.main script/server.clj
