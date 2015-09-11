(ns todomvc.dev
  (:require [todomvc.core :as todomvc]
            [clojure.browser.repl :as repl]))

(defonce conn (repl/connect "http://localhost:9000/repl"))
