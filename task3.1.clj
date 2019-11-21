(ns org.eliseev.trpo.task3.1
  (:use clojure.test))

(def dh 0.01)

(defn trap_sq [b1 b2 h] (* 0.5 h (+ b1 b2)))

(defn intg_n [f n]
  (nth (map first (iterate (fn [[sum i]]
    [(+ sum (trap_sq (f (* i dh)) (f (* (+ 1 i) dh)) dh)) (inc i)]
  ) [0 0] )) n))

(defn intg[f]
  (fn [x] 
		  (def n (quot x dh))
    (+ 
     (intg_n f n)
     (trap_sq (f (* n dh)) (f x) (- x (* n dh))))
  ))

(defn fnc [x] (Math/log (+ 1 x)))
(defn exp [x] (Math/exp x))

(println (time ((intg fnc) 7.0)))
(println (time ((intg fnc) 8.0)))
(println (time ((intg fnc) 9.0)))
(println (time ((intg fnc) 10.0)))