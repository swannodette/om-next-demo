(ns todomvc.parser
  (:require [datomic.api :as d]))

(defmulti readf (fn [env k params] k))

(defmethod readf :default
  [_ k _]
  {:value {:error (str "No handler for read key " k)}})

(defmulti mutatef (fn [env k params] k))

(defmethod mutatef :default
  [_ k _]
  {:value {:error (str "No handler for mutation key " k)}})

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

(defmethod readf :todo/by-id
  [{:keys [conn selector]} _ {:keys [db/id]}]
  (let [db (d/db conn)]
    {:value (d/pull db selector id)}))

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

(defmethod mutatef 'todo/update
  [{:keys [conn]} k {:keys [db/id todo/completed todo/title]}]
  (d/transact conn
    [(merge {:db/id id}
       (when (or (true? completed) (false? completed))
         {:todo/completed completed})
       (when title
         {:todo/title title}))])
  {:value [id]})

(defmethod mutatef 'todo/delete
  [{:keys [conn]} k {:keys [db/id]}]
  (d/transact conn [[:db.fn/retractEntity id]])
  {:value [:todos/list]})

(comment
  (require '[todomvc.core :as cc])

  (cc/dev-start)

  (def conn (:connection (:db @cc/servlet-system)))

  (require '[om.next.server :refer [parser]])

  (def p (parser {:read readf :mutate mutatef}))

  (p {:conn conn} [{:todos/list [:db/id :todo/title :todo/completed]}])

  (p {:conn conn} '[(todo/update {:db/id 17592186045424 :todo/completed false})])

  (p {:conn conn} '[(todos/create {:todo/title "Finish Om"})])

  (p {:conn conn} '[(todo/delete {:db/id 17592186045418})])

  ;; this fails
  (d/transact conn
    [{:db/id          #db/id[:db.part/user]
      :todo/title     nil
      :todo/completed false
      :todo/created   (java.util.Date.)}])

  ;; can pull
  (let [db (d/db conn)]
    (d/pull db [:db/id :todo/title] 17592186045418))

  (let [id 17592186045418]
    (p {:conn conn}
     `[(todo/update {:db/id 17592186045424 :todo/completed false})
       ({:todo/by-id [:todo/title :todo/completed]} {:db/id ~id})]))
  )