(ns todomvc.item
  (:require [clojure.string :as string]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [todomvc.util :refer [hidden pluralize]]))

(def ESCAPE_KEY 27)
(def ENTER_KEY 13)

(defn submit [c]
  (when-let [edit-text (-> c om/get-state :edit-text)]
    (if-not (string/blank? (.trim edit-text))
      (om/call c 'todos/save)
      (om/call c 'todos/delete-temp)))
  false)

(defn edit [c {:keys [title] :as props}]
  (om/call c 'todos/edit)
  (om/update-state! c merge
    {:needs-focus true :title title}))

(defn key-down [c {:keys [title] :as props} e]
  (condp == (.-keyCode e)
    ESCAPE_KEY
      (do
        (om/call c 'todos/cancel-edit)
        (om/update-state! c assoc :edit-text title))
    ENTER_KEY
      (submit c)
    nil))

(defn change [c e]
  (om/update-state! c assoc
    :edit-text (.. e -target -value)))

;; -----------------------------------------------------------------------------
;; Todo Item

(defui TodoItem
  static om/IQuery
  (query [this]
    [:db/id :todo/editing :todo/completed :todo/title :todo/hidden])

  Object
  (componentDidUpdate [this next-props next-state]
    (when (and (:editing next-props)
               (om/get-state this :needs-focus))
      (let [node (om/dom-node this "editField")
            len  (.. node -value -length)]
        (.focus node)
        (.setSelectionRange node len len))
      (om/update-state! this assoc :needs-focus nil)))

  (render [this]
    (let [{:keys [db/id todo/completed todo/editing todo/title] :as props} (om/props this)
          class (cond-> ""
                  completed (str "completed ")
                  editing   (str "editing"))]
      (dom/li #js {:className class :style (hidden (:todo/hidden props))}
        (dom/div #js {:className "view"}
          (dom/input
            #js {:className "toggle"
                 :type      "checkbox"
                 :checked   (and completed "checked")
                 :onChange  (fn [_]
                              (om/call this 'todo/set-state
                                {:db/id id :todo/completed}))})
          (dom/label
            #js {:onDoubleClick (fn [e] (edit this props))}
            title)
          (dom/button
            #js {:className "destroy"
                 :onClick (fn [_] (om/call this 'todo/delete))}))
        (dom/input
          #js {:ref       "editField"
               :className "edit"
               :value     (om/get-state this :edit-text)
               :onBlur    #(submit this)
               :onChange  #(change this %)
               :onKeyDown #(change this %)})))))

(def item (om/create-factory TodoItem))
