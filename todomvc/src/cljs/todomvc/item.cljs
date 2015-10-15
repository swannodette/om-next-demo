(ns todomvc.item
  (:require [clojure.string :as string]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [todomvc.util :refer [hidden pluralize]]))

(def ESCAPE_KEY 27)
(def ENTER_KEY 13)

(defn submit [c {:keys [db/id todo/title] :as props} e]
  (let [edit-text (string/trim (or (om/get-state c :edit-text) ""))]
    (when-not (= edit-text title)
      (om/transact! c
       (cond-> '[(todo/cancel-edit)]
         (= :temp id)
         (conj '(todos/delete-temp))

         (and (not (string/blank? edit-text))
           (not= edit-text title))
         (into
           `[(todo/update {:db/id ~id :todo/title ~edit-text})
             [:todos/by-id ~id]]))))
    (doto e (.preventDefault) (.stopPropagation))))

(defn edit [c {:keys [db/id todo/title] :as props}]
  (om/transact! c `[(todo/edit {:db/id ~id})])
  (om/update-state! c merge {:needs-focus true :edit-text title}))

(defn key-down [c {:keys [todo/title] :as props} e]
  (condp == (.-keyCode e)
    ESCAPE_KEY
      (do
        (om/transact! c '[(todo/cancel-edit)])
        (om/update-state! c assoc :edit-text title)
        (doto e (.preventDefault) (.stopPropagation)))
    ENTER_KEY
      (submit c props e)
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
                     (om/transact! c
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
         :onClick (fn [_] (om/transact! c `[(todo/delete {:db/id ~id})]))}))

(defn edit-field [c props]
  (dom/input
    #js {:ref       "editField"
         :className "edit"
         :value     (om/get-state c :edit-text)
         :onBlur    (fn [e] (submit c props e))
         :onChange  (fn [e] (change c e))
         :onKeyDown (fn [e] (key-down c props e))}))

(defui TodoItem
  static om/Ident
  (ident [this {:keys [db/id]}]
    [:todos/by-id id])

  static om/IQuery
  (query [this]
    [:db/id :todo/editing :todo/completed :todo/title])

  Object
  (componentDidUpdate [this prev-props prev-state]
    (when (and (:todo/editing (om/props this))
               (om/get-state this :needs-focus))
      (let [node (om/dom-node this "editField")
            len  (.. node -value -length)]
        (.focus node)
        (.setSelectionRange node len len))
      (om/update-state! this assoc :needs-focus nil)))

  (render [this]
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

(def item (om/factory TodoItem {:keyfn :db/id}))
