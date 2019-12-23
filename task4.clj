(ns org.sitnov.trpo.task4
  (:use clojure.test))


; const
(defn constant [n]
	{pre [(or (= n 1) (= n 0))]}
	(list ::const n))


(defn constant? [expr]
	(= (first expr) ::const))

(defn constant-value [expr]
	(second expr))

(defn true_constant? [expr]
	(and (constant? expr) (= (constant-value expr) 1)))

(defn false_constant? [expr]
	(and (constant? expr) (= (constant-value expr) 0)))

; var
(defn variable [name]
	{pre [(keyword? name)]}
	(list ::var name))

(defn variable? [expr]
	(= (first expr) ::var))

(defn variable-name [expr]
	(second expr))

(defn same-variables? [v1 v2]
	(and
		(variable? v1)
		(variable? v2)
		(= (variable-name v1) (variable-name v2))))

; operators
; and
(defn conjunction [x & rest]
	(if (empty? rest)
		x
		(list ::and (cons x rest))))

(defn conjunction? [x]
	(= (first x) ::and))

; or
(defn disjunction [x & rest]
	(if (empty? rest)
		x
		(list ::or (cons x rest))))

(defn disjunction? [x]
	(= (first x) ::or))

; not
(defn negation [x]
	(list ::not x))

(defn negation? [x]
	(= (first x) ::not))

; x -> y 
(defn implication [x y]
	(disjunction (negation x) y))


(defn args [x]
	(first (rest x)))

(defn atomic? [x]
	(or
		(variable? x) 
		(constant? x)))


(declare evaluate)

; list of rules [predicate-fn modifier-fn rule-name]

