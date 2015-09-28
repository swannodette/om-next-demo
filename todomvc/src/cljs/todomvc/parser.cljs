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

  (p {:state   todomvc.core/app-state
      :indexer (om/get-indexer todomvc.core/reconciler)}
    '[(todo/edit {:db/id 17592186045418})])

  (pprint/pprint @(om/get-indexer todomvc.core/reconciler))

  (om/key->paths (om/get-indexer todomvc.core/reconciler)
    [:todos/by-id 17592186045418])
  )