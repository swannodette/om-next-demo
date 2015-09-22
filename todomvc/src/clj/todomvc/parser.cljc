(ns todomvc.parser
  (:require [#?(:clj om.next.server :cljs om.next)
             :refer [parser]]))

(defmulti read (fn [_ k _] k))

(defmulti mutate (fn [_ k _] k))
