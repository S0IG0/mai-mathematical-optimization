#!/bin/sh
# Подставляем адреса работ из переменных окружения в config.js при старте.
set -e
envsubst '${LAB1_URL} ${LAB2_URL} ${LAB3_URL} ${LAB4_URL} ${LAB5_URL}' \
  < /etc/nginx/mo/config.template.js \
  > /usr/share/nginx/html/config.js
