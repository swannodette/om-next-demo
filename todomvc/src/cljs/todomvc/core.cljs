(ns todomvc.core
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [secretary.macros :refer [defroute]])
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [cljs.core.async :refer [put! <! chan]]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [clojure.string :as string]
            [todomvc.item :as item])
  (:import [goog History]
           [goog.history EventType]))

;; -----------------------------------------------------------------------------
;; Utilities

(defn hidden [is-hidden]
  (if is-hidden
    #js {:display "none"}
    #js {}))

(defn pluralize [n word]
  (if (== n 1)
    word
    (str word "s")))

(defn toggle-all [e c]
  )

;; -----------------------------------------------------------------------------
;; Components

(defn main [{:keys [todos showing editing] :as app}]
  (dom/section #js {:id "main" :style (hidden (empty? todos))}
    (dom/input
      #js {:id "toggle-all"
           :type "checkbox"
           :onChange #(toggle-all % app)
           :checked (every? :completed todos)})
    (apply dom/ul #js {:id "todo-list"} todos)))

(defn clear-button [completed]
  (when (pos? completed)
    (dom/button
      #js {:id "clear-completed"
           :onClick #(do %)}
      (str "Clear completed (" completed ")"))))

(defn footer [app count completed]
  (let [sel (-> (zipmap [:all :active :completed] (repeat ""))
              (assoc (:showing app) "selected"))]
    (dom/footer #js {:id "footer" :style (hidden (empty? (:todos app)))}
      (dom/span #js {:id "todo-count"}
        (dom/strong nil count)
        (str " " (pluralize count "item") " left"))
      (dom/ul #js {:id "filters"}
        (dom/li nil
          (dom/a #js {:href "#/" :className (sel :all)}
            "All"))
        (dom/li nil
          (dom/a #js {:href "#/active" :className (sel :active)}
            "Active"))
        (dom/li nil
          (dom/a #js {:href "#/completed" :className (sel :completed)}
            "Completed")))
      (clear-button completed))))