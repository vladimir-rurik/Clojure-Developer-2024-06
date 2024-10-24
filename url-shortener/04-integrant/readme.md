# URL Shortener

## Установка зависимостей

```bash
lein deps
npm install
```

## Универсальный запуск из командной строки

### Сборка клиентской части

```bash
npx shadow-cljs compile app
```

### Запуск в режиме разработки (REPL)

```bash
lein repl
```

Теперь можно запустить систему следующими образом

```clojure
user=> (go)
Server started on port: 8000
```

Или остановить

```clojure
user=> (halt)
Server stopped
```

## Запуск через Jack-in: VSCode + Calva

1. Запускаем Jack-in через Command Palette.
2. Выбираем project type: `shadow-cljs`.
3. Выбираем build to compile: `app`.
4. Выбираем build to connect: `app`.
   В этот момент в `output.calva-repl` будет выводиться

   ```text
   ; Waiting for Shadow CLJS runtimes, start your CLJS app...`
   ```
  
5. Запускаем систему с помощью `(user/go)`.
6. Открываем в браузере `localhost:8000` — это и есть наш CLJS runtime.

После этого мы должны подключиться к CLJS реплу.
