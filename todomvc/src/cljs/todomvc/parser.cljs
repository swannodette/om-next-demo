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
  [{:keys [state indexer]} k _]
  (let [st @state]
    (if-let [list (get st k)]
      (if-let [ref (:todos/editing st)]
        ;; TRANSPARENTLY MERGE LOCAL STATE
        {:value (update-in list
                  (om/subpath k (om/key->any indexer ref))
                  assoc :todo/editing true)}
        {:value list})
      {:quote true})))

;; =============================================================================
;; Mutations

(defmulti mutate om/dispatch)

(defmethod mutate :default
  [_ _ _] {:quote true})

(defmethod mutate 'todo/update
  [{:keys [state indexer]} _ {:keys [db/id] :as new-props}]
  {:quote true
   :action ;; OPTIMISTIC UPDATE
   (fn []
     (let [ref [:todos/by-id id]]
       (swap! state update-in (om/key->any indexer ref)
         merge new-props)))})

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