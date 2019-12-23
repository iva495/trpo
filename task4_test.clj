(ns org.eliseev.trpo.task4
  (:use clojure.test))

(load-file "task4.clj")

; are two forms the same?
(declare has-same-form?)

(defn same-forms? [f1 f2]
	(cond
		(variable? f1) (same-variables? f1 f2)

		(constant? f1) (and 
							(constant? f2) 
							(= (constant-value f1) (constant-value f2)))

		(negation? f1) (and 
							(negation? f2)
							(same-forms? (args f1) (args f2)))

		(conjunction? f1) (and
							(conjunction? f2)
							(= (count (args f1)) (count (args f2))) 
							(every? #(has-same-form? % (args f2)) (args f1)))

		(disjunction? f1) (and
							(disjunction? f2)
							(= (count (args f1)) (count (args f2))) 
							(every? #(has-same-form? % (args f2)) (args f1)))

		:else false
	)
)

(defn has-same-form? [form list_of_forms]
	some #(same-forms? form %) list_of_forms)


; is DNF?
(defn is-negation-or-atomic? [f]
	(or (negation? f) (atomic? f)))

(defn is-negation-atomic-or-conjunction? [f]
	(or (is-negation-or-atomic? f) (conjunction? f)))

(defn is-dnf? [form]
	(cond
		(atomic? form) true
		(negation? form) (atomic? (args form)) ; negations of atomic
		(conjunction? form) (every? is-negation-or-atomic? (args form)) ; conj of negations or atomic
		(disjunction? form) (every? is-negation-atomic-or-conjunction? (args form)) ; disj of negations, atomics or conjs
		:else false))



(deftest ev-wo-vars-test 
	(testing "Testing clojure4 / evalutation (without variables)"
		(is (let [
				f1_expr (implication (variable :x) (variable :y))
				f2_expr (disjunction (negation (variable :x)) (variable :y))
				f1 (full-evaluation f1_expr)
				f2 (full-evaluation f2_expr)
				]
				(and (is-dnf? f1) (same-forms? f1 f2))))

	(is (let [
				f1_expr (implication (variable :x) (implication (variable :y) (variable :z)))
				f2_expr (disjunction (negation (variable :x)) (disjunction (negation (variable :y)) (variable :z)))
				f1 (full-evaluation f1_expr)
				f2 (full-evaluation f2_expr)
				]
				(and (is-dnf? f1) (same-forms? f1 f2))))))

(deftest ev-w-vars-test 
	(testing "Testing clojure4 / evalutation (with variables)"
		(is (let [
					f1_expr (disjunction (conjunction (variable :x) (disjunction (variable :y) (variable :z))) (implication (variable :x) (variable :y)))
					f1 (full-evaluation f1_expr {:x (constant 1)})
					f2_expr (disjunction (variable :z) (variable :y) (variable :y))
					f2 (full-evaluation f2_expr)
					]
					(and (is-dnf? f1) (same-forms? f1 f2))))

		(is (let [
					f1_expr (conjunction
								(disjunction
									(negation (variable :z))
									(conjunction (variable :x) (variable :y))
								)
								(disjunction (variable :x) (variable :z))
							)
					f1 (full-evaluation f1_expr {:x (constant 1), :y (constant 0), :z (constant 0)})
					f2_expr (constant 1)
					f2 (full-evaluation f2_expr)
					]
					(and (is-dnf? f1) (same-forms? f1 f2)))))
)


(run-tests 'org.eliseev.trpo.task4)