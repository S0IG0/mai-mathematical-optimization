# syntax=docker/dockerfile:1

FROM nginx:alpine
COPY index.html /usr/share/nginx/html/index.html
COPY config.template.js /etc/nginx/mo/config.template.js
# Скрипт выполняется штатным entrypoint nginx перед запуском сервера
# и генерирует /usr/share/nginx/html/config.js из переменных окружения.
COPY 40-mo-config.sh /docker-entrypoint.d/40-mo-config.sh
RUN chmod +x /docker-entrypoint.d/40-mo-config.sh
EXPOSE 80
