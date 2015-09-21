(ns structural-typing.use.f-type
  (:require [structural-typing.type :as type]
            [structural-typing.preds :as pred])
  (:use midje.sweet
        structural-typing.assist.special-words))

(fact "about checking"
  (let [repo (-> type/empty-type-repo
                 (type/replace-error-handler #(cons :error %))
                 (type/replace-success-handler (constantly "yay"))
                 (type/named :A (requires :a))
                 (type/named :B (requires :b)))]
                             
    (fact "calls the error and success function depending on the oopsies it gets back"
      (type/checked repo :A {:a 1}) => "yay"
      (type/checked repo :A {}) => (just :error (contains {:path [:a]})))

    (fact "can take a vector of type signifiers"
      (type/checked repo [:A :B] {:a 1}) => (just :error (contains {:path [:b]}))
      (type/checked repo [:A :B] {:b 1}) => (just :error (contains {:path [:a]}))
      (type/checked repo [:A :B] {:a 1, :b 1}) => "yay")))

(fact "about `described-by?`"
  (let [repo (-> type/empty-type-repo
                 (type/named :A (requires :a))
                 (type/named :B (requires :b)))]
                             
    (fact "one signifier"
      (type/described-by? repo :A {:a 1}) => true
      (type/described-by? repo :A {}) => false)

    (fact "can take a vector of type signifiers"
      (type/described-by? repo [:A :B] {:a 1}) => false
      (type/described-by? repo [:A :B] {:b 1}) => false
      (type/described-by? repo [:A :B] {:a 1, :b 1}) => true)))



(fact "imported preds"
  (let [repo (-> type/empty-type-repo
                 (type/named :Member {:a (pred/member [1 2 3])})
                 (type/named :Exactly {:a (pred/exactly even?)}))]
    (type/checked repo :Member {:a 3}) => {:a 3}
    (type/checked repo :Exactly {:a even?}) => {:a even?}))



(fact "degenerate cases"
  (fact "a predicate list can be empty"
    (let [repo (-> type/empty-type-repo
                   (type/named :A {:a []}))]
      (type/checked repo :A {})
      (type/checked repo :A {:a 1})))

  (fact "a whole type map can be empty"
    (let [repo (-> type/empty-type-repo
                   (type/named :A {}))]
      (type/checked repo :A {})
      (type/checked repo :A {:a 1}))))

    

