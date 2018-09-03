(ns reminders.routes.core
  (:require [compojure.core :refer [GET POST PUT DELETE defroutes]]
            [compojure.route :refer [not-found]]
            [reminders.resources.core :refer [get-reminders create-reminder
                                              update-reminder delete-reminder
                                              login logout sign-up]]
            [reminders.utils :refer [with-auth throttler]]))

(defroutes app-routes
  (GET "/" request (throttler (fn [_] "Hello World!")))
  (GET "/reminders" request (throttler (with-auth request get-reminders)))
  (GET "/reminders/:id{[0-9]+}" request (throttler (with-auth request get-reminders)))
  (POST "/reminders" request (throttler (with-auth request create-reminder)))
  (PUT "/reminders/:id{[0-9]+}" request (throttler (with-auth request update-reminder)))
  (DELETE "/reminders/:id{[0-9]+}" request (throttler (with-auth request delete-reminder)))
  (POST "/register-user" request (throttler (sign-up request)))
  (POST "/login" request (throttler (login request)))
  (POST "/logout" request (throttler (logout request)))
  (not-found "<h1>This is not the page you are looking for</h1>
              <p>Sorry, the page you requested was not found!</p>"))
