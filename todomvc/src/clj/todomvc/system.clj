(ns todomvc.system
  (:require [com.stuartsierra.component :as component]
            todomvc.server
            [todomvc.datomic :as todomvc]))

(defn dev-system [config-options]
  (let [{:keys [db-uri web-port]} config-options]
    (component/system-map
      :db (todomvc.datomic/new-database db-uri)
      :webserver
      (component/using
        (todomvc.server/dev-server web-port)
        {:datomic-connection  :db}))))

(defn prod-system [config-options]
  (let [{:keys [db-uri]} config-options]
    (component/system-map
      :db (todomvc.datomic/new-database db-uri)
      :webserver
      (component/using
        (todomvc.server/prod-server)
        {:datomic-connection  :db}))))

(comment
  (def sys
    (dev-system
      {:db-uri   "datomic:mem://localhost:4334/todos"
       :web-port 8081}))

  (def sys' (component/start sys))

  (require '[datomic.api :as d])

  (def conn (d/connect "datomic:mem://localhost:4334/todos"))

  ;;  (def conn (-> sys' :db :connection))

  (def db (d/db conn))

  (todomvc/todos db
    [:db/id :todo/created :todo/title :todo/completed])
)
