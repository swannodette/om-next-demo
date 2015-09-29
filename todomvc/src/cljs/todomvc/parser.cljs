(ns todomvc.parser
  (:require [om.next :as om]))

;; =============================================================================
;; Reads

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state]} k _]
  (let [st @state]
    (if (contains? st k)
      {:value (get st k)}
      {:quote true})))

(defmethod read :todos/list
  [{:keys [state indexer]} k _]
  (let [st @state]
    (if-let [list (get st k)]
      (if-let [ref (:todos/editing st)]
        {:value (update-in list
                  (om/subpath k (first (om/key->paths indexer ref)))
                  assoc :todo/editing true)}
        {:value list})
      {:quote true})))

;; =============================================================================
;; Mutations

(defmulti mutate om/dispatch)

(defmethod mutate :default
  [_ _ _] {:quote true})

(defmethod mutate 'todo/edit
  [{:keys [state indexer]} _ {:keys [db/id]}]
  {:action
   (fn []
     (swap! state assoc :todos/editing [:todos/by-id id]))})

(defmethod mutate 'todo/cancel-edit
  [{:keys [state indexer]} _ {:keys [db/id]}]
  {:action
   (fn []
     (swap! state dissoc :todos/editing))})

(defmethod mutate 'todos/create-temp
  [{:keys [state]} _ new-todo]
  {:value [:todos/list]
   :action (fn [] (swap! state assoc :todos/temp new-todo))})

(defmethod mutate 'todos/delete-temp
  [{:keys [state]} _ {:keys [db/id]}]
  {:value [:todos/list]
   :action (fn [] (swap! state dissoc :todos/temp))})

(comment
  (require '[cljs.pprint :as pprint])

  (def p (om/parser {:read read :mutate mutate}))

  (def idxr (om/get-indexer todomvc.core/reconciler))

  todomvc.core/app-state

  (first (om/key->components idxr [:todos/by-id 17592186045418]))

  (p {:state todomvc.core/app-state}
    '[:todos/list])

  (p {:state todomvc.core/app-state}
    '[{:todos/list [:db/id]}])

  (let [ref [:todos/by-id 17592186045418]]
    (om/transact (first (om/key->components idxr ref))
      `[(todo/edit {:db/id 17592186045418}) ~ref]))

  (pprint/pprint @(om/get-indexer todomvc.core/reconciler))

  (first
    (om/key->paths (om/get-indexer todomvc.core/reconciler)
      [:todos/by-id 17592186045418]))
  )