# url-shortener

## Установка зависимостей

```bash
lein deps
npm install
```

## Сборка клиентской части

```bash
npx shadow-cljs compile app
```

## Запуск в режиме разработки (REPL)

```bash
lein repl
```

Затем из репла загружаем окружение разработки

```clojure
user=> (dev)
:loaded
dev=>
```

Теперь можно запустить систему следующими образом

```clojure
dev=> (go)
Server started on port: 8000
:initiated
```

После того как поменяли код, систему можно обновить с помощью

```clojure
dev=> (reset)
:reloading (...)
:resumed
```

Чтобы остановить систему

```clojure
dev=> (halt)
Server stopped!
:halted
```

## Запуск в режиме продакшена

1. Создаём базу данных, например в PostgreSQL.
2. Создаём переменную окружения с URL до базы данных `export DATABASE_URL=jdbc:postgresql://host/dbname`.
3. Устанавливаем миграции `lein run :duct/migrator`.
4. Запускаем сервер `lein run`.

## Ссылки

- [Duct framework](https://github.com/duct-framework/duct)
  - [Duct core](https://github.com/duct-framework/core)
  - [Duct module.sql](https://github.com/duct-framework/module.sql/)
    - [Duct database.sql.hikaricp](https://github.com/duct-framework/database.sql.hikaricp)
      - [database.sql](https://github.com/duct-framework/database.sql) db boundary, you can read about it [here](https://github.com/duct-framework/duct/wiki/Boundaries).
    - [Duct migrator.ragtime](https://github.com/duct-framework/migrator.ragtime)
  - [Duct module.logging](https://github.com/duct-framework/module.logging)
    - [Duct logger.timbre](https://github.com/duct-framework/logger.timbre)
