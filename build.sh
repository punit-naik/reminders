#!/bin/bash

rm reminders.db
lein do clean, ring uberjar
