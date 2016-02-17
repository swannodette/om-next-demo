(ns todomvc.core
  (:require [com.stuartsierra.component :as component]
            [todomvc.system :as system]))

(def servlet-system (atom nil))

;; =============================================================================
;; Development

(def dev-config
  {:db-uri   "datomic:mem://localhost:4334/todos"
   :web-port 8081})

(defn dev-start []
  (let [sys  (system/dev-system dev-config)
        sys' (component/start sys)]
    (reset! servlet-system sys')
    sys'))

;; =============================================================================
;; Production

(defn service [req]
  ((:handler (:webserver @servlet-system)) req))

(defn start []
  (let [s (system/prod-system
            {:db-uri   "datomic:mem://localhost:4334/todos"})]
    (let [started-system (component/start s)]
      (reset! servlet-system started-system))))

(defn stop []
  (swap! servlet-system component/stop))
