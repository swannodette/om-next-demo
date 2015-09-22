(ns todomvc.parser
  (:require [datomic.api :as d]))

(defmulti readf (fn [env k params] k))

(defmulti mutatef (fn [env k params] k))

(defn todos
  ([db]
   (todos db '[*]))
  ([db selector]
   (todos db selector nil))
  ([db selector filter]
   (let [q (cond->
             '[:find [(pull ?eid selector) ...]
               :in $ selector
               :where
               [?eid :todo/created]]
             (= :completed filter) (conj '[?eid :todo/completed true])
             (= :active filter)    (conj '[?eid :todo/completed false]))]
     (d/q q db selector))))

(defmethod readf :todos/list
  [{:keys [conn selector]} _ _]
  {:value (todos (d/db conn) selector)})

(defmethod mutatef 'todos/create
  [{:keys [conn]} k {:keys [:todo/title]}]
  (d/transact conn
    [{:db/id          #db/id[:db.part/user]
      :todo/title     title
      :todo/completed false
      :todo/created   (java.util.Date.)}])
  {:value [:todos/list]})

(defmethod mutatef 'todos/set-state
  [{:keys [conn]} k {:keys [db/id todo/completed]}]
  (d/transact conn [{:db/id id :todo/completed completed}])
  {:value [id]})

(defmethod mutatef 'todos/change-title
  [{:keys [conn]} k {:keys [db/id todo/title]}]
  (d/transact conn [{:db/id id :todo/title title}])
  {:value [id]})

(defmethod mutatef 'todos/delete
  [{:keys [conn]} k {:keys [db/id]}]
  (d/transact conn [[:db.fn/retractEntity id]])
  {:value [:todos/list]})

(comment
  (require '[todomvc.core :as cc])

  (cc/dev-start)

  (def conn (:connection (:db @cc/servlet-system)))

  (require '[om.next.server :refer [parser]])

  (def p (parser {:read readf :mutate mutatef}))

  (p {:conn conn} [{:todos/list [:db/id :todo/title]}])

  (p {:conn conn} '[(todos/create {:todo/title "Finish Om"})])

  (p {:conn conn} '[(todos/delete {:db/id 17592186045418})])

  ;; this fails
  (d/transact conn
    [{:db/id          #db/id[:db.part/user]
      :todo/title     nil
      :todo/completed false
      :todo/created   (java.util.Date.)}])
  )