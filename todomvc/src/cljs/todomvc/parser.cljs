(ns todomvc.parser
  (:require [om.next :as om]))

;; =============================================================================
;; Reads

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state]} k _]
  (let [st @state] ;; CACHING!!!
    (if (contains? st k)
      {:value (get st k)}
      {:quote true})))

(defmethod read :todos/list
  [{:keys [state]} k _]
  (let [st @state]
    (if-let [todos (into [] (map (get st :todos/by-id)) (get st k))]
      (if-let [ref (:todos/editing st)]
        ;; TRANSPARENTLY MERGE LOCAL STATE
        {:value (update-in todos ref assoc :todo/editing true)}
        {:value todos})
      {:quote true})))

;; =============================================================================
;; Mutations

(defmulti mutate om/dispatch)

(defmethod mutate :default
  [_ _ _] {:quote true})

(defmethod mutate 'todo/update
  [{:keys [state ref]} _ new-props]
  {:quote true
   :action ;; OPTIMISTIC UPDATE
   (fn []
     (swap! state update-in ref merge new-props))})

(defmethod mutate 'todo/edit
  [{:keys [state]} _ {:keys [db/id]}]
  {:action
   (fn []
     (swap! state assoc :todos/editing [:todos/by-id id]))})

(defmethod mutate 'todo/cancel-edit
  [{:keys [state]} _ _]
  {:action
   (fn []
     (swap! state dissoc :todos/editing))})

(defmethod mutate 'todos/create-temp
  [{:keys [state]} _ new-todo]
  {:value [:todos/list]
   :action (fn [] (swap! state assoc :todos/temp new-todo))})

(defmethod mutate 'todos/delete-temp
  [{:keys [state]} _ _]
  {:value [:todos/list]
   :action (fn [] (swap! state dissoc :todos/temp))})

(comment
  (require '[cljs.pprint :as pp])

  (pp/pprint
    @(om/get-indexer todomvc.core/reconciler))
  )