(ns todomvc.util
  (:require [cognitect.transit :as t])
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

(comment
  (def sel [{:todos/list [:db/id :todo/title :todo/completed :todo/created]}])

  (t/write (t/writer :json) sel)
  )