(def evaluation-rules
	(list
		; propagate negations

		[(fn [expr vrs]
			(and
				(negation? expr)
				(conjunction? (args expr))))
		 (fn [expr vrs] 
		 	(apply disjunction (map negation (args (args expr)))))

		 "not (and ...)"
		]

		[(fn [expr vrs]
			(and
				(negation? expr)
				(disjunction? (args expr))))
		 (fn [expr vrs] 
		 	(apply conjunction (map negation (args (args expr)))))

		 "not (or ...)"
		]

		; remove double negations

		[(fn [expr vrs]
			(and
				(negation? expr)
				(negation? (args expr))))
		 (fn [expr vrs] 
		  	(args (args expr)))

		 "not not x"
		]

		; distributive rule
		; X && (Y || Z) == (X && Y) || (X && Z)

		[(fn [expr vrs]
			(and
				(conjunction? expr)
				(some disjunction? (args expr))))
		 (fn [expr vrs]
		 	(let [
		 		conj_elements (args expr)
		 		disj (some #(if (disjunction? %) % false) conj_elements)
		 		xrest (filter #(not (= % disj)) conj_elements)]

		 		(do 
		 			;(println "\t" (map printexpr conj_elements))
		 			;(println "\t" (printexpr disj))
		 			;(println "\t" (map printexpr xrest))
		 			(apply disjunction (map #(apply conjunction (cons % xrest)) (args disj)))
		 		)))

		 "X && (Y || Z)"
		]

		; expand brackets

		[(fn [expr vrs]
			(and
				(conjunction? expr)
				(some conjunction? (args expr))))
		 (fn [expr vrs]
		 	(let [
		 		conj_elements (args expr)
		 		conjunc (some #(if (conjunction? %) % false) conj_elements)
		 		xrest (filter #(not (= % conjunc)) conj_elements)]

		 		(do 
		 			;(println "\t" (map printexpr conj_elements))
		 			;(println "\t" (printexpr conjunc))
		 			;(println "\t" (map printexpr xrest))
		 			(apply conjunction (concat xrest (args conjunc)))
		 		)))

		 "X && (Y && Z)"
		]

		[(fn [expr vrs]
			(and
				(disjunction? expr)
				(some disjunction? (args expr))))
		 (fn [expr vrs]
		 	(let [
		 		disj_elements (args expr)
		 		disjunc (some #(if (disjunction? %) % false) disj_elements)
		 		xrest (filter #(not (= % disjunc)) disj_elements)]

		 		(do 
		 			;(println "\t" (map printexpr disj_elements))
		 			;(println "\t" (printexpr disjunc))
		 			;(println "\t" (map printexpr xrest))
		 			(apply disjunction (concat xrest (args disjunc)))
		 		)))

		 "X || (Y || Z)"
		]

		; simplify expressions

		[(fn [expr vrs]
			(and
				(conjunction? expr)
				(some false_constant? (args expr))))
		 (fn [expr vrs]
		 	(constant 0))

		 "X && 0"
		]

		[(fn [expr vrs]
			(and
				(disjunction? expr)
				(some true_constant? (args expr))))
		 (fn [expr vrs]
		 	(constant 1))

		 "X || 1"
		]

		[(fn [expr vrs]
			(and
				(conjunction? expr)
				(some true_constant? (args expr))))
		 (fn [expr vrs]
		 	(let [rst (filter #(not (true_constant? %)) (args expr))]
		 		(apply conjunction (if (empty? rst) (constant 1) rst))
		 		))

		 "X && 1"
		]

		[(fn [expr vrs]
			(and
				(disjunction? expr)
				(some false_constant? (args expr))))
		 (fn [expr vrs]
		 	(let [rst (filter #(not (false_constant? %)) (args expr))]
		 		(apply disjunction (if (empty? rst) (constant 0) rst))
		 		))

		 "X || 0"
		]

		; negation of a constant

		[(fn [expr vrs]
			(and
				(negation? expr)
				(false_constant? (args expr))))
		 (fn [expr vrs]
		 	(constant 1))

		 "not 0"
		]

		[(fn [expr vrs]
			(and
				(negation? expr)
				(true_constant? (args expr))))
		 (fn [expr vrs]
		 	(constant 0))

		 "not 1"
		]

		; const ops
		; 1 && 1
		[(fn [expr vrs]
			(and
				(negation? expr)
				(true_constant? (args expr))))
		 (fn [expr vrs]
		 	(constant 0))

		 "not 1"
		]

		; variables substitution
		; find values of variable names in <vrs> and replace names with constant values

		[(fn [expr vrs]
			(and (variable? expr) (contains? vrs (variable-name expr))))
		 (fn [expr vrs] ; #{:fred}
		 	(get vrs (variable-name expr)))

		 "variable substitution"
		]

		; evaluate arguments of not/and/or expressions

		[(fn [expr vrs]
			(and (negation? expr) (not (constant? (args expr)))))
		 (fn [expr vrs]
		 	(negation (evaluate (args expr) vrs)))

		 "not (evaluated)"
		]

		[(fn [expr vrs]
			(and (disjunction? expr) (not (constant? (args expr)))))
		 (fn [expr vrs] 
		 	(apply disjunction (map #(evaluate % vrs) (args expr))))

		 "or (evaluated)"
		]

		[(fn [expr vrs]
			(and (conjunction? expr) (not (constant? (args expr)))))
		 (fn [expr vrs] 
		 	(apply conjunction (map #(evaluate % vrs) (args expr))))

		 "and (evaluated)"
		]

		; if none of these rules apply, statement should remain intact

		[(fn [expr vrs] true) (fn [expr vrs] expr) "statement"]
))

; apply first rule that applies (which predicate is true)
(defn evaluate [expr variables-values]
	((some (fn [rule] 
				(if ((first rule) expr variables-values)
					(second rule)
					false))
			evaluation-rules)
		expr variables-values))


(require '[clojure.string :as string])

(defn printterm? [expr]
	(or 
		(atomic? expr)
		(negation? expr)))

(defn printexpr [expr]
	(str
		(if (printterm? expr) "" "(")
		(cond
			(variable? expr) (str (variable-name expr))
			(constant? expr) (str (constant-value expr))
			(disjunction? expr) (string/join " || " (map printexpr (args expr)))
			(conjunction? expr) (string/join " && " (map printexpr (args expr)))
			(negation? expr) (str "!" (printexpr (args expr)) "")
			:else "unknown")
		(if (printterm? expr) "" ")")))

; apply rules until expression stops changing
(defn full-evaluation 
	([expr] (full-evaluation expr nil))
	([expr vrs] (full-evaluation expr vrs false))
	([expr vrs debug]
		(do 
			(if debug (println (printexpr expr)) false)
			(let [ev (evaluate expr vrs)]
				(if (= ev expr)
					expr (full-evaluation ev vrs debug))))))



; (def expr (disjunction (variable "x") (negation (variable "x"))))
(def expr (disjunction (constant 0) (constant 1)))
(println (printexpr expr))
(println (printexpr (full-evaluation (variable :x) {:x (constant 1)}) ))