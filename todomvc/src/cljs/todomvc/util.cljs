(ns todomvc.util
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan put!]]
            [cognitect.transit :as t])
  (:import [goog.net XhrIo]))

(defn hidden [is-hidden]
  (if is-hidden
    #js {:display "none"}
    #js {}))

(defn pluralize [n word]
  (if (== n 1)
    word
    (str word "s")))

(defn transit-post [url]
  (fn [{:keys [remote]} cb]
    (.send XhrIo url
      (fn [e]
        (this-as this
          (cb (t/read (t/reader :json) (.getResponseText this)))))
      "POST" (t/write (t/writer :json) remote)
      #js {"Content-Type" "application/transit+json"})))

(defn transit-post-chan [url edn]
  (let [c (chan)]
    ((transit-post url) edn (fn [res] (put! c res)))
    c))

(comment
  (def sel [{:todos/list [:db/id :todo/title :todo/completed :todo/created]}])

  (t/write (t/writer :json) sel)

  (go (println (<! (transit-post-chan "/api" sel))))
  )