(ns reminders.api-test
  (:require [clojure.test :refer [deftest testing is]]
            [reminders.core :refer [handler]]
            [reminders.resources.db.core :refer [init-db!]]
            [ring.mock.request :as mock]
            [cheshire.core :refer [generate-string parse-string]]
            [clojure.java.shell :refer [sh]]))

(deftest api-test
  (testing "API (GET, POST, PUT, DELETE) tests"
    (let [_ (init-db!)
          _ (handler (mock/request :post "/register-user" {:user-name "punit-naik" :password "test123"}))
          token (get-in (handler (mock/request :post "/login" {:user-name "punit-naik" :password "test123"}))
                        [:headers "authorization"])
          response (handler (-> (mock/request :post "/reminders" {:description "get eggs" :scheduled_time "1535827167"})
                                (mock/header "authorization" token)))
          id (:id (try (parse-string (:body response) true) (catch Exception e nil)))]
      (is (= response
             {:status  201
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:id id :description "get eggs" :scheduled_time "2018-09-02 00:09:00"})}))
      (is (= (handler (-> (mock/request :get "/reminders")
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:reminders [{:id id :description "get eggs" :scheduled_time "2018-09-02 00:09:00"}]})}))
      (is (= (handler (-> (mock/request :get (str "/reminders/" id))
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:reminder {:id id :description "get eggs" :scheduled_time "2018-09-02 00:09:00"}})}))
      (is (= (handler (-> (mock/request :get (str "/reminders/" id"?date_format=unix"))
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:reminder {:id id :description "get eggs" :scheduled_time 1535827167}})}))
      (is (= (handler (-> (mock/request :put (str "/reminders/" id) {:description "get milk"})
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:id id :description "get milk" :scheduled_time "2018-09-02 00:09:00"})}))
      (is (= (handler (-> (mock/request :delete (str "/reminders/" id))
                          (mock/header "authorization" token)))
             {:status  200
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :body (generate-string {:deleted true})})))))

(deftest auth-api-test
  (testing "APIs with auth validation"
    (let [_ (init-db!)]
      (is (= (handler (mock/request :post "/reminders" {:description "get eggs" :scheduled_time "1535827167"}))
             {:status 401
              :headers {}
              :body "Not authorized"})))))

(deftest data-validation-api-test
  (testing "APIs with data validation"
    (let [_ (init-db!)
          _ (handler (mock/request :post "/register-user" {:user-name "punit-naik" :password "test123"}))
          token (get-in (handler (mock/request :post "/login" {:user-name "punit-naik" :password "test123"}))
                        [:headers "authorization"])]
      (is (= (handler (-> (mock/request :post "/reminders" {:description "get eggs" :scheduled_time "a"})
                          (mock/header "authorization" token)))
             {:status 400
              :headers {}
              :body "In: [:params :scheduled_time] val: \"a\" fails spec: :reminders.utils/scheduled_time at: [:params :scheduled_time] predicate: (fn [x] (re-matches #\"\\d+\" x))\n"})))))
