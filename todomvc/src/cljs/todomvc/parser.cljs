(ns todomvc.parser
  (:require [om.next :as om]
            [om.next.parser :as p]))

(defmulti read p/dispatch)

(defmethod read :default
  [{:keys [state]} k _]
  (case k
    :todos/temp (:todos/temp state)
    {:quote true}))

(defmulti mutate p/dispatch)

(defmethod mutate :default
  [_ _ _] {:quote true})

(defmethod mutate :edit
  [{:keys [state indexer]} _ {:keys [db/id]}]
  {:action
   (fn [] (swap! state assoc-in
            (first (om/ref->paths indexer id)) :editing true))})

(defmethod mutate 'todos/create-temp
  [{:keys [state]} _ new-todo]
  {:value [:todos/list]
   :action (fn [] (swap! state assoc :todos/temp new-todo))})

(defmethod mutate 'todos/delete-temp
  [{:keys [state]} _ {:keys [db/id]}]
  {:value [:todos/list]
   :action (fn [] (swap! state dissoc :todos/temp))})
