(ns todomvc.parser
  (:require [datomic.api :as d]))

(defmulti readf (fn [env k params] k))

(defmulti mutatef (fn [env k params] k))

(defn todos
  ([db] (todos db '[*]))
  ([db selector]
   (mapv first
     (d/q '[:find (pull ?eid selector)
            :in $ selector
            :where
            [?eid :todo/created]]
       db selector))))

(defmethod readf :todos/list
  [{:keys [conn]} _ selector]
  (todos conn selector))

(comment
  (require '[om.next.server :refer [parser]])

  (def p (parser {:read readf}))


  )