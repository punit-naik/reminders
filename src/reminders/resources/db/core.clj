(ns reminders.resources.db.core
  (:require [clojure.java.jdbc :as j]
            [java-jdbc.ddl :as ddl]
            [taoensso.timbre :refer [info]]
            [buddy.hashers :as hashers]
            [reminders.utils :refer [unix->iso8601]]))

(defonce ^:private db-conf
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "reminders.db"})

(defn init-db!
  "Create all the necessary tables!"
  []
  (try
    (do
      (j/db-do-commands db-conf
        (ddl/create-table :reminders
          [:id "INTEGER PRIMARY KEY AUTOINCREMENT"]
          [:description "text"]
          [:scheduled_time :datetime]))
      (j/db-do-commands db-conf
        (ddl/create-table :auth
          [:id "INTEGER PRIMARY KEY AUTOINCREMENT"]
          [:user_name "text UNIQUE"]
          [:password "text UNIQUE"])))
    (catch Exception e (.getMessage e))))

(defn fetch-reminders
  "Gets the reminders from the `reminders` table"
  [{:keys [date_format reminder-id all?]}]
  (let [result (map
                 (fn [row]
                   (if (= date_format "unix")
                     row
                     (assoc row :scheduled_time (unix->iso8601 (long (:scheduled_time row))))))
                 (j/query db-conf
                   (if reminder-id
                     (str "select * from reminders where id = " reminder-id)
                     "select * from reminders")))]
    (if (and (= (count result) 1) (not all?))
      (first result) result)))

(defn insert-reminder
  "Inserts a reminder in the `reminders` table"
  [row]
  (fetch-reminders {:reminder-id (first (vals (last (j/insert! db-conf :reminders row))))}))

(defn update-reminder
  "Updates a reminder with a specific ID from the `reminders` table"
  [{:keys [reminder-id] :as row}]
  (when (seq (dissoc row :reminder-id))
    (j/update! db-conf :reminders (dissoc row :reminder-id) ["id = ?" reminder-id]))
  (fetch-reminders {:reminder-id reminder-id}))

(defn delete-reminder
  "Deletes a reminder with a specific ID from the `reminders` table"
  [reminder-id]
  (= (first (j/delete! db-conf :reminders ["id = ?" reminder-id])) 1))

(defn register-user
  "Registers a user with a `user_name` and a `password`"
  [{:keys [user-name password]}]
  (j/insert! db-conf :auth {:user_name user-name :password (hashers/derive password)}))

(defn lookup-user
  "Searches for a user in the `auth` table by their `user_name` and `password`"
  [{:keys [user-name password]}]
  (when-let [row (first (j/query db-conf (str "select user_name, password from auth where user_name = \"" user-name "\"")))]
    (when (hashers/check password (:password row))
      row)))
