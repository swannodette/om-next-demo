(ns todomvc.core
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [cljs.pprint :as pprint]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [todomvc.util :as util :refer [hidden pluralize]]
            [todomvc.item :as item]
            [todomvc.parser :as p])
  (:import [goog History]
           [goog.history EventType]))

;; -----------------------------------------------------------------------------
;; Components

(defn main [todos {:keys [todos/list] :as props}]
  (let [checked? (every? :todo/completed list)]
    (dom/section #js {:id "main" :style (hidden (empty? list))}
     (dom/input
       #js {:id       "toggle-all"
            :type     "checkbox"
            :onChange (fn [_]
                        (om/transact! todos
                          `[(todos/toggle-all
                              {:value ~(not checked?)})
                            :todos/list]))
            :checked  checked?})
     (apply dom/ul #js {:id "todo-list"}
       (map item/item list)))))

(defn clear-button [todos completed]
  (when (pos? completed)
    (dom/button
      #js {:id "clear-completed"
           :onClick (fn [_] (om/transact! todos `[(todos/clear)]))}
      (str "Clear completed (" completed ")"))))

(defn footer [todos props active completed]
  (dom/footer #js {:id "footer" :style (hidden (empty? (:todos/list props)))}
    (dom/span #js {:id "todo-count"}
      (dom/strong nil active)
      (str " " (pluralize active "item") " left"))
    (apply dom/ul #js {:id "filters" :className (name (:todos/showing props))}
      (map (fn [[x y]] (dom/li nil (dom/a #js {:href (str "#/" x)} y)))
        [["" "All"] ["active" "Active"] ["completed" "Completed"]]))
    (clear-button todos completed)))

(defui Todos
  static om/IQueryParams
  (params [this]
    {:todo-item (om/get-query item/TodoItem)})

  static om/IQuery
  (query [this]
    '[{:todos/list ?todo-item}])

  Object
  (render [this]
    (let [props (merge (om/props this) {:todos/showing :all})
          {:keys [todos/list]} props
          active (count (remove :todo/completed list))
          completed (- (count list) active)]
      (dom/div nil
        (dom/header #js {:id "header"}
          (dom/h1 nil "todos")
          (dom/input
            #js {:ref "newField"
                 :id "new-todo"
                 :placeholder "What needs to be done?"
                 :onKeyDown #(do %)})
          (main this props)
          (footer this props active completed))))))

(def todos (om/factory Todos))

(def reconciler
  (om/reconciler
    {:state  {}
     :parser (om/parser {:read p/read :mutate p/mutate})
     :send   (util/transit-post "/api")}))

(om/add-root! reconciler Todos (gdom/getElement "todoapp"))
