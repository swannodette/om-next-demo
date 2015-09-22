(ns todomvc.parser
  (:require [datomic.api :as d]))

(defmulti readf (fn [env k params] k))

(defmulti mutatef (fn [env k params] k))

(defn todos
  ([db] (todos db '[*]))
  ([db selector]
   (println selector)
   (d/q '[:find [(pull ?eid selector) ...]
          :in $ selector
          :where
          [?eid :todo/created]]
     db selector)))

(defmethod readf :todos/list
  [{:keys [conn selector]} _ _]
  {:value (todos (d/db conn) selector)})

(comment
  (require '[todomvc.core :as cc])

  (def conn (:connection (:db @cc/servlet-system)))

  (require '[om.next.server :refer [parser]])

  (def p (parser {:read readf}))

  (p {:conn conn} [{:todos/list [:db/id :todo/title]}])
  )