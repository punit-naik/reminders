(ns reminders.db-test
  (:require [reminders.resources.db.core :as db]
            [clojure.java.jdbc :as j]
            [clojure.test :refer [deftest testing is]]))

(def ^:pivate sample-data
  {:unix-timestamp
    '({:id 1 :description "Get milk!" :scheduled_time 1535814181}
      {:id 2 :description "Get office docs sorted!" :scheduled_time 1535814299})
   :iso8601-timestamp
    '({:id 1 :description "Get milk!" :scheduled_time "2018-09-01 20:33:00"}
      {:id 2 :description "Get office docs sorted!" :scheduled_time "2018-09-01 20:34:00"})})

(deftest fetch-reminders-test
  (testing "`reminders` table select query tests"
    (with-redefs [j/query (fn [_ _] (:unix-timestamp sample-data))]
      (is (= (count (db/fetch-reminders {})) 2))
      (is (= (db/fetch-reminders {})
             (:iso8601-timestamp sample-data)))
      (is (= (db/fetch-reminders {:date_format "unix"})
             (:unix-timestamp sample-data))))
    (with-redefs [j/query (fn [_ _] (filter #(= (:id %) 1) (:unix-timestamp sample-data)))]
      (is (= (db/fetch-reminders {:reminder-id 1})
             (first (filter #(= (:id %) 1) (:iso8601-timestamp sample-data)))))
      (is (= (db/fetch-reminders {:reminder-id 1 :date_format "unix"})
             (first (filter #(= (:id %) 1) (:unix-timestamp sample-data))))))))

(deftest insert-reminders-test
  (testing "`reminders` table insert query tests"
    (let [unix-row (filter #(= (:id %) 1) (:unix-timestamp sample-data))
          iso8601-row (filter #(= (:id %) 1) (:iso8601-timestamp sample-data))]
      (with-redefs [j/insert! (fn [_ _ _] '({:last_insert_rowid 1}))
                    j/query (fn [_ _] unix-row)]
        (is (= (db/insert-reminder (dissoc (first unix-row) :id))
               (first iso8601-row)))))))

(deftest update-reminders-test
  (testing "`reminders` table update query tests"
    (let [unix-row (filter #(= (:id %) 1) (:unix-timestamp sample-data))
          iso8601-row (filter #(= (:id %) 1) (:iso8601-timestamp sample-data))]
      (with-redefs [j/update! (fn [_ _ _ _] '(1))
                    j/query (fn [_ _] unix-row)]
        (is (= (db/update-reminder (assoc (dissoc (first unix-row) :id) :reminder-id 1))
               (first iso8601-row)))))))

(deftest delete-reminders-test
  (testing "`reminders` table delete query tests"
    (with-redefs [j/delete! (fn [_ _ _] '(1))]
      (is (= (db/delete-reminder 1) true)))))
