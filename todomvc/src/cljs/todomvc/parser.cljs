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
      {:remote true})))

;; work around for a bizarre :simple/:advanced bug
;; circle back - David
(defn join [st editing ref]
  (cond-> (get-in st ref)
    (= editing ref) (assoc :todo/editing true)))

(defn get-todos [{:keys [todos/editing] :as st}]
  (into [] (map #(join st editing %)) (get st :todos/list)))

(defmethod read :todos/list
  [{:keys [state]} k _]
  (let [st @state]
    (if (contains? st k)
      {:value (get-todos st)}
      {:remote true})))

;; =============================================================================
;; Mutations

(defmulti mutate om/dispatch)

(defmethod mutate :default
  [_ _ _] {:remote true})

(defmethod mutate 'todos/clear
  [{:keys [state]} _ _]
  {:action
   (fn []
     (let [st @state]
       (swap! state update-in [:todos/list]
         (fn [list]
           (into []
             (remove #(get-in st (conj % :todo/completed))))))))})

(defmethod mutate 'todos/toggle-all
  [{:keys [state]} _ {:keys [value]}]
  {:action
   (fn []
     (letfn [(step [state' ref]
               (update-in state' ref assoc
                 :todo/completed value))]
       (swap! state
         #(reduce step % (:todos/list %)))))})

(defmethod mutate 'todo/update
  [{:keys [state ref]} _ new-props]
  {:remote true
   :action ;; OPTIMISTIC UPDATE
   (fn []
     (swap! state update-in ref merge new-props))
   })

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
