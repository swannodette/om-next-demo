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
;; Component

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
  (DatomicDatabase.
    db-uri
    (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))
    (first (Util/readAll (io/reader (io/resource "data/initial.edn"))))
    nil))

(comment
  )


