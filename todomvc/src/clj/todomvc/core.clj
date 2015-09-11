(ns todomvc.core
  (:require [com.stuartsierra.component :as component]
            [contacts.system :as system]))

(def servlet-system (atom nil))

;; =============================================================================
;; Development

(defn dev-start []
  (let [sys  (system/dev-system
               {:db-uri   "datomic:mem://localhost:4334/contacts"
                :web-port 8081})
        sys' (component/start sys)]
    (reset! servlet-system sys')
    sys'))

;; =============================================================================
;; Production

(defn service [req]
  ((:handler (:webserver @servlet-system)) req))

(defn start []
  (let [s (system/prod-system
            {:db-uri   "datomic:mem://localhost:4334/contacts"})]
    (let [started-system (component/start s)]
      (reset! servlet-system started-system))))

(defn stop [])
