(ns org.eliseev.trpo.task3.2
  (:use clojure.test))

(defn tfilter [pred? sequnce]
  (iterate (fn [res el] (conj res el)) ([[] 0]) sequnce)
)


; (map (fn [el] el) sequnce)
; @(future (if (pred? el) el nil))
; ()

(defn make-all-odds [] (filter odd? (range)))
(defn tmake-all-odds [] (tfilter odd? (range)))

; (println (filter odd? (range)))
(println (tfilter odd? (range)))
 
; (deftest a-test
;   (testing "test1"
;     (is (< (Math/abs (- ((intg exp) 0.0) 0.0)) 0.01))
;     (is (< (Math/abs (- ((intg exp) 1.0) 1.718281828459045)) 0.01))
;     (is (< (Math/abs (- ((intg exp) 2.0) 6.38905609893065)) 0.01))
;     (is (< (Math/abs (- ((intg exp) 3.0) 19.085536923187668)) 0.01))
;     (is (< (Math/abs (- ((intg exp) 4.0) 53.598150033144236)) 0.01))
;     (is (< (Math/abs (- ((intg exp) 5.0) 147.4131591025766)) 0.01))
;     (is (< (Math/abs (- ((intg exp) 6.0) 402.4287934927351)) 0.01))
;     (is (< (Math/abs (- ((intg exp) 7.0) 1095.6331584284585)) 0.01))
;     ))
; (run-tests 'org.eliseev.trpo.task2)