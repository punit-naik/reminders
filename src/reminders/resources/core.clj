(ns reminders.resources.core
  (:require [reminders.resources.db.core :as db]
            [reminders.utils :refer [with-exception-api]]
            [ring.util.response :refer [response redirect]]
            [buddy.sign.jwt :refer [sign unsign]]
            [cheshire.core :refer [generate-string parse-string]]
            [taoensso.timbre :refer [spy]]))

(defonce ^:private secret (or (System/getenv "APP_SECRET") (System/getProperty "APP_SECRET")))

(defn create-reminder
  "Creates a reminder with `description` and `scheduled_time` in the `reminders` table"
  [{:keys [params]}]
  (with-exception-api
    (db/insert-reminder
      {:description (:description params)
       :scheduled_time (Long/valueOf (:scheduled_time params))})
    201))

(defn get-reminders
  "Gets all reminders or a reminder with a specific ID from the `reminders` table"
  [{:keys [params route-params uri]}]
  (let [response (with-exception-api
                   (db/fetch-reminders
                     {:date_format (:date_format params)
                      :reminder-id (:id route-params)
                      :all? (= uri "/reminders")})
                   200)]
    (conj (dissoc response :body)
      (if (map? (:body response))
        {:body {:reminder (:body response)}}
        {:body {:reminders (:body response)}}))))

(defn update-reminder
  "Updates a reminder with optional `description` and `scheduled_time` in the `reminders` table"
  [{:keys [params route-params]}]
  (with-exception-api
    (db/update-reminder
      (assoc (select-keys params [:description :scheduled_time])
        :reminder-id (:id route-params)))
    200))

(defn delete-reminder
  "Delets a reminder with a specific ID from the `reminders` table"
  [{:keys [route-params]}]
  (let [response (with-exception-api (db/delete-reminder (:id route-params)) 200)]
    (assoc (dissoc response :body) :body {:deleted (:body response)})))

(defn login
  "Attaches a token in the authorization header upon successful login,
   else throws a 401 exception"
  [{:keys [params headers]}]
  (if-let [user (db/lookup-user (select-keys params [:user-name :password]))]
    (assoc {:status 200 :body "Authenticated!"}
      :headers
      (assoc headers
        "authorization"
        (str "Token " (sign user secret))))
    {:status 401
     :body "credentials provided are either wrong or not provided at all"}))

(defn logout
  "Logs out by removing he authorization header from the request altogether"
  [request]
  (assoc (redirect "/")
    :headers (dissoc (:headers request) "authorization")))

(defn sign-up
  "Registers a user in the `auth` table"
  [{:keys [params]}]
  (try
    {:status 201
     :body {:user-created (not (empty? (db/register-user (select-keys params [:user-name :password]))))}}
    (catch Exception e
      {:status 401
       :body {:user-created false}})))
