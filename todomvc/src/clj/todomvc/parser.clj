(ns todomvc.parser
  (:refer-clojure :exclude [read])
  (:require [datomic.api :as d]))

;; =============================================================================
;; Reads

(defmulti readf (fn [env k params] k))

(defmethod readf :default
  [_ k _]
  {:value {:error (str "No handler for read key " k)}})

(defn todos
  ([db]
   (todos db nil))
  ([db selector]
   (todos db selector nil))
  ([db selector {:keys [filter as-of]}]
   (let [db (cond-> db
              as-of (d/as-of as-of))
         q  (cond->
              '[:find [(pull ?eid selector) ...]
                :in $ selector
                :where
                [?eid :todo/created]]
              (= :completed filter) (conj '[?eid :todo/completed true])
              (= :active filter)    (conj '[?eid :todo/completed false]))]
     (d/q q db (or selector '[*])))))

(defmethod readf :todos/by-id
  [{:keys [conn selector]} _ {:keys [id]}]
  {:value (d/pull @(d/sync conn) (or selector '[*]) id)})

(defmethod readf :todos/list
  [{:keys [conn selector]} _ params]
  {:value (todos (d/db conn) selector params)})

;; =============================================================================
;; Mutations

(defmulti mutatef (fn [env k params] k))

(defmethod mutatef :default
  [_ k _]
  {:value {:error (str "No handler for mutation key " k)}})

(defmethod mutatef 'todos/create
  [{:keys [conn]} k {:keys [:todo/title]}]
  {:value [:todos/list]
   :action
   (fn []
     @(d/transact conn
        [{:db/id          #db/id[:db.part/user]
          :todo/title     title
          :todo/completed false
          :todo/created   (java.util.Date.)}]))})

(defmethod mutatef 'todo/update
  [{:keys [conn]} k {:keys [db/id todo/completed todo/title]}]
  {:value [[:todos/by-id id]]
   :action
   (fn []
     @(d/transact conn
        [(merge {:db/id id}
           (when (or (true? completed) (false? completed))
             {:todo/completed completed})
           (when title
             {:todo/title title}))]))})

(defmethod mutatef 'todo/delete
  [{:keys [conn]} k {:keys [db/id]}]
  {:value [:todos/list]
   :action
   (fn []
     @(d/transact conn [[:db.fn/retractEntity id]]))})

(comment
  (require '[todomvc.core :as cc])

  (cc/dev-start)

  (def conn (:connection (:db @cc/servlet-system)))

  (require '[om.next.server :refer [parser]])

  (def p (parser {:read readf :mutate mutatef}))

  (p {:conn conn} [{:todos/list [:db/id :todo/title :todo/completed]}])

  (p {:conn conn} '[(todos/create {:todo/title "Finish Om"})])

  (p {:conn conn} '[(todo/delete {:db/id 17592186045418})])

  (let [id 17592186045418]
    (p {:conn conn}
     `[(todo/update {:db/id ~id :todo/completed true})
       [:todos/by-id ~id]]))

  )