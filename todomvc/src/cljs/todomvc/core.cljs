(ns todomvc.core
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [secretary.macros :refer [defroute]])
  (:require [goog.events :as events]
            [goog.dom :as gdom]
            [cljs.pprint :as pprint]
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
      (map item/item list))))

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

(def todos (om/factory Todos))

(def reconciler
  (om/reconciler
    {:state  {}
     :parser (om/parser {:read p/read :mutate p/mutate})
     :send   (util/transit-post "/api")}))

(om/add-root! reconciler Todos (gdom/getElement "todoapp"))

(comment
  (om/get-query Todos)

  (js/setTimeout #(pprint/pprint @app-state) 5000)

  (go (pprint/pprint
        (<! (util/transit-post-chan "/api" (om/get-query Todos)))))

  (go (pprint/pprint
        (<! (util/transit-post-chan "/api"
              `[({:todos/list ~(om/get-query item/TodoItem)}
                  {:as-of #inst "2015-09-29T14:27:02.270-00:00"})]))))

  (go (reset! app-state
        (<! (util/transit-post-chan "/api"
              `[({:todos/list ~(om/get-query item/TodoItem)}
                  {:as-of #inst "2015-09-29T14:27:02.270-00:00"})]))))

  (def idxr (om/get-indexer reconciler))

  (def p (om/parser {:read p/read :mutate p/mutate}))

  (p {:state (get-in reconciler [:config :state])}
     `[(todo/update
         {:db/id 17592186045418, :todo/title "Get Apple Juice"})
       [:todos/by-id 17592186045418]])

  ;; The Indexer!

  (pprint/pprint @idxr)

  (let [c (first (om/key->components idxr
                   [:todos/by-id 17592186045418]))]
    (om/props c))
  )