(ns todomvc.datomic
  (:require [datomic.api :as d]
            [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [cognitect.transit :as t])
  (:import datomic.Util))

;; =============================================================================
;; Queries

(defn contacts
  ([db] (contacts db '[*]))
  ([db selector]
   (mapv first
     (d/q '[:find (pull ?eid selector)
            :in $ selector
            :where
            [?eid :person/first-name]] ;; talk about how we can make it do first OR last name
       db selector))))

(defn get-contact
  ([db id] (get-contact db id '[*]))
  ([db id selector]
   (d/pull db selector id)))

;; =============================================================================
;; CRUD

;; TODO: rewrite this into something generic, client will send transaction

(defn create-contact [conn data]
  (let [tempid (d/tempid :db.part/user)
        r @(d/transact conn [(assoc data :db/id tempid)])]
    (assoc data :db/id (str (d/resolve-tempid (:db-after r) (:tempids r) tempid)))))


(defn update-contact [conn data]
  @(d/transact conn [(assoc data :db/id (edn/read-string (:db/id data)))])
  true)


(defn delete-contact [conn id]
  @(d/transact conn [[:db.fn/retractEntity (edn/read-string id)]])
  true)


;; PHONE
(defn create-phone [conn data]
  (let [tempid (d/tempid :db.part/user)
        r @(d/transact conn
                       [(assoc
                            data
                          :db/id
                          tempid
                          :person/_telephone
                          (edn/read-string (:person/_telephone data)))])]
    (assoc data :db/id (str (d/resolve-tempid (:db-after r) (:tempids r) tempid)))))


(defn update-phone [conn data]
  @(d/transact conn [(assoc data :db/id (edn/read-string (:db/id data)))])
  true)


(defn delete-phone [conn id]
  @(d/transact conn [[:db.fn/retractEntity (edn/read-string id)]])
  true)

(defrecord DatomicDatabase [uri schema initial-data connection]
  component/Lifecycle
  (start [component]
    (d/create-database uri)
    (let [c (d/connect uri)]
      @(d/transact c schema)
      @(d/transact c initial-data)
      (assoc component :connection c)))
  (stop [component]))

(defn new-database [db-uri]
  (DatomicDatabase. db-uri
                    (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))
                    (first (Util/readAll (io/reader (io/resource "data/initial.edn"))))
                    nil))

;; =============================================================================
;; Query testing

(comment
  (create-contact (:connection (:db @contacts.core/servlet-system))
                  {:person/first-name "person" :person/last-name "withphone"})

  (delete-contact (:connection (:db @contacts.core/servlet-system))
                  "17592186045423")

  (update-contact (:connection (:db @contacts.core/servlet-system))
                  {:db/id "17592186045429" :person/first-name "Foooo"})


  (create-phone (:connection (:db @contacts.core/servlet-system))
                  {:person/_telephone "17592186045438"
                   :telephone/number "123-456-7890"})

  (d/touch (d/entity (d/db (:connection (:db @contacts.core/servlet-system)))
             17592186045438))

  (update-phone (:connection (:db @contacts.core/servlet-system))
                {:db/id "17592186045440"
                 :telephone/number "000-456-7890"})

  (delete-phone (:connection (:db @contacts.core/servlet-system))
                "17592186045440")

  )


