(ns org.eliseev.trpo.task1
  (:require [clojure.string :as str])
  (:use clojure.test))

(defn add_char [oldword alp]
  (map
   (fn [newchar] (.concat oldword newchar))
   (filter (fn [char] (not (str/ends-with? oldword char))) alp))
)

(defn solve_for_uniq [alp n]
  (reduce
   (fn [wordlist _]
     (reduce into (map #(add_char % alp) wordlist)))
   alp
   (range (- n 1))))

(defn in? [coll elm]  
  (some #(= elm %) coll))

(defn my_uniq [alp]
  (reduce (fn [new_alp char] (if (in? new_alp char) new_alp (concat new_alp [char]))) [] alp)
)

(defn solve [alp n]
  (solve_for_uniq (my_uniq alp) n))

(println (solve (distinct ["a", "b", "b", "c"]) 3))
(println (solve (distinct []) 3))