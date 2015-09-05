(ns structural-typing.guts.preds.f_wrap
  (:require [structural-typing.guts.preds.wrap :as subject]
            [structural-typing.assist.oopsie :as oopsie]
            [structural-typing.guts.expred :as expred]
            [structural-typing.assist.lifting :as lifting])
  (:use midje.sweet))


(defn lift-and-run [pred value]
  ( (lifting/lift pred) {:leaf-value 3}))


(fact "show-as and explain-with"
  (fact "simple example"
    (let [pred (->> even?
                    (subject/show-as "name")
                    (subject/explain-with :predicate-string))]
      (pred 0) => true
      (pred 1) => false

      (oopsie/explanations (lift-and-run pred 1)) => ["name"]
      ( (lifting/lift pred) {:leaf-value 2}) => []))
    
  (fact "an anonymous lambda prints as something innocuous"
    (lift-and-run #(> 1 %) 3)
    => (just (contains {:predicate-string "<custom-predicate>"
                        :leaf-value 3
                        :explainer expred/default-predicate-explainer})))

  (fact "a named lambda has its name used as the predicate-string"
    (lift-and-run (fn greater-than-3 [n] (> n 3)) 3)
    => (just (contains {:predicate-string "greater-than-3"
                        :explainer expred/default-predicate-explainer})))
    
  (fact "functions can be tagged with names"
    (lift-and-run (->> (fn [n] (> n 3))
                       (subject/show-as "three"))
                  3)
    => (just (contains {:predicate-string "three"
                        :explainer expred/default-predicate-explainer})))

  (fact "you can override the explainer"
    (let [explainer (fn [{:keys [predicate-string
                                 path
                                 leaf-value]}]
                      (format "%s - %s - %s" path predicate-string leaf-value))]
      (lift-and-run (subject/explain-with explainer even?) 3)
      => (just (contains {:predicate (exactly even?) ; original predicate
                          :predicate-string "even?"  
                          :leaf-value 3
                          :explainer (exactly explainer)})))))

