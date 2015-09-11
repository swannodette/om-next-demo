(ns todomvc.core
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [secretary.macros :refer [defroute]])
  (:require [goog.events :as events]
            [secretary.core :as secretary]
            [cljs.core.async :refer [put! <! chan]]
            [om.next :as om]
            [om.dom :as dom]
            [clojure.string :as string]
            [todomvc.item :as item])
  (:import [goog History]
           [goog.history EventType]))
