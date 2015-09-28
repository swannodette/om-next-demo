(ns todomvc.parser
  (:require [om.next :as om]))

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state]} k _]
  (case k
    :todos/temp (:todos/temp state)
    {:quote true}))

(defmulti mutate om/dispatch)

(defmethod mutate :default
  [_ _ _] {:quote true})

(defmethod mutate 'todo/edit
  [{:keys [state indexer]} _ {:keys [db/id]}]
  {:action
   (fn [] (swap! state assoc-in
            (first (om/key->paths indexer id)) :editing true))})

(defmethod mutate 'todo/cancel-edit
  [{:keys [state indexer]} _ {:keys [db/id]}]
  {:action
   (fn [] (swap! state assoc-in
            (first (om/key->paths indexer id)) :editing false))})

(defmethod mutate 'todos/create-temp
  [{:keys [state]} _ new-todo]
  {:value [:todos/list]
   :action (fn [] (swap! state assoc :todos/temp new-todo))})

(defmethod mutate 'todos/delete-temp
  [{:keys [state]} _ {:keys [db/id]}]
  {:value [:todos/list]
   :action (fn [] (swap! state dissoc :todos/temp))})
