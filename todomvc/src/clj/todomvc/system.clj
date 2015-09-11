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
  (def s
    (dev-system
      {:db-uri   "datomic:mem://localhost:4334/todos"
       :web-port 8081}))

  (todomvc.datomic/new-database "datomic:mem://localhost:4334/todoms")

  (def s1 (component/start s))

  (require '[datomic.api :as d])

  (def conn (-> s1 :db :connection))
  (def db (d/db conn))

  (todomvc/todos db
    [:todo/created :todo/title])

  (d/q '[:find (pull ?p sel)
         :in $ ?street sel
         :where
         [?a :address/street ?street]
         [?p :person/address ?a]]
    db "Maple Street"
    [:person/first-name :person/last-name])
)
