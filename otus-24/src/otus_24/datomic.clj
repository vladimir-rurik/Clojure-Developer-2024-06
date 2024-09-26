(ns otus-24.datomic
  (:require [datomic.api :as d]))

(def db-uri "datomic:mem://test")

(d/create-database db-uri)

(def conn (d/connect db-uri))

(def schema-tx
  [{:db/ident :mage/name
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/doc "A mage's name"}
   {:db/ident :mage/age
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/long}
   {:db/ident :mage/spells
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc "Spellbook, cons"}
   {:db/ident :spell/name
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/string
    :db/unique :db.unique/identity}
   {:db/ident :spell/manacost
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/long}])

(d/transact conn schema-tx)

(d/pull (d/db conn) '[* {:db/unique [*]}] :mage/name)

(d/transact conn [{:mage/name "David"
                   :mage/age 25}
                  {:mage/name "John"
                   :mage/age 75
                   :db/id "john"}
                  {:mage/name "Ivan"
                   :mage/age 200}

                  {:spell/name "Fireball"
                   :spell/manacost 100
                   :db/id "fb"}

                  {:spell/name "Frostbolt"
                   :spell/manacost 100
                   :db/id "fsb"}

                  ;; другой вариант сохранения информации о фактах в БД
                  [:db/add "john" :mage/spells "fb"]
                  [:db/add "john" :mage/spells "fsb"]])

(d/pull (d/db conn) '[*] [:mage/name "John"])
;; => {:db/id 17592186045420,
;;     :mage/name "John",
;;     :mage/age 75,
;;     :mage/spells [{:db/id 17592186045423}]}

;; => {:db/id 17592186045420,
;;     :mage/name "John",
;;     :mage/age 75,
;;     :mage/spells [{:db/id 17592186045422} {:db/id 17592186045423}]}

(d/transact conn [[:db/retract [:mage/name "John"] :mage/spells [:spell/name "Fireball"]]])

(d/q '[:find ?tx .
       :where [?e :mage/name "John" ?tx]]
     (d/db conn))
;; => 13194139534314

(def db
  (d/as-of (d/db conn) 13194139534314))

(def tx
  (d/with db [{:spell/name "Thunderstorm"
               :spell/manacost 1000
               :db/id "ts"}
              [:db/add "john" :mage/spells "ts"]]))

;; => #'otus-24.datomic/tx

(def db'
  (:db-after tx))

(keys tx)
;; => (:db-before :db-after :tx-data :tempids)

(:tempids tx)
;; => {"ts" 17592186045426, "john" 17592186045427}

(d/pull (:db-after tx) '[*] 17592186045426)
;; => {:db/id 17592186045426}

(d/pull db' '[* {:mage/spells [*]}] [:mage/name "John"])

;; => {:db/id 17592186045420,
;;     :mage/name "John",
;;     :mage/age 75,
;;     :mage/spells [{:db/id 17592186045423}]}

{:db/id 17592186045420,
 :mage/name "John",
 :mage/age 75,
 :mage/spells [{:db/id 17592186045422}
               {:db/id 17592186045423}]}
