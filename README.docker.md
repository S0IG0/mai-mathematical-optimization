# Развёртывание (Docker + Nginx Proxy Manager)

5 лабораторных работ по методам оптимизации. Каждая работа = Spring Boot
backend + React/Vite frontend (за nginx). Отдельная **hub-страница** —
единая точка входа со ссылками на все работы (открываются во встроенном
окне с шапкой «На главную»).

## Архитектура

```
                         сеть proxy (внешняя, общая с NPM)
                         ┌───────────────────────────────┐
  Браузер ─► NPM (443) ─►│ mo-hub        (точка входа)    │
                         │ mo-web-1 … mo-web-5 (фронты)   │
                         └───────────────┬───────────────┘
                                         │ сеть internal (приватная)
                         ┌───────────────┴───────────────┐
                         │ mo-backend-1 … mo-backend-5    │  (наружу не видны)
                         └───────────────────────────────┘
```

- **Браузер общается только с фронтами** (`mo-web-N`). Каждый `web-N` (nginx)
  отдаёт собранный фронт и сам проксирует `/api` и `/ws` на свой `backend-N`
  по приватной сети. Бэкендам **не нужен публичный хост** — они изолированы.
- Порты наружу **не публикуются** → конфликтов с NPM/Portainer нет.
- NPM ходит к контейнерам по именам через общую сеть `proxy`.

## Развёртывание на сервере

Предполагается, что NPM уже работает и использует внешнюю сеть `proxy`
(как у вашего nextcloud).

### 1. Сеть proxy

Она уже создана (NPM её использует). На всякий случай:

```bash
docker network inspect proxy >/dev/null 2>&1 || docker network create proxy
```

### 2. DNS

Добавьте записи на ваш домен (пример для `soigo.dedyn.io`), указывающие на
IP сервера:

```
mathopt.soigo.dedyn.io
lab1.soigo.dedyn.io
lab2.soigo.dedyn.io
lab3.soigo.dedyn.io
lab4.soigo.dedyn.io
lab5.soigo.dedyn.io
```

### 3. Адреса работ для хаба

```bash
cp .env.example .env
# отредактируйте .env: пропишите свои https://labN.<домен>/
```

### 4. Запуск

```bash
docker compose up -d        # образы соберутся автоматически при первом запуске
```

### 5. Проксі-хосты в Nginx Proxy Manager

Для каждого создать Proxy Host (вкладка *Hosts → Proxy Hosts → Add*):

| Domain Name              | Forward Hostname | Port | Websockets |
|--------------------------|------------------|------|------------|
| mathopt.soigo.dedyn.io   | `mo-hub`         | 80   | —          |
| lab1.soigo.dedyn.io      | `mo-web-1`       | 80   | —          |
| lab2.soigo.dedyn.io      | `mo-web-2`       | 80   | **вкл**    |
| lab3.soigo.dedyn.io      | `mo-web-3`       | 80   | **вкл**    |
| lab4.soigo.dedyn.io      | `mo-web-4`       | 80   | —          |
| lab5.soigo.dedyn.io      | `mo-web-5`       | 80   | —          |

Настройки для каждого хоста:
- **Scheme:** `http`, **Forward Port:** `80`.
- Вкладка **SSL:** *Request a new SSL Certificate*, включить *Force SSL*.
- Вкладка **Details:** включить *Websockets Support* (нужно для работ 2 и 3,
  можно включить для всех — не помешает).

> Forward Hostname — это имя контейнера (`mo-hub`, `mo-web-1` …). NPM видит их,
> потому что и NPM, и наши контейнеры подключены к сети `proxy`.

### 6. Готово

Точка входа: **https://mathopt.soigo.dedyn.io**

## Управление

```bash
docker compose ps                 # статус
docker compose logs -f web-2      # логи сервиса
docker compose up -d --build      # пересобрать и обновить
docker compose down               # остановить
```

## Локальный запуск (без NPM)

`.env` не нужен — хаб подставит дефолтные `http://localhost:808N`.
Сеть `proxy` всё равно нужна (укажена как external):

```bash
docker network create proxy        # один раз
docker compose up -d --build
```

Чтобы открыть локально без NPM, временно добавьте `ports:` к нужным сервисам
(например, хабу `- "8080:80"`) — либо обращайтесь к контейнерам по их IP.
