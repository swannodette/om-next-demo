(ns todomvc.util
  (:require [cljs.core.async :refer [chan]]
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

(defn transit-post [url edn]
  (let [c (chan)]
    (.send XhrIo url "POST"
      (fn [cb res]
        (put! c (t/read (t/reader :json) res)))
      (t/write (t/writer :json) edn)
      #js {"Content-Type" "application/transit+json"})
    c))
