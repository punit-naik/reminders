#!/bin/bash

# Optional Steps to check if the JWT authentication is working properly
# Register the user
curl -X POST -v -d"user-name=punit-naik&password=test123"  http://localhost:3000/register-user

# Login to get the JWT
curl -X POST -v -d"user-name=punit-naik&password=test123"  http://localhost:3000/login

# API testing
# Get all reminders (you will see no data). This is just to check if the JWT is working
curl -X GET -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGFlYjU4Yzc3ZmQ4MTA2YzJmMTE0N2ExYTViN2FmNDU3JDEyJDk1ZDEyM2RiNmM1YzRhMGE0ZDgxN2FhYWU3MWU4MTZlMDI4MTI3YjEyMDNlMmI0YSJ9.RziN_1rJPY8UGP5sljvq5Rtj-kZ36ll38hMfAgZKV9M" http://localhost:3000/reminders

# Put some data (the API will return the created reminders)
curl -X POST -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGFlYjU4Yzc3ZmQ4MTA2YzJmMTE0N2ExYTViN2FmNDU3JDEyJDk1ZDEyM2RiNmM1YzRhMGE0ZDgxN2FhYWU3MWU4MTZlMDI4MTI3YjEyMDNlMmI0YSJ9.RziN_1rJPY8UGP5sljvq5Rtj-kZ36ll38hMfAgZKV9M" -d "description=get milk&scheduled_time=1535827167" http://localhost:3000/reminders
curl -X POST -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGFlYjU4Yzc3ZmQ4MTA2YzJmMTE0N2ExYTViN2FmNDU3JDEyJDk1ZDEyM2RiNmM1YzRhMGE0ZDgxN2FhYWU3MWU4MTZlMDI4MTI3YjEyMDNlMmI0YSJ9.RziN_1rJPY8UGP5sljvq5Rtj-kZ36ll38hMfAgZKV9M" -d "description=get eggs&scheduled_time=1535828999" http://localhost:3000/reminders

# Get all reminders (this time you will see the data)
curl -X GET -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGFlYjU4Yzc3ZmQ4MTA2YzJmMTE0N2ExYTViN2FmNDU3JDEyJDk1ZDEyM2RiNmM1YzRhMGE0ZDgxN2FhYWU3MWU4MTZlMDI4MTI3YjEyMDNlMmI0YSJ9.RziN_1rJPY8UGP5sljvq5Rtj-kZ36ll38hMfAgZKV9M" http://localhost:3000/reminders

# Get a particular reminder (by ID). The below API call will give you the reminder corresponding to ID 1.
curl -X GET -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGFlYjU4Yzc3ZmQ4MTA2YzJmMTE0N2ExYTViN2FmNDU3JDEyJDk1ZDEyM2RiNmM1YzRhMGE0ZDgxN2FhYWU3MWU4MTZlMDI4MTI3YjEyMDNlMmI0YSJ9.RziN_1rJPY8UGP5sljvq5Rtj-kZ36ll38hMfAgZKV9M" http://localhost:3000/reminders/1

# Update a reminder (by ID), both `description` and `scheduled_time` are optional
curl -X PUT -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGFlYjU4Yzc3ZmQ4MTA2YzJmMTE0N2ExYTViN2FmNDU3JDEyJDk1ZDEyM2RiNmM1YzRhMGE0ZDgxN2FhYWU3MWU4MTZlMDI4MTI3YjEyMDNlMmI0YSJ9.RziN_1rJPY8UGP5sljvq5Rtj-kZ36ll38hMfAgZKV9M" -d "description=get milk 2 ltrs" http://localhost:3000/reminders/1

# Delete a reminder (by ID)
curl -X DELETE -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGFlYjU4Yzc3ZmQ4MTA2YzJmMTE0N2ExYTViN2FmNDU3JDEyJDk1ZDEyM2RiNmM1YzRhMGE0ZDgxN2FhYWU3MWU4MTZlMDI4MTI3YjEyMDNlMmI0YSJ9.RziN_1rJPY8UGP5sljvq5Rtj-kZ36ll38hMfAgZKV9M" http://localhost:3000/reminders/1

# Test to check for data validation. Should fail because scheduled_time should be a number (long).
curl -X POST -v -H "Authorization: Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiJwdW5pdC1uYWlrIiwicGFzc3dvcmQiOiJiY3J5cHQrc2hhNTEyJGFlYjU4Yzc3ZmQ4MTA2YzJmMTE0N2ExYTViN2FmNDU3JDEyJDk1ZDEyM2RiNmM1YzRhMGE0ZDgxN2FhYWU3MWU4MTZlMDI4MTI3YjEyMDNlMmI0YSJ9.RziN_1rJPY8UGP5sljvq5Rtj-kZ36ll38hMfAgZKV9M" -d "description=failed reminder&scheduled_time=a" http://localhost:3000/reminders
