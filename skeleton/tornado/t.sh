#!/bin/bash

# object: https://api.leancloud.cn/1.1/classes/Post
# create user: https://api.leancloud.cn/1.1/users
# curl -v -X POST \
#   -H "X-AVOSCloud-Application-Id: csUaPwlbgCbf1ykgNW7m4DDo" \
#   -H "X-AVOSCloud-Application-Key: XtPGKidFtu9IYxUNYcqqK19f" \
#   -H "Content-Type: application/json" \
#   -d '{"username": "zaopuppy","password": "123333"}' \
#   https://api.leancloud.cn/1.1/users

# login
curl -X GET \
  -H "X-AVOSCloud-Application-Id: csUaPwlbgCbf1ykgNW7m4DDo" \
  -H "X-AVOSCloud-Application-Key: XtPGKidFtu9IYxUNYcqqK19f" \
  -G \
  --data-urlencode 'username=zaopuppy' \
  --data-urlencode 'password=123333' \
  https://api.leancloud.cn/1.1/login


echo ""


