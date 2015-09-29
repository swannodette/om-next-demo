(ns todomvc.item
  (:require [clojure.string :as string]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [todomvc.util :refer [hidden pluralize]]))

(def ESCAPE_KEY 27)
(def ENTER_KEY 13)

(defn submit [c {:keys [db/id todo/title] :as props}]
  (when-let [edit-text (om/get-state c :edit-text)]
    (om/transact c
      (cond-> `[(todo/cancel-edit) (todos/delete-temp)]
        (not (string/blank? (.trim edit-text)))
        (conj `[(todo/update {:db/id ~id :todo/title ~title})]))))
  false)

(defn edit [c {:keys [db/id todos/title] :as props}]
  (om/transact c
    `[(todo/edit {:db/id ~id}) [:todos/by-id ~id]])
  (om/update-state! c merge {:needs-focus true :edit-text title}))

(defn key-down [c {:keys [todos/title] :as props} e]
  (condp == (.-keyCode e)
    ESCAPE_KEY
      (do
        (om/call c 'todos/cancel-edit)
        (om/update-state! c assoc :edit-text title))
    ENTER_KEY
      (submit c props)
    nil))

(defn change [c e]
  (om/update-state! c assoc
    :edit-text (.. e -target -value)))

;; -----------------------------------------------------------------------------
;; Todo Item

(defn checkbox [c {:keys [:db/id :todo/completed]}]
  (dom/input
    #js {:className "toggle"
         :type "checkbox"
         :checked (and completed "checked")
         :onChange (fn [_]
                     (om/transact c
                       `[(todo/update
                           {:db/id ~id :todo/completed ~(not completed)})
                         [:todos/by-id ~id]]))}))

(defn label [c {:keys [todo/title] :as props}]
  (dom/label
    #js {:onDoubleClick (fn [e] (edit c props))}
    title))

(defn delete-button [c {:keys [db/id]}]
  (dom/button
    #js {:className "destroy"
         :onClick (fn [_] (om/call c 'todo/delete {:db/id id}))}))

(defn edit-field [c props]
  (dom/input
    #js {:ref       "editField"
         :className "edit"
         :value     (om/get-state c :edit-text)
         :onBlur    #(submit c props)
         :onChange  #(change c %)
         :onKeyDown #(change c %)}))

(defui TodoItem
  static om/IQuery
  (query [this]
    [:db/id :todo/editing :todo/completed :todo/title])

  Object
  (componentDidUpdate [this next-props next-state]
    (when (and (:todo/editing next-props)
               (om/get-state this :needs-focus))
      (let [node (om/dom-node this "editField")
            len  (.. node -value -length)]
        (.focus node)
        (.setSelectionRange node len len))
      (om/update-state! this assoc :needs-focus nil)))

  (render [this]
    (println "TodoItem render")
    (let [props (om/props this)
          {:keys [todo/completed todo/editing]} props
          class (cond-> ""
                  completed (str "completed ")
                  editing   (str "editing"))]
      (dom/li #js {:className class}
        (dom/div #js {:className "view"}
          (checkbox this props)
          (label this props)
          (delete-button this props))
        (edit-field this props)))))

(def item (om/create-factory TodoItem))
