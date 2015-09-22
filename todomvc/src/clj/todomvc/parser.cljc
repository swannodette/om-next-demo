(ns todomvc.parser
  (:require [om.next.parser]))

(defmulti read (fn [_ k _] k))

(defmulti mutate (fn [_ k _] k))
