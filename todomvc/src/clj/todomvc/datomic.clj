(ns todomvc.datomic
  (:require [datomic.api :as d]
            [com.stuartsierra.component :as component]
            [clojure.java.io :as io])
  (:import datomic.Util))

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
