(ns todomvc.core
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [secretary.macros :refer [defroute]])
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [secretary.core :as secretary]
            [cljs.core.async :refer [put! <! chan]]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [todomvc.util :as util :refer [hidden pluralize]]
            [todomvc.item :as item]
            [todomvc.parser :as p])
  (:import [goog History]
           [goog.history EventType]))

;; -----------------------------------------------------------------------------
;; Components

(defn main [{:keys [todos/list] :as app}]
  (dom/section #js {:id "main" :style (hidden (empty? list))}
    (dom/input
      #js {:id       "toggle-all"
           :type     "checkbox"
           :onChange identity
           :checked  (every? :todo/completed list)})
    (apply dom/ul #js {:id "todo-list"}
      (map-indexed
        (fn [i {:keys [db/id] :as props}]
          (item/item
            (assoc props :react-key id :om-index i)))
        list))))

(defn clear-button [completed]
  (when (pos? completed)
    (dom/button
      #js {:id "clear-completed"
           :onClick #(do %)}
      (str "Clear completed (" completed ")"))))

(defn footer [app count completed]
  (dom/footer #js {:id "footer" :style (hidden (empty? (:todos/list app)))}
    (dom/span #js {:id "todo-count"}
      (dom/strong nil count)
      (str " " (pluralize count "item") " left"))
    (apply dom/ul #js {:id "filters" :className (name (:todos/showing app))}
      (map (fn [[x y]] (dom/li nil (dom/a #js {:href (str "#/" x)} y)))
        [["" "All"] ["active" "Active"] ["completed" "Completed"]]))
    (clear-button completed)))

(defui Todos
  static om/IQueryParams
  (params [this]
    {:todo-item (om/get-query item/TodoItem)})

  static om/IQuery
  (query [this]
    '[{:todos/list ?todo-item}])

  Object
  (render [this]
    (let [props (merge (om/props this) {:todos/showing :all})]
      (dom/div nil
        (dom/header #js {:id "header"}
          (dom/h1 nil "todos")
          (dom/input
            #js {:ref "newField"
                 :id "new-todo"
                 :placeholder "What needs to be done?"
                 :onKeyDown #(do %)})
          (main props)
          (footer props 0 0))))))

(def todos (om/create-factory Todos))

(def app-state (atom {}))

(def reconciler
  (om/reconciler
    {:state   app-state
     :parser  (om/parser {:read p/read :mutate p/mutate})
     :send    (util/transit-post "/api")
     :ui->ref (fn [c]
                (if-let [id (-> c om/props :db/id)]
                  [:todos/by-id id]
                  c))}))

(om/add-root! reconciler (gdom/getElement "todoapp") Todos)

(comment
  (require '[cljs.pprint :as pprint])

  (om/get-query Todos)

  (go (pprint/pprint
        (<! (util/transit-post-chan "/api" (om/get-query Todos)))))

  (go (pprint/pprint
        (<! (util/transit-post-chan "/api"
              `[({:todos/list ~(om/get-query item/TodoItem)}
                  {:as-of #inst "2015-09-29T06:08:59.022-00:00"})]))))

  (go (reset! app-state
        (<! (util/transit-post-chan "/api"
              `[({:todos/list ~(om/get-query item/TodoItem)}
                  #_{:as-of #inst "2015-09-29T06:08:59.022-00:00"})]))))
  )