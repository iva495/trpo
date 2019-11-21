(ns org.eliseev.trpo.task1
  (:require [clojure.string :as str])
  (:use clojure.test))

(load-file "task1.clj")

(deftest a-test
  (testing "test1"
    (is (= (sort (solve ["a" "b" "c"] 3)) (sort ["aba" "cba" "aca" "bca" "bab" "cab" "acb" "bcb" "bac" "cac" "abc" "cbc"])))
    (is (= (sort (solve ["a" "b" "c"] 1)) (sort ["a" "b" "c"])))
    (is (= (sort (solve ["a" "b" "c" "c"] 3)) (sort ["aba" "cba" "aca" "bca" "bab" "cab" "acb" "bcb" "bac" "cac" "abc" "cbc"])))
    (is (= (sort (solve ["a" "b" "c" "c"] 1)) (sort ["a" "b" "c"])))
    (is (= (sort (solve [] 3)) (sort [])))
    ))
(run-tests 'org.eliseev.trpo.task1)