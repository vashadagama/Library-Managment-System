# План приведения проекта LIMS к ТЗ курсового проекта

**Дата анализа:** 25.10.2026  
**Старый проект:** `D:\Учёба\MyProjects\lims`  
**Материалы курсового:** `D:\Учёба\5 семестр\Организация баз данных курсовой проект`

---

## Статус работ (обновлено 18.11.2026, база переведена на v0.0.2.9)

### Важное изменение: переход на актуальную версию с GitHub

Обнаружено, что автор параллельно развивал код на GitHub (`vashadagama/Library-Managment-System`): серия коммитов `0.0.2.0–0.0.2.9` (31.10–15.11.2026) уже реализовала большую часть «Этапа A»:

- **Flyway-миграции V1–V4** (init, классификаторы/fines/notifications/audit, сиды, FK-индексы);
- **справочники как сущности** (`entity/status/*`: Role, UserStatus, ItemStatus, LoanStatus, Genre, Language, Publisher, Location, AuditType, NotificationType) вместо enum;
- **новые таблицы по схеме ТЗ**: fines, notifications, audit_logs, system_parameters, publishers, locations (итого 22 доменные таблицы);
- `ddl-auto=validate`, docker-compose.yml, DictionaryController + SystemParameterService;
- переработанный фронтенд (Win98-стиль, экраны: Главная, Каталог, Авторы, Читатели, Выдачи, Статистика);
- **самостоятельно устранены дефекты**: №1 (права users: GET/POST → LIBRARIAN+ADMIN, остальное ADMIN), №2 (AuthenticationException → 401 с русским сообщением), №3 (синхронное восстановление сессии в AuthContext), №4 (cast/coalesce-трюк в поиске), №8 (статусы через справочник → 404), №1b (axios: разлогин только при 401 с guard на /auth/login), COUNT(DISTINCT) в популярности.

Локально: master сброшен на origin/master (`7f54e81` = 0.0.2.9). Старые ветки сохранены: `coursework-old-defects` — фиксы на устаревшей базе (оттуда доступен снапшот pre-coursework), рабочая ветка — `coursework`.

### Исправлено дополнительно (коммит `4d73aad`, ветка `coursework`, запушена на GitHub)

- **DELETE `/api/users/{id}`** — мягкое удаление (status=DELETED), только для читателей, запрет при активных выдачах, повторном удалении и для сотрудников; удалённые скрыты из списков (`findByStatusNameNot`).
- **Обработчики ошибок**: 400 (мусорный JSON, bad UUID, IllegalArgumentException), 404 (NoResourceFound), 405 (метод не поддержан) вместо 500; @Slf4j-логирование во всех handler-ах.
- **JwtRequestFilter**: try-catch вокруг парсинга JWT — просроченный/битый токен → неаутентифицированный запрос (401), а не 500/403.
- **SecurityConfig**: authenticationEntryPoint (401 с JSON) + accessDeniedHandler (403 с JSON).
- **`/api/users/search`** — убран паттерн `:search IS NULL OR ...` (500 «could not determine data type» → 200).
- **Популярные книги**: STRING_AGG вынесен в подзапрос — дубли авторов на мульти-авторных книгах устранены (проверено на книге с 2 авторами: loanCount=1, оба автора).
- **springdoc 2.5.0 → 3.2.0** — Swagger работает на Spring Boot 4.
- **V3__seed_classifiers.sql**: добавлены статусы FROZEN и DELETED в user_statuses (по ТЗ — 4 статуса).

### Состояние проверки

- Юнит-тесты: **21/21 зелёные**.
- API-проверки (smoke_2_9.ps1 + deep_checks.ps1): все кейсы PASS — права, поиск (books/magazines/users), словари анонимно, популярность, delete-потоки (204 → 400 «уже удалён» → 400 «активные выдачи» → запрет удаления сотрудника → скрытие из списка), 400/404/405, 401 entry point, Swagger 200.
- БД: пересоздана, Flyway V1–V4 применены чисто, сиды полные, демо-данные DataInitializer (5 users, 8 items, 9 copies, 3 loans; admin@lims.com/admin123, librarian@lims.com/librarian123, reader1-3 без паролей).
- Браузерные проверки нового UI: вход/inline-ошибка «Неверный email или пароль»/восстановление сессии после полной перезагрузки на /readers/403 без разлогина — PASS.

### Не выполнено (следующие шаги)

- ~~Этап B~~ — **выполнен (коммит `1de562c`, 18.11.2026)**:
  - Штрафы: начисление при возврате было уже в 0.0.2.9; добавлены API (`GET /api/fines`, `PATCH /api/fines/{id}/pay`) и экран «Штрафы» (фильтры неоплаченные/все/оплаченные, оплата с подтверждением).
  - Уведомления: `NotificationService` — DUE_SOON (за 3 дня до срока) и OVERDUE, дедупликация по (пользователь, тип, текст); генерация планировщиком ежедневно в 03:00 и catch-up при старте (`ApplicationReadyEvent`); API `GET /api/notifications`.
  - Аудит: `AuditService` (REQUIRES_NEW, не ломает бизнес-операцию) — LOGIN, ISSUE, RETURN, RENEW, CREATE/UPDATE/DELETE USER, UPDATE FINE; API `GET /api/audit` (только ADMIN).
  - Отчёты (4 по ТЗ) + выгрузка PDF (OpenPDF, кириллица через системный TTF): «Популярные издания» (за период), «Активные читатели», «Задолженности по штрафам», «Просроченные выдачи» — `GET /api/reports/*`, кнопки на странице «Статистика».
  - Сотрудники (админ-раздел): создание LIBRARIAN/ADMIN (пароль обязателен, библиотекарю запрещено — 403), смена роли `PATCH /api/users/{id}/role` (запрет смены собственной роли, требование пароля), установка пароля `PATCH /api/users/{id}/password`; UI — поля роль/пароль в форме создания + диалог «Роль пользователя».
  - Просрочки: `LoanDto.fineAmount` — проекция штрафа (дни × `fine_per_day`) в `/api/loans/overdue`; колонка «Штраф» в UI выдач.
  - Системные параметры: уже использовались в 0.0.2.9 (`max_active_loans_default`, `loan_period_days`, `fine_per_day`) — подтверждено.
- **Этап C:** финальное полное тестирование (юнит + API + UI обеих ролей) → отчёт → комплект документации в `D:\Учёба\MyProjects\documentation` (после подтверждения пользователем).

### Проверки Этапа B (stage_b_checks.ps1 — 18/19 PASS; 1 «FAIL» = парсинг десятичных запятыми в PowerShell-скрипте)

- Просрочка → catch-up пометил 3 выдачи OVERDUE при старте, создал 3 уведомления OVERDUE.
- Возврат просроченной выдачи → штраф 50.00 ₽ (5 дн. × 10 ₽/дн.) создан; штрафы видны в API и UI; оплата → 200, повторная → 400.
- 4 PDF-отчёта: 200, валидный `%PDF` (29–44 КБ), скачиваются из UI с тостом подтверждения.
- Аудит: LOGIN/RETURN/UPDATE/CREATE записываются; ADMIN видит, LIBRARIAN → 403.
- Сотрудники: LIBRARIAN создаёт сотрудника → 403; ADMIN → 200; смена роли без пароля → 400, с паролем → 200; смена своей роли → 400.
- Сборка фронтенда (vite build) — успешно; браузерные проверки новых экранов — PASS.

---

## 0. Краткий вывод

Старый проект — это рабочий учебный прототип библиотечной информационной системы на Spring Boot + React. Его можно и нужно использовать как основу: уже есть каркас приложения, JWT-авторизация, каталог книг и журналов, экземпляры, выдачи, читатели, часть статистики, Docker для PostgreSQL и тесты.

Но **модель данных старого проекта не соответствует новой схеме БД из курсового**:

- вместо справочников (`roles`, `user_statuses`, `item_statuses`, `loan_statuses`, `genres`, `languages`, `publishers`, `locations`) используются Java-enum и строки;
- у книг/журналов издательство, язык и место хранения — обычные строки, а в новой схеме это внешние ключи;
- нет таблиц `fines`, `notifications`, `audit_logs`, `system_parameters`;
- у пользователя лишний `phone_number`, неверно назван `patronimyc`, пароль хранится в `password`, а в схеме — `password_hash`;
- у экземпляра нет `acquisition_date` и `price`;
- у выдачи нет `librarian_id` и `renewal_count`;
- база создаётся Hibernate через `spring.jpa.hibernate.ddl-auto=update`, а не миграциями.

**Стратегия:** сначала привести проект к новой схеме БД и заставить его работать в прежнем объёме (этап A), затем добавлять недостающий функционал по ТЗ (этап B), затем тесты, документация и защита (этап C).

**Ориентировочная трудоёмкость:** 6–8 недель при регулярной работе. Критический путь — этап A (БД + сущности), его желательно закрыть до 20.11.2026, чтобы не сорвать этап 2 курсового.

---

## 1. Исходные материалы курсового

В папке курсового лежат:

| Файл | Что это |
|---|---|
| `(0) ТЗ БД.docx` | Техническое задание на 14 листов: назначение, функции, требования, этапы, сроки |
| `(1) Концептуальная модель БД.png` | Концептуальная модель: 20 сущностей, связи, кардинальности |
| `(2) Схема БД.jpg` | Физическая схема PostgreSQL: таблицы, поля, типы, PK/FK/UNIQUE/NULL |
| `(2) library_db.sql` | SQL-дамп PostgreSQL 15.18: только DDL, без данных |
| `library.db` | Текстовый SQL-скрипт с тем же DDL (не бинарная база SQLite) |
| `Этап 2 БД и схема Артемов.rar` | Архив с теми же материалами |

Важные наблюдения:

1. **Дамп пустой** — в нём нет тестовых данных, только структура. Демо-данные нужно создавать самостоятельно.
2. В дампе есть psql-команды `\restrict`, `\unrestrict` и блоки `COPY ... FROM stdin`. **Flyway их не понимает** — при переносе в миграцию их нужно убрать.
3. В дампе нет индексов, кроме PK/UNIQUE. Для курсового по организации БД индексы, проверочные ограничения и представления — хорошее поле для демонстрации.
4. Схема на картинке подписана «Postgres 18», а дамп снят с PostgreSQL 15.18. Для проекта подойдёт PostgreSQL 15+, но версию лучше зафиксировать в документации и `docker-compose`.
5. Концептуальная и физическая модели уже есть; логическую модель (реляционную, с обоснованием 3НФ) и словарь данных, скорее всего, нужно будет оформить в пояснительной записке.

---

## 2. Что представляет собой старый проект

### 2.1. Стек

**Backend:**

- Java 26;
- Spring Boot 4.0.5;
- Spring Web MVC;
- Spring Data JPA / Hibernate;
- Spring Security + JJWT 0.11.5;
- PostgreSQL JDBC;
- Lombok;
- springdoc-openapi 2.5.0;
- Maven Wrapper (`mvnw.cmd`);
- H2 — только для тестов.

**Frontend:**

- React 18;
- Vite 5;
- Axios;
- React Router 6;
- React Toastify;
- Electron 30 — как необязательная обёртка для desktop.

**Инфраструктура:**

- `docker-compose.yml` поднимает только PostgreSQL 15;
- база `lims`, пользователь `user`, пароль `password`, порт 5432.

### 2.2. Архитектура и поток запроса

```
React (Vite, :3000)
   ↓ Axios + JWT в заголовке Authorization
JwtRequestFilter
   ↓
Controller (@RestController, /api/...)
   ↓
Service (@Service, @Transactional)
   ↓
Repository (Spring Data JPA)
   ↓
PostgreSQL (localhost:5432/lims)
```

Слои:

- `model` — JPA-сущности;
- `repository` — интерфейсы Spring Data;
- `service` — бизнес-логика;
- `controller` — REST API;
- `dto` — объекты запросов/ответов;
- `security` — JWT, UserDetails;
- `config` — SecurityConfig, DataInitializer;
- `scheduler` — ежедневная проверка просрочек;
- `exception` — глобальная обработка ошибок.

### 2.3. Как создаётся база данных

В `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/lims
spring.datasource.username=user
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Это значит: **Hibernate при старте сам создаёт и изменяет таблицы по сущностям**. Для прототипа удобно, но для курсового это плохой вариант:

- нет истории миграций и воспроизводимости;
- схема зависит от аннотаций, а не от утверждённой физической модели;
- `update` не удаляет лишние колонки и не переименовывает их;
- ограничения, индексы, представления и seed-данные не описаны явно;
- на другой машине может получиться другая схема;
- преподавателю сложно показать, что БД спроектирована, а не «сгенерировалась».

При этом **отказываться от PostgreSQL не нужно**. Нужно заменить способ управления схемой: Flyway + явные SQL-миграции + `ddl-auto=validate`. Это и есть «полноценная БД» в понимании курсового.

### 2.4. Что уже реализовано

**Авторизация:**

- `POST /api/auth/login` — email + пароль, возвращает JWT;
- `JwtRequestFilter` проверяет токен;
- роли: `READER`, `LIBRARIAN`, `ADMIN`;
- читатели создаются без пароля и войти не могут;
- пароли хранятся в BCrypt.

**API:**

- авторы: CRUD;
- книги: CRUD, поиск, фильтры по жанру/издательству/году, пагинация;
- журналы: CRUD, поиск, пагинация;
- экземпляры: создание, список, смена статуса, удаление;
- выдачи: выдача по экземпляру и по изданию, возврат, продление, просрочки, история, статистика;
- пользователи: список, создание, смена статуса, лимит выдач, поиск.

**Frontend:**

- вход;
- главная панель со статистикой и быстрым поиском;
- каталог книг/журналов;
- карточки книги/журнала с экземплярами;
- читатели;
- выдачи;
- статистика популярных книг;
- формы создания/редактирования книг и журналов;
- ролевое меню.

**Плюсы, которые нужно сохранить:**

- JWT + BCrypt + валидация DTO;
- глобальный обработчик ошибок;
- разделение controller/service/repository/dto;
- пагинация;
- Docker для БД;
- задел на планировщик;
- unit-тесты.

### 2.5. Слабые места старого проекта

1. **Нет Flyway** — схема создаётся Hibernate.
2. **Справочники заменены enum/строками** — не соответствует новой схеме.
3. **Нет штрафов** — в `returnBook` только `System.out.printf` с «заглушкой».
4. **Нет уведомлений** — таблицы и логики нет.
5. **Нет аудита** — журнала действий нет.
6. **Нет системных параметров** — срок выдачи 14 дней и штраф 10 руб./день захардкожены.
7. **Нет отчётов и PDF** — только топ популярных книг.
8. **Админка неполная** — нет управления справочниками, сотрудниками, ролями, аудитом, параметрами.
9. **Опечатки/лишние поля** — `BORROVED` вместо `BORROWED`, `patronimyc`, `phoneNumber`.
10. **Секреты в коде** — JWT-секрет и пароль БД в `application.properties`, демо-пароли в `DataInitializer`.
11. **Хрупкая конкурентность** — два одновременных запроса на выдачу могут выбрать один и тот же экземпляр.
12. **Нет интеграционных тестов** — unit-тесты не проверяют схему и ограничения.
13. **Грязный git** — есть незакоммиченные изменения, `node_modules`, логи JVM-краша.
14. **Окружение** — Docker Desktop сейчас не запущен, `mvn`/`node` не в PATH (но есть `mvnw.cmd` и `node_modules`).

---

## 3. Что требует ТЗ и схема БД

### 3.1. Функциональные требования

**Аутентификация и авторизация:**

- регистрация читателя библиотекарем;
- создание учётных записей сотрудников администратором;
- вход сотрудников по email/паролю с JWT;
- роли `LIBRARIAN`, `ADMIN` (читатели — отдельная категория пользователей без входа).

**Справочники:**

- роли;
- статусы пользователей;
- статусы экземпляров;
- статусы выдач;
- жанры;
- языки;
- издательства;
- места хранения.

**Администратор:**

- создание сотрудников;
- изменение роли и блокировка любого пользователя;
- управление читателями: заморозка, блокировка, лимит выдач, удаление;
- управление всеми справочниками;
- просмотр журнала аудита;
- настройка системных параметров (срок выдачи, штраф за день);
- полная статистика.

**Библиотекарь:**

- регистрация и ведение читателей;
- управление каталогом (книги, журналы, авторы);
- управление экземплярами (добавление, статусы: доступен, выдан, на ремонте, утерян, списан);
- выдача по экземпляру или по изданию с автовыбором;
- возврат;
- продление;
- начисление и списание штрафов;
- отчёты по выдачам и просрочкам.

**Автоматизированные процессы:**

- ежедневная проверка просроченных выдач;
- начисление штрафа за каждый день просрочки;
- создание и отправка уведомлений о просрочке.

**Выходные данные:**

- результаты поиска по каталогу;
- активные выдачи читателя;
- просроченные выдачи с суммой штрафа;
- отчёт «Популярные издания» за период;
- отчёт «Активные читатели»;
- отчёт «Задолженности по штрафам»;
- выгрузка отчётов в PDF.

### 3.2. Модель данных

20 таблиц:

1. `roles`
2. `user_statuses`
3. `item_statuses`
4. `loan_statuses`
5. `genres`
6. `languages`
7. `publishers`
8. `locations`
9. `system_parameters`
10. `users`
11. `authors`
12. `library_items`
13. `books`
14. `magazines`
15. `item_authors`
16. `item_copies`
17. `loans`
18. `fines`
19. `notifications`
20. `audit_logs`

Ключевые особенности:

- `library_items` — супертип для книг и журналов (class-table inheritance);
- `books.id` и `magazines.id` одновременно PK и FK на `library_items.id`;
- `item_authors` — M:N между изданиями и авторами;
- все статусы и роли — справочники, а не enum;
- `users` хранит и сотрудников, и читателей; роль различает их;
- `loans` хранит `librarian_id` и `renewal_count`;
- `fines` привязаны к выдаче;
- `notifications` привязаны к пользователю;
- `audit_logs` — полиморфный журнал (`entity_type`, `entity_id`, `action`, `user_id`).

### 3.3. Нефункциональные требования

- Java 26;
- Spring Boot;
- Spring Security;
- Spring Data JPA / Hibernate;
- **Flyway**;
- PostgreSQL;
- React + Vite;
- Docker + docker-compose;
- Maven;
- JJWT;
- Lombok;
- JWT с ограниченным сроком;
- BCrypt;
- валидация входных данных;
- защита от SQL-инъекций за счёт JPA;
- PDF-отчёты;
- время выполнения запросов — от 0,5 с до 2 мин в зависимости от объёма;
- восстановление после сбоя — до 2 ч / 24 ч по ТЗ;
- документация по ГОСТ 19 и ОС ТУСУР.

### 3.4. Документы и этапы

- Этап 1: концептуальная модель — до 09.10.2026.
- Этап 1.2: физическая модель и схема БД — до 20.11.2026.
- Этап 2: программная реализация и отладка — до 26.12.2026.
- Этап 3: приёмочные испытания, руководство оператора, пояснительная записка — до 26.12.2026.

---

## 4. Сопоставление старого проекта с ТЗ

| Область | Сейчас в проекте | Нужно по ТЗ/схеме | Что делать | Приоритет |
|---|---|---|---|---|
| Схема БД | Hibernate `ddl-auto=update` | Flyway, явный DDL, `validate` | Этап A1 | P0 |
| Справочники | Enum и строки | 8 таблиц-справочников + CRUD | A2, B2 | P0 |
| Пользователи | `patronimyc`, `password`, `phoneNumber`, enum role/status | `middle_name`, `password_hash`, FK role/status, без phone | A2 | P0 |
| Роли | Enum `READER/LIBRARIAN/ADMIN` | Таблица `roles` | A2 | P0 |
| Статусы | Enum | Таблицы `user_statuses`, `item_statuses`, `loan_statuses` | A2 | P0 |
| Книги | Издательство/язык/локация — строки, жанр — enum | FK на `publishers/languages/locations`, `genre_id` | A2 | P0 |
| Журналы | То же + отдельный enum жанров | Единый `genres`, FK | A2 | P0 |
| Авторы | Есть | Есть, M:N | Сохранить | P2 |
| Экземпляры | Инв. номер + enum статуса | + `acquisition_date`, `price`, `status_id` | A2, B3 | P0 |
| Выдачи | Нет `librarian_id`, `renewal_count`; enum статуса | FK, `renewal_count`, `librarian_id` | A2, B4 | P0 |
| Штрафы | Заглушка в консоли | Таблица `fines`, начисление, оплата, списание, отчёт | B5 | P1 |
| Уведомления | Нет | Таблица `notifications`, планировщик, отправка | B7 | P1 |
| Аудит | Нет | `audit_logs`, AOP, UI | B8 | P1 |
| Системные параметры | Нет | Таблица + CRUD + использование | B6 | P1 |
| Отчёты/PDF | Только топ книг | 4 отчёта + PDF | B9 | P1 |
| Админ-функции | Частично | Сотрудники, роли, блокировка, удаление, справочники, аудит, параметры, статистика | B1, B2 | P1 |
| Библиотекарь | Частично | Читатели, каталог, экземпляры, выдачи, штрафы, отчёты | B3–B5 | P0/P1 |
| Безопасность | JWT/BCrypt/валидация | Секреты в env, статусы, method security, CORS | B11 | P1 |
| Docker | Только БД | Полный стек, healthcheck, env | A1, C | P2 |
| Тесты | Unit | Интеграционные, Testcontainers, приёмочные | C | P1 |
| Документация | Нет | Модели, словарь данных, руководство, программа испытаний | C | P0 |

---

## 5. Ключевые решения

### 5.1. Flyway вместо `ddl-auto=update`

**Рекомендация:** да, переходить на Flyway.

Почему:

- ТЗ прямо требует Flyway;
- схема становится артефактом проекта, а не побочным эффектом Hibernate;
- можно показать физическую модель, ограничения, индексы и seed-данные;
- легко развернуть БД с нуля на другой машине;
- `ddl-auto=validate` ловит расхождения между сущностями и схемой.

Как:

1. Создать `src/main/resources/db/migration`.
2. Перенести DDL из `library_db.sql` в `V1__init_schema.sql`, убрав:
   - `\restrict` / `\unrestrict`;
   - `COPY ... FROM stdin` и `\.`;
   - `ALTER TABLE ... OWNER TO postgres`;
   - `SELECT pg_catalog.set_config('search_path', '', false)` (или заменить на `public`);
   - лишние `SET`.
3. Добавить Flyway-зависимость.
4. В `application.properties`:
   ```properties
   spring.flyway.enabled=true
   spring.flyway.locations=classpath:db/migration
   spring.jpa.hibernate.ddl-auto=validate
   spring.jpa.open-in-view=false
   ```
5. Для существующей БД со старой схемой — либо пересоздать её, либо использовать `baseline-on-migrate` (но лучше пересоздать, чтобы не тащить старую схему).

**Важно:** не редактировать уже применённые миграции. Любое изменение схемы — новая миграция `V5__...sql`.

### 5.2. Справочники вместо enum

**Рекомендация:** сделать JPA-сущности для справочников, а в бизнес-логике использовать строковые константы (или helper-методы), а не Java-enum.

Почему:

- схема требует FK на таблицы;
- enum нельзя напрямую замапить на FK;
- справочники должны быть редактируемыми администратором;
- добавление нового жанра/языка не должно требовать перекомпиляции.

Как:

- `Role`, `UserStatus`, `ItemStatus`, `LoanStatus`, `Genre`, `Language`, `Publisher`, `Location` — `@Entity` с `UUID id` и `String name`.
- Удалить старые enum `UserRole`, `UserStatus`, `ItemStatus`, `LoanStatus`, `BookGenre`, `MagazineGenre` (или оставить только как набор констант в отдельном классе `DictionaryCodes`).
- В сервисах сравнивать `entity.getName()` с константой:
  ```java
  if (!user.getStatus().getName().equals(DictionaryCodes.ACTIVE)) { ... }
  ```
- В DTO отдавать и `id`, и `name`, чтобы фронтенд не делал лишних запросов.

Это самая трудоёмкая часть этапа A, но без неё схема не сойдётся.

### 5.3. Читатели — это пользователи с ролью READER

В схеме нет отдельной таблицы читателей. `users` хранит всех, а `role_id` различает:

- `READER` — читатель, `password_hash` может быть `NULL`, вход запрещён;
- `LIBRARIAN` — сотрудник, может входить;
- `ADMIN` — администратор, может входить.

ТЗ говорит о разграничении доступа по ролям `LIBRARIAN`, `ADMIN` — это не противоречит наличию `READER` в справочнике ролей: читатели просто не являются пользователями системы с правом входа. В пояснительной записке это стоит явно описать.

### 5.4. Можно ли менять схему БД

**Рекомендация:** схему из курсового оставить базовой, но добавить то, что улучшает целостность и производительность:

- индексы на FK и поля отчётов;
- `CHECK`-ограничения (`due_date >= loan_date`, `return_date >= loan_date`, `price >= 0` и т.п.);
- частичный уникальный индекс, запрещающий две активные выдачи одного экземпляра:
  ```sql
  CREATE UNIQUE INDEX ux_loans_active_copy
      ON loans(copy_id)
      WHERE return_date IS NULL;
  ```
- уникальность издательств/локаций, если это нужно для справочников;
- представления для отчётов.

Это не ломает концептуальную модель, а усиливает физическую. Если преподаватель требует схему «как в дампе» без изменений — индексы и проверки всё равно можно добавить отдельной миграцией и объяснить в записке.

**Спорный момент:** `audit_logs.entity_id` имеет тип `UUID NOT NULL`. Для системных параметров ключ — `VARCHAR`, поэтому их изменение нельзя залогировать в это поле. Варианты:

1. аудировать только сущности с UUID, а параметры не аудировать (проще, но неполно);
2. добавить в `audit_logs` поля `entity_key VARCHAR(100)` и `details JSONB`, сделать `entity_id` nullable (лучше для ТЗ, но это изменение схемы).

Рекомендую вариант 2, если схему ещё можно править; иначе — вариант 1 с оговоркой в документации.

### 5.5. Отчёты: SQL-представления + PDF на Java

**Рекомендация:** отчёты строить SQL-запросами/представлениями, а PDF генерировать в Java.

Почему:

- курс по базам данных — уместно показать VIEW и сложные запросы;
- PDF — это представление, а не бизнес-логика;
- проще тестировать.

Представления:

- `v_loan_details` — выдача + читатель + экземпляр + издание + статусы;
- `v_overdue_loans` — просроченные выдачи + дни просрочки + сумма штрафа;
- `v_popular_items` — популярные издания за период;
- `v_active_readers` — активные читатели за период;
- `v_fine_debts` — задолженности по штрафам.

PDF:

- библиотека OpenPDF (или PDFBox);
- **обязательно подключить TTF-шрифт с кириллицей** (например, DejaVuSans.ttf) через `BaseFont.IDENTITY_H`, иначе русский текст в PDF превратится в кракозябры;
- эндпоинты `GET /api/reports/...` с `produces = application/pdf`.

### 5.6. Уведомления: БД + опциональная почта

ТЗ требует «создание и отправка уведомлений». Читатели не входят в систему, поэтому:

- создавать запись в `notifications`;
- отправлять email через `JavaMailSender`, если SMTP настроен;
- если SMTP не настроен — писать в лог и считать, что уведомление создано;
- для демонстрации удобно поднять MailHog в `docker-compose` и смотреть письма в веб-интерфейсе.

Не отправлять письмо каждый день по одной и той же просрочке — это спам. Рекомендуемая логика:

- уведомление при переходе выдачи в `OVERDUE`;
- повторное напоминание раз в 7 дней;
- уведомление о начисленном штрафе.

### 5.7. PDF и кириллица

OpenPDF по умолчанию использует Helvetica, которая не поддерживает кириллицу. Нужно:

- положить `DejaVuSans.ttf` (и, при необходимости, `DejaVuSans-Bold.ttf`) в `src/main/resources/fonts`;
- зарегистрировать шрифт:
  ```java
  BaseFont baseFont = BaseFont.createFont(
      "fonts/DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
  ```
- использовать этот шрифт во всех `Font`.

### 5.8. Electron

ТЗ требует **веб-клиент в браузере**. Electron — необязательная обёртка, которая не даёт плюсов для курсового и добавляет проблем со сборкой. Рекомендую:

- основной сценарий — `npm run dev` + браузер;
- Electron оставить в проекте как «опционально», но не тратить на него время и не включать в демонстрацию.

### 5.9. Версия PostgreSQL

- дамп снят с 15.18;
- на схеме подписано «Postgres 18»;
- ТЗ просто говорит «PostgreSQL».

Рекомендую зафиксировать **PostgreSQL 16 или 18** в `docker-compose` и в записке. Если преподаватель ориентируется на картинку — 18; если на дамп — 15+. Принципиальной разницы для этого проекта нет, но версию нужно указать явно.

### 5.10. Имя пакета и продукта

Пакет `com.example.lims` можно оставить — переименование в `ru.tusur.library` добавит много механической работы и не влияет на оценку. В документации использовать название из ТЗ: «Автоматизированная система управления библиотекой». В коде допустимо оставить LIMS как краткое имя.

---

## 6. Этап 0 — подготовка и страховка (0,5–1 день)

### Задачи

1. **Зафиксировать текущее состояние git.**
   ```powershell
   cd "D:\Учёба\MyProjects\lims"
   git status
   git add -A
   git commit -m "chore: snapshot before coursework refactor"
   git tag pre-coursework
   git checkout -b coursework
   ```
   Если не хотите коммитить всё — сделайте `git stash`, но не потеряйте пользовательские изменения.

2. **Почистить репозиторий.**
   - удалить из индекса `target/`, `node_modules/`, `.idea/`, `hs_err_pid*.log`, `replay_pid*.log`;
   - проверить `.gitignore`;
   - логи JVM-краша сохранить вне репозитория, если они нужны для разбора.

3. **Поднять окружение.**
   - запустить Docker Desktop;
   - `docker compose up -d`;
   - проверить `docker ps` и подключение к `localhost:5432`;
   - проверить, что `.\mvnw.cmd -DskipTests package` собирается (сейчас собирается успешно);
   - установить Node.js/npm, если их нет в PATH, либо использовать существующий `frontend/node_modules`.

4. **Снять baseline.**
   - запустить backend и frontend на старой схеме;
   - проверить вход, каталог, выдачу, возврат;
   - сделать скриншоты для пояснительной записки;
   - сохранить список текущих эндпоинтов.

5. **Решить судьбу старых данных.**
   - рекомендую **пересоздать БД** `lims` с нуля: старые данные — демо, а схема всё равно несовместима;
   - если данные нужны — сделать `pg_dump` и хранить отдельно, но не тащить их в новую схему без маппинга.

### Результат этапа

- отдельная ветка `coursework`;
- чистое рабочее дерево;
- работающий старый проект;
- понятный baseline для сравнения.

---

## 7. Этап A — приведение к общему виду (1,5–2 недели)

**Цель:** приложение запускается на новой схеме БД, все старые функции работают, `ddl-auto=validate` проходит. Новые функции ТЗ пока не добавляем (кроме минимально необходимых справочников и seed).

### A1. Фундамент БД и Flyway

**Задачи:**

1. Создать структуру:
   ```
   src/main/resources/db/migration/
     V1__init_schema.sql
     V2__indexes_constraints_views.sql
     V3__dictionaries.sql
     V4__demo_data.sql
   ```

2. Подготовить `V1__init_schema.sql`:
   - взять DDL из `library_db.sql`;
   - убрать psql-мета-команды и `COPY`;
   - убрать `OWNER TO postgres`;
   - оставить `CREATE TABLE`, `PRIMARY KEY`, `FOREIGN KEY`, `UNIQUE`, `CHECK`;
   - проверить порядок создания таблиц (сначала справочники, потом `users`, `library_items`, `books`, `magazines`, `item_copies`, `loans`, `fines`, `notifications`, `audit_logs`).

3. Подготовить `V2__indexes_constraints_views.sql`:
   - индексы на FK;
   - индексы на поля поиска и отчётов;
   - частичный уникальный индекс на активную выдачу;
   - `CHECK`-ограничения;
   - представления для отчётов.

   Примерный набор индексов:
   ```sql
   CREATE INDEX ix_users_role_id ON users(role_id);
   CREATE INDEX ix_users_status_id ON users(status_id);
   CREATE INDEX ix_users_name ON users(last_name, first_name);

   CREATE INDEX ix_library_items_title ON library_items(title);
   CREATE INDEX ix_library_items_language_id ON library_items(language_id);
   CREATE INDEX ix_library_items_publisher_id ON library_items(publisher_id);
   CREATE INDEX ix_library_items_location_id ON library_items(location_id);

   CREATE INDEX ix_books_genre_id ON books(genre_id);
   CREATE INDEX ix_magazines_genre_id ON magazines(genre_id);

   CREATE INDEX ix_item_authors_author_id ON item_authors(author_id);
   CREATE INDEX ix_item_copies_item_id ON item_copies(item_id);
   CREATE INDEX ix_item_copies_status_id ON item_copies(status_id);

   CREATE INDEX ix_loans_user_id ON loans(user_id);
   CREATE INDEX ix_loans_copy_id ON loans(copy_id);
   CREATE INDEX ix_loans_status_id ON loans(status_id);
   CREATE INDEX ix_loans_due_date ON loans(due_date);
   CREATE INDEX ix_loans_loan_date ON loans(loan_date);

   CREATE INDEX ix_fines_loan_id ON fines(loan_id);
   CREATE INDEX ix_fines_is_paid ON fines(is_paid);

   CREATE INDEX ix_notifications_user_id ON notifications(user_id);
   CREATE INDEX ix_notifications_sent_at ON notifications(sent_at);

   CREATE INDEX ix_audit_logs_entity ON audit_logs(entity_type, entity_id);
   CREATE INDEX ix_audit_logs_changed_at ON audit_logs(changed_at);
   CREATE INDEX ix_audit_logs_user_id ON audit_logs(user_id);

   CREATE UNIQUE INDEX ux_loans_active_copy
       ON loans(copy_id)
       WHERE return_date IS NULL;

   ALTER TABLE loans
       ADD CONSTRAINT ck_loans_due_date
       CHECK (due_date >= loan_date);

   ALTER TABLE loans
       ADD CONSTRAINT ck_loans_return_date
       CHECK (return_date IS NULL OR return_date >= loan_date);
   ```

   Для поиска по названию с `ILIKE '%...%'` обычный B-tree не поможет. Если данных много, можно добавить `pg_trgm` и GIN-индекс:
   ```sql
   CREATE EXTENSION IF NOT EXISTS pg_trgm;
   CREATE INDEX ix_library_items_title_trgm
       ON library_items USING gin (title gin_trgm_ops);
   ```
   Но для учебного объёма это необязательно; главное — уметь объяснить.

4. Подготовить `V3__dictionaries.sql`:
   - роли: `READER`, `LIBRARIAN`, `ADMIN`;
   - статусы пользователей: `ACTIVE`, `FROZEN`, `BLOCKED`, `DELETED`;
   - статусы экземпляров: `AVAILABLE`, `BORROWED`, `UNDER_REPAIR`, `LOST`, `WITHDRAWN`;
   - статусы выдач: `ACTIVE`, `RETURNED`, `RENEWED`, `OVERDUE`, `LOST`;
   - жанры (единый список);
   - языки;
   - издательства;
   - локации;
   - системные параметры:
     - `loan_days = 14`;
     - `renewal_days = 14`;
     - `max_renewals = 2`;
     - `fine_per_day = 10.00`;
     - `notification_email_enabled = false`.

   Использовать `INSERT ... ON CONFLICT (name) DO NOTHING` или `WHERE NOT EXISTS`, чтобы миграция была идемпотентной.

5. Подготовить `V4__demo_data.sql` (опционально):
   - 5–10 авторов;
   - 5–10 книг;
   - 3–5 журналов;
   - 15–20 экземпляров;
   - 5–10 читателей;
   - 1–2 библиотекаря;
   - 1 администратор;
   - 3–5 выдач, включая просроченную.
   - Пароли — BCrypt-хеши. Для демо можно использовать известный пароль, но в записке указать, что это демо-данные.

6. Настроить `application.properties`:
   ```properties
   spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/lims}
   spring.datasource.username=${DB_USER:user}
   spring.datasource.password=${DB_PASSWORD:password}
   spring.jpa.hibernate.ddl-auto=validate
   spring.jpa.open-in-view=false
   spring.flyway.enabled=true
   spring.flyway.locations=classpath:db/migration
   spring.jpa.show-sql=false
   ```
   Для разработки можно оставить `show-sql=true`, но не в финальной версии.

7. Обновить `docker-compose.yml`:
   - PostgreSQL 16/18;
   - healthcheck;
   - volume для данных;
   - при желании — сервис backend и frontend (позже).

8. Проверить:
   - удалить старую БД/том;
   - `docker compose up -d`;
   - запустить приложение;
   - убедиться, что Flyway применил миграции, Hibernate не создаёт таблицы, `validate` проходит.

**Результат A1:** чистая БД разворачивается одной командой, схема соответствует дампу, есть индексы, ограничения, справочники и демо-данные.

### A2. Приведение JPA-сущностей к новой схеме

**Новые сущности:**

- `Role` → `roles`;
- `UserStatus` → `user_statuses`;
- `ItemStatus` → `item_statuses`;
- `LoanStatus` → `loan_statuses`;
- `Genre` → `genres`;
- `Language` → `languages`;
- `Publisher` → `publishers`;
- `Location` → `locations`;
- `SystemParameter` → `system_parameters`;
- `Fine` → `fines`;
- `Notification` → `notifications`;
- `AuditLog` → `audit_logs`.

**Изменения существующих сущностей:**

| Было | Стало |
|---|---|
| `User.patronimyc` | `User.middleName`, колонка `middle_name` |
| `User.password` | `User.passwordHash`, колонка `password_hash` |
| `User.phoneNumber` | Удалить (нет в схеме) |
| `User.role` (enum) | `@ManyToOne Role role`, `role_id` |
| `User.status` (enum) | `@ManyToOne UserStatus status`, `status_id` |
| `LibraryItem.publisher` (String) | `@ManyToOne Publisher publisher`, `publisher_id` |
| `LibraryItem.location` (String) | `@ManyToOne Location location`, `location_id` |
| `LibraryItem.language` (String) | `@ManyToOne Language language`, `language_id` |
| `Book.genre` (BookGenre) | `@ManyToOne Genre genre`, `genre_id` |
| `Magazine.genre` (MagazineGenre) | `@ManyToOne Genre genre`, `genre_id` |
| `ItemCopy.status` (ItemStatus enum) | `@ManyToOne ItemStatus status`, `status_id` |
| `ItemCopy` | + `acquisitionDate`, `price` |
| `Loan.status` (LoanStatus enum) | `@ManyToOne LoanStatus status`, `status_id` |
| `Loan` | + `librarian` (`@ManyToOne User`, `librarian_id`), + `renewalCount` |

**Технические детали:**

- `Book` и `Magazine` наследуются от `LibraryItem` через `@Inheritance(strategy = JOINED)`. В схеме `books.id`/`magazines.id` — PK и FK на `library_items.id`. Hibernate это поддерживает; при необходимости добавить `@PrimaryKeyJoinColumn`.
- Для `User` использовать `@Column(name = "password_hash")`, а не `password`.
- Для `User` не оставлять `phoneNumber`, иначе `validate` выдаст ошибку.
- Для `LibraryItem` не оставлять старые строковые поля.
- Для всех `@ManyToOne` указать `@JoinColumn(name = "...")`.
- Для `Author` связь M:N через `item_authors` уже есть — сохранить.
- Для `Fine`, `Notification`, `AuditLog` создать репозитории и сервисы позже, на этапе B; на этапе A достаточно сущностей и маппинга, чтобы `validate` проходил.

**Удаление/замена enum:**

- удалить `BookGenre`, `MagazineGenre`, `UserRole`, `UserStatus`, `ItemStatus`, `LoanStatus`;
- создать `DictionaryCodes` с константами:
  ```java
  public final class DictionaryCodes {
      public static final String READER = "READER";
      public static final String LIBRARIAN = "LIBRARIAN";
      public static final String ADMIN = "ADMIN";
      public static final String ACTIVE = "ACTIVE";
      public static final String FROZEN = "FROZEN";
      public static final String BLOCKED = "BLOCKED";
      public static final String DELETED = "DELETED";
      public static final String AVAILABLE = "AVAILABLE";
      public static final String BORROWED = "BORROWED";
      public static final String UNDER_REPAIR = "UNDER_REPAIR";
      public static final String LOST = "LOST";
      public static final String WITHDRAWN = "WITHDRAWN";
      public static final String RETURNED = "RETURNED";
      public static final String RENEWED = "RENEWED";
      public static final String OVERDUE = "OVERDUE";
  }
  ```
- исправить опечатку `BORROVED` → `BORROWED` во всех местах.

**Репозитории:**

- `RoleRepository`, `UserStatusRepository`, `ItemStatusRepository`, `LoanStatusRepository`, `GenreRepository`, `LanguageRepository`, `PublisherRepository`, `LocationRepository`, `SystemParameterRepository`, `FineRepository`, `NotificationRepository`, `AuditLogRepository`.
- В существующих репозиториях заменить enum-параметры на сущности или на `String name`:
  ```java
  List<ItemCopy> findByItemIdAndStatus_Name(UUID itemId, String statusName);
  ```
  или
  ```java
  @Query("select c from ItemCopy c where c.item.id = :itemId and c.status.name = :status")
  List<ItemCopy> findByItemIdAndStatusName(@Param("itemId") UUID itemId,
                                           @Param("status") String status);
  ```
  Второй вариант проще для сервисов.

**Результат A2:** все сущности маппятся на схему, `mvnw test` компилируется, `ddl-auto=validate` проходит.

### A3. DTO и API

На этапе A нужно сохранить работоспособность старых экранов, но API неизбежно изменится: вместо строк — ID справочников.

**Пример нового запроса на создание книги:**

```json
{
  "title": "Мастер и Маргарита",
  "publisherId": "uuid",
  "publicationDate": "1967-01-01",
  "isbn": "978-5-389-01678-9",
  "locationId": "uuid",
  "languageId": "uuid",
  "genreId": "uuid",
  "pageCount": 480,
  "authorIds": ["uuid1", "uuid2"]
}
```

**Ответ:**

```json
{
  "id": "uuid",
  "title": "Мастер и Маргарита",
  "publisherId": "uuid",
  "publisherName": "Азбука",
  "publicationDate": "1967-01-01",
  "isbn": "978-5-389-01678-9",
  "locationId": "uuid",
  "locationDisplay": "Зал 1, стеллаж 2, полка A",
  "languageId": "uuid",
  "languageName": "Русский",
  "genreId": "uuid",
  "genreName": "Художественная литература",
  "pageCount": 480,
  "authors": [...],
  "totalCopies": 3,
  "availableCopies": 1
}
```

**Что сделать:**

- обновить `BookCreateDto`, `BookDto`, `MagazineCreateDto`, `MagazineDto`;
- добавить `ItemCopyCreateDto.acquisitionDate`, `price`;
- обновить `ItemCopyDto` (статус — `statusId` + `statusName`);
- обновить `LoanDto` (`statusId` + `statusName`, `librarianId`, `renewalCount`);
- обновить `UserDto` (`roleId` + `roleName`, `statusId` + `statusName`);
- обновить сервисы и контроллеры;
- временно сохранить старые URL, чтобы не ломать фронтенд; новые поля добавить постепенно.

**Результат A3:** backend компилируется и отдаёт новые DTO; фронтенд пока может падать на изменившихся полях — это нормально, фронтенд правим на этапе B10.

### A4. Seed и администратор

- Убрать `DataInitializer` из основного профиля.
- Сделать `DataInitializer` с `@Profile("demo")` или перенести демо-данные в `V4__demo_data.sql`.
- Создать администратора с BCrypt-хешем.
- Пароль администратора задавать через переменную окружения или документировать демо-пароль.
- Проверить, что роли/статусы находятся по имени, а не по хардкоду UUID.

**Результат A4:** после чистого старта можно войти как администратор, справочники заполнены.

### A5. Проверка этапа A

- `docker compose down -v && docker compose up -d`;
- `.\mvnw.cmd clean test`;
- `.\mvnw.cmd spring-boot:run`;
- вход;
- создание книги/журнала с справочниками;
- создание экземпляра;
- выдача/возврат/продление;
- поиск;
- `ddl-auto=validate` не падает.

**Definition of Done этапа A:**

- чистая БД разворачивается миграциями;
- Hibernate не создаёт таблицы;
- все старые функции работают на новой схеме;
- справочники заполнены;
- приложение собирается и запускается;
- есть базовые тесты, которые не падают.

---

## 8. Этап B — доработка до ТЗ (3–4 недели)

### B1. Пользователи, роли, администраторы

**Backend:**

- создание сотрудника (`LIBRARIAN`/`ADMIN`) с паролем;
- изменение роли;
- блокировка/разблокировка;
- заморозка читателя;
- изменение лимита выдач;
- мягкое удаление читателя (`status = DELETED`);
- запрет блокировки/удаления самого себя;
- запрет удаления последнего администратора;
- проверка статуса при входе и при выдаче.

**Семантика статусов:**

| Статус | Вход | Выдача | Отображение |
|---|---|---|---|
| `ACTIVE` | да (сотрудник) | да (читатель) | обычное |
| `FROZEN` | да (сотрудник), читатель не входит | нет | предупреждение |
| `BLOCKED` | нет | нет | блокировка |
| `DELETED` | нет | нет | скрыт из списков, история сохранена |

**Frontend:**

- раздел «Сотрудники» для администратора;
- создание/редактирование сотрудника;
- смена роли;
- блокировка;
- читатели: заморозка, блокировка, лимит, удаление.

### B2. Справочники

**Backend:**

- CRUD для 8 справочников;
- проверка ссылок перед удалением (нельзя удалить жанр, если он используется);
- защита системных записей (роли, статусы);
- пагинация/поиск не обязательны, но список должен быть.

**Frontend:**

- раздел «Справочники» с вкладками;
- добавление/редактирование/удаление;
- при удалении используемой записи — понятная ошибка.

### B3. Каталог и экземпляры

**Backend:**

- книги/журналы: создание/редактирование с `publisherId`, `languageId`, `locationId`, `genreId`, `authorIds`;
- экземпляры: создание с `acquisitionDate`, `price`; редактирование; смена статуса;
- поиск: название, автор, жанр, язык, издательство, год; пагинация;
- в результатах: общее и доступное количество экземпляров, место хранения.

**Frontend:**

- формы с выпадающими списками справочников;
- множественный выбор авторов;
- карточка издания с экземплярами, ценами, датами поступления, статусами;
- отображение доступности и локации в каталоге.

### B4. Выдачи

**Backend:**

- выдача по экземпляру;
- выдача по изданию с автовыбором доступного экземпляра;
- проверка статуса читателя;
- проверка лимита активных выдач;
- проверка статуса экземпляра;
- запись `librarian_id`;
- срок возврата из `loan_days`;
- возврат: `return_date`, статус `RETURNED`, экземпляр `AVAILABLE`, финализация штрафа;
- продление: проверка `max_renewals`, `renewal_count++`, `due_date += renewal_days`, статус `RENEWED`;
- просрочка: `OVERDUE`, если `due_date < today` и нет возврата;
- история по читателю и экземпляру.

**Конкурентность:**

- при выдаче захватывать экземпляр пессимистической блокировкой:
  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from ItemCopy c where c.id = :id")
  Optional<ItemCopy> findByIdForUpdate(@Param("id") UUID id);
  ```
- либо полагаться на частичный уникальный индекс `ux_loans_active_copy` и обрабатывать `DataIntegrityViolationException`.

**Frontend:**

- выдача из карточки издания/экземпляра;
- выбор читателя по ФИО/номеру билета, а не ввод UUID;
- возврат/продление;
- история.

### B5. Штрафы

**Backend:**

- сущность `Fine`, репозиторий, сервис, контроллер;
- начисление при возврате:
  ```
  days = max(0, returnDate - dueDate)
  amount = days * fine_per_day
  ```
- ежедневное обновление суммы для активных просрочек (неоплаченных);
- оплата: `is_paid = true`;
- списание: удаление штрафа или отдельный статус (решить и задокументировать);
- запрет оплаты до возврата (рекомендуется);
- отчёт по задолженностям.

**Рекомендуемая модель начисления:**

- одна запись `fines` на выдачу;
- `reason` = «Просрочка: N дн. × X ₽»;
- пока штраф не оплачен и выдача не возвращена — сумма пересчитывается ежедневно;
- после возврата сумма фиксируется;
- после оплаты дальнейшее начисление не производится.

Если хочется сохранить историю начислений по дням — можно создавать отдельную запись за каждый день, но тогда отчёт должен суммировать. Для курсового проще одна запись на выдачу.

**Frontend:**

- список штрафов;
- фильтры «неоплаченные/оплаченные»;
- кнопки «Оплатить», «Списать»;
- сумма задолженности читателя.

### B6. Системные параметры

**Backend:**

- CRUD (админ);
- typed getters:
  ```java
  int loanDays();
  int renewalDays();
  int maxRenewals();
  BigDecimal finePerDay();
  boolean emailEnabled();
  ```
- значения по умолчанию, если параметр отсутствует;
- валидация чисел.

**Frontend:**

- страница настроек;
- редактирование значений;
- пояснения.

### B7. Планировщик, просрочки, уведомления

**Backend:**

- `@Scheduled(cron = "0 0 3 * * *")`;
- найти активные/продлённые выдачи с `due_date < today`;
- перевести в `OVERDUE`;
- начислить/обновить штраф;
- создать уведомление;
- отправить email, если включено;
- логировать количество обработанных записей.

**Идемпотентность:**

- не создавать повторный штраф на ту же выдачу;
- не отправлять повторное уведомление каждый день (или отправлять, но с интервалом);
- проверять наличие записи за сегодня.

**Уведомления:**

- типы: `OVERDUE`, `FINE`, `DUE_SOON` (опционально);
- сообщение: читатель, издание, срок, дни просрочки, сумма;
- email через `JavaMailSender`;
- MailHog для демо.

**Frontend:**

- журнал уведомлений для администратора/библиотекаря;
- фильтры по читателю/типу/дате.

### B8. Аудит

**Backend:**

- аннотация `@Audited(entityType = "BOOK", action = "CREATE")`;
- аспект вокруг сервисных методов;
- текущий пользователь из `SecurityContext`;
- запись в `audit_logs`;
- аудировать: создание/изменение/удаление сущностей, выдачу/возврат/продление, оплату/списание штрафа, вход, изменение справочников/параметров.

**Ограничение схемы:**

- `entity_id UUID NOT NULL` не подходит для `system_parameters` (ключ — varchar).
- Варианты: расширить схему (`entity_key`, `details`) или не аудировать параметры. Рекомендую расширить, если можно.

**Frontend:**

- журнал аудита с фильтрами по дате, пользователю, типу сущности, действию;
- пагинация.

### B9. Отчёты и PDF

**Отчёты:**

1. «Популярные издания» за период:
   - издание, авторы, жанр, количество выдач;
   - сортировка по убыванию;
   - фильтр по датам.
2. «Активные читатели»:
   - ФИО, номер билета, количество выдач за период;
   - сортировка по убыванию.
3. «Задолженности по штрафам»:
   - читатель, сумма долга, количество выдач, дни просрочки;
   - только неоплаченные.
4. «Просроченные выдачи»:
   - читатель, издание, дата выдачи, срок, дни просрочки, сумма штрафа.

**Backend:**

- SQL-запросы/представления;
- DTO для отчётов;
- `ReportController`;
- `PdfReportService` с OpenPDF и DejaVuSans;
- `Content-Disposition: attachment; filename="..."`.

**Frontend:**

- страница «Отчёты»;
- выбор периода;
- кнопки «Скачать PDF»;
- предпросмотр таблицы.

### B10. Frontend

**Новые разделы:**

- `/admin/users` — сотрудники;
- `/admin/dictionaries` — справочники;
- `/admin/parameters` — системные параметры;
- `/admin/audit` — аудит;
- `/admin/reports` — отчёты;
- `/fines` — штрафы;
- `/notifications` — уведомления.

**Доработки:**

- загрузка справочников через хук/контекст;
- формы книг/журналов с dropdown;
- выбор авторов;
- карточка читателя с историей;
- выдача по читателю (поиск по ФИО/билету);
- отображение доступности;
- ролевое меню;
- обработка ошибок/загрузки;
- `VITE_API_URL` вместо хардкода;
- убрать Electron из основного сценария.

### B11. Безопасность и надёжность

- JWT-секрет в переменной окружения, не в git;
- срок жизни токена — 1–8 часов;
- проверка статуса пользователя при входе и в фильтре;
- `@PreAuthorize` для админских/библиотекарских операций;
- CORS из переменной окружения;
- валидация всех DTO;
- `open-in-view=false`;
- DTO вместо сущностей в ответах;
- ограничение прав БД-пользователя приложения (без DDL);
- backup/restore (`pg_dump`, `pg_restore`) и описание в записке;
- логирование без секретов;
- обработка `DataIntegrityViolationException` понятными сообщениями.

---

## 9. Этап C — тестирование, документация, защита (1,5–2 недели)

### C1. Тесты

- unit-тесты сервисов (Mockito) — переписать под новые сущности;
- `@DataJpaTest` + Testcontainers PostgreSQL — проверка маппинга, ограничений, индексов;
- `@SpringBootTest` + Testcontainers — проверка Flyway и контекста;
- MockMvc — проверка API и ролей;
- тест конкурентной выдачи (два запроса на один экземпляр);
- тест миграций с нуля;
- тест отчётов (данные → PDF не пустой, кириллица).

Если Testcontainers слишком тяжёл — минимум: отдельная тестовая БД PostgreSQL и `@Sql`/Flyway.

### C2. Документация

- логическая модель (отношения, атрибуты, PK/FK, кардинальности);
- физическая модель (таблицы, типы, ограничения, индексы, представления);
- словарь данных;
- обоснование 3НФ и денормализаций (аудит, уведомления);
- схема архитектуры;
- описание API (OpenAPI/Swagger);
- руководство оператора;
- программа и методика приёмочных испытаний;
- сценарий демонстрации;
- раздел безопасности;
- раздел резервного копирования/восстановления;
- трассировка требований ТЗ → реализация;
- скриншоты;
- оформление по ГОСТ 19 и ОС ТУСУР.

### C3. Приёмка и защита

Сценарий демонстрации:

1. Вход администратора.
2. Справочники: добавить жанр/язык/издательство/локацию.
3. Создать авторов, книгу, журнал.
4. Добавить экземпляры с ценой и датой поступления.
5. Зарегистрировать читателя.
6. Выдать книгу.
7. Продлить, затем сделать просрочку.
8. Показать штраф и уведомление.
9. Вернуть книгу, оплатить штраф.
10. Показать отчёты и PDF.
11. Показать журнал аудита.
12. Показать разграничение ролей (библиотекарь vs администратор).

Подготовить ответы:

- почему Flyway, а не `ddl-auto`;
- почему справочники, а не enum;
- как обеспечена 3НФ;
- какие индексы и зачем;
- как работает транзакция выдачи;
- как защищены пароли и JWT;
- как считается штраф;
- как обеспечена идемпотентность планировщика;
- как восстановить БД.

---

## 10. Календарный план

| Период | Этап | Результат |
|---|---|---|
| 25.10–31.10 | 0 + A1 | Ветка, чистая БД, Flyway, V1–V3, приложение стартует |
| 01.11–07.11 | A2 | Сущности и репозитории приведены к схеме |
| 08.11–14.11 | A3–A5 | DTO/API, seed, проверка; этап A закрыт |
| 15.11–21.11 | B1–B3 | Пользователи, справочники, каталог, экземпляры; этап 2 курсового сдан |
| 22.11–28.11 | B4–B6 | Выдачи, штрафы, системные параметры |
| 29.11–05.12 | B7–B8 | Планировщик, уведомления, аудит |
| 06.12–12.12 | B9–B10 | Отчёты, PDF, фронтенд |
| 13.12–19.12 | B11 + C | Безопасность, тесты, документация |
| 20.12–26.12 | C | Приёмка, демонстрация, защита |

**Если времени меньше:** приоритет P0 — схема, сущности, авторизация, каталог, экземпляры, выдачи. P1 — штрафы, отчёты, аудит, уведомления. P2 — косметика, Electron, дополнительные фильтры.

---

## 11. Первые шаги (что делать прямо сейчас)

1. Закоммитить текущее состояние и создать ветку `coursework`.
2. Запустить Docker Desktop и поднять PostgreSQL.
3. Создать `V1__init_schema.sql` из очищенного дампа.
4. Добавить Flyway и выставить `ddl-auto=validate`.
5. Создать `V2` (индексы/ограничения/views) и `V3` (справочники).
6. Пересобрать `User` + `Role` + `UserStatus` + `UserRepository` + `UserDetailsServiceImpl` + `AuthController` + `UserService` + DTO.
7. Проверить вход администратора на новой схеме.
8. Пересобрать `LibraryItem` + `Book` + `Magazine` + `Genre` + `Publisher` + `Language` + `Location`.
9. Проверить каталог.
10. Пересобрать `ItemCopy` + `Loan`.
11. Проверить выдачу/возврат.
12. Только после этого начинать штрафы, отчёты, аудит и уведомления.

**Почему такой порядок:** каждый шаг оставляет приложение в рабочем состоянии. Не нужно переписывать всё сразу и потом неделю искать, почему не стартует контекст.

---

## 12. Открытые вопросы

1. **Можно ли менять схему БД?** Индексы, `CHECK`, частичный уникальный индекс, `audit_logs.entity_key/details` — это улучшения, но они отличаются от дампа. Если схема уже сдана и менять нельзя, часть улучшений придётся опустить.
2. **Должны ли читатели входить в систему?** По ТЗ — нет, только сотрудники. Подтвердить.
3. **Нужны ли по курсу VIEW, триггеры, хранимые процедуры, роли/гранты?** В ТЗ этого нет, но курс называется «Организация баз данных». Если да — добавим отдельный блок.
4. **Почта: настоящая или имитация?** Для демо подойдёт MailHog; для реальной — SMTP.
5. **Нужен ли Electron?** Рекомендую нет.
6. **Какая версия PostgreSQL нужна преподавателю?** 15, 16 или 18.
7. **Сохранять ли старые данные?** Рекомендую пересоздать БД.
8. **Сдан ли этап 2 (физическая модель) и можно ли ещё править схему?**
9. **Сколько времени в неделю готовы уделять?** От этого зависит, что успеем до 26.12.

---

## 13. Как мы будем работать

- Вы пишете код, я проверяю, объясняю и даю конкретные фрагменты.
- Один шаг — один коммит.
- После каждого этапа присылайте ошибки/логи/скриншоты.
- Не переходите к следующему этапу, пока текущий не собирается и не запускается.
- Я могу:
  - разобрать ошибку;
  - предложить SQL-миграцию;
  - показать, как переписать сущность/сервис;
  - проверить API-контракт;
  - составить чек-лист приёмки;
  - помочь с текстом пояснительной записки.

---

## Приложение A. Карта таблиц и Java-сущностей

| Таблица | Java-сущность | Примечание |
|---|---|---|
| `roles` | `Role` | справочник |
| `user_statuses` | `UserStatus` | справочник |
| `item_statuses` | `ItemStatus` | справочник |
| `loan_statuses` | `LoanStatus` | справочник |
| `genres` | `Genre` | единый для книг и журналов |
| `languages` | `Language` | справочник |
| `publishers` | `Publisher` | справочник |
| `locations` | `Location` | hall/shelf/rack |
| `system_parameters` | `SystemParameter` | key/value |
| `users` | `User` | сотрудники и читатели |
| `authors` | `Author` | M:N с изданиями |
| `library_items` | `LibraryItem` | супертип |
| `books` | `Book` | наследник |
| `magazines` | `Magazine` | наследник |
| `item_authors` | — | M:N, отдельной сущности не нужно |
| `item_copies` | `ItemCopy` | экземпляры |
| `loans` | `Loan` | выдачи |
| `fines` | `Fine` | штрафы |
| `notifications` | `Notification` | уведомления |
| `audit_logs` | `AuditLog` | аудит |

---

## Приложение B. План миграций

| Миграция | Содержание |
|---|---|
| `V1__init_schema.sql` | Все таблицы, PK, FK, UNIQUE, CHECK из дампа |
| `V2__indexes_constraints_views.sql` | Индексы, дополнительные CHECK, частичный уникальный индекс, VIEW для отчётов |
| `V3__dictionaries.sql` | Роли, статусы, жанры, языки, издательства, локации, системные параметры |
| `V4__demo_data.sql` | Демо-данные для показа (опционально) |
| `V5__...` | Будущие изменения схемы (например, audit details) |

Правила:

- не менять применённые миграции;
- каждая миграция — атомарная и идемпотентная, где возможно;
- seed-данные — через `ON CONFLICT DO NOTHING`;
- для тестов — отдельная БД/контейнер;
- `flyway_schema_history` не трогать вручную.

---

## Приложение C. Чек-лист ТЗ

### Аутентификация и роли

- [ ] Регистрация читателя библиотекарем
- [ ] Создание сотрудника администратором
- [ ] Вход по email/паролю с JWT
- [ ] Роли `LIBRARIAN`, `ADMIN`
- [ ] Читатели не входят в систему
- [ ] BCrypt для паролей
- [ ] JWT с ограниченным сроком
- [ ] Проверка статуса пользователя

### Справочники

- [ ] Роли
- [ ] Статусы пользователей
- [ ] Статусы экземпляров
- [ ] Статусы выдач
- [ ] Жанры
- [ ] Языки
- [ ] Издательства
- [ ] Места хранения
- [ ] CRUD для справочников
- [ ] Защита используемых записей от удаления

### Администратор

- [ ] Создание сотрудников
- [ ] Изменение роли
- [ ] Блокировка пользователя
- [ ] Заморозка читателя
- [ ] Изменение лимита выдач
- [ ] Удаление читателя (мягкое)
- [ ] Управление справочниками
- [ ] Журнал аудита
- [ ] Системные параметры
- [ ] Полная статистика

### Библиотекарь

- [ ] Регистрация читателей
- [ ] Ведение читателей
- [ ] Создание/редактирование книг
- [ ] Создание/редактирование журналов
- [ ] Создание/редактирование авторов
- [ ] Добавление экземпляров
- [ ] Смена статуса экземпляра
- [ ] Выдача по экземпляру
- [ ] Выдача по изданию с автовыбором
- [ ] Возврат
- [ ] Продление
- [ ] Начисление штрафа
- [ ] Списание штрафа
- [ ] Отчёты по выдачам
- [ ] Отчёты по просрочкам

### Автоматизация

- [ ] Ежедневная проверка просрочек
- [ ] Начисление штрафа за день
- [ ] Создание уведомления
- [ ] Отправка уведомления (email/лог)
- [ ] Идемпотентность планировщика

### Выходные данные

- [ ] Поиск по каталогу
- [ ] Активные выдачи читателя
- [ ] Просроченные выдачи с суммой штрафа
- [ ] Отчёт «Популярные издания»
- [ ] Отчёт «Активные читатели»
- [ ] Отчёт «Задолженности по штрафам»
- [ ] PDF-выгрузка
- [ ] Количество доступных/общих экземпляров
- [ ] Место хранения

### Нефункциональные

- [ ] Java 26
- [ ] Spring Boot
- [ ] Spring Security
- [ ] Spring Data JPA/Hibernate
- [ ] Flyway
- [ ] PostgreSQL
- [ ] React + Vite
- [ ] Docker/docker-compose
- [ ] Maven
- [ ] JJWT
- [ ] Lombok
- [ ] Валидация входных данных
- [ ] Защита от SQL-инъекций
- [ ] PDF
- [ ] Индексы для производительности
- [ ] Резервное копирование/восстановление

### Документация

- [ ] Концептуальная модель
- [ ] Логическая модель
- [ ] Физическая модель
- [ ] Словарь данных
- [ ] Обоснование 3НФ
- [ ] Руководство оператора
- [ ] Программа и методика испытаний
- [ ] Пояснительная записка
- [ ] Оформление по ГОСТ 19 / ОС ТУСУР

---

## Приложение D. Что переиспользовать из старого проекта

**Брать почти без изменений:**

- `SecurityConfig` (с доработками);
- `JwtUtil`, `JwtRequestFilter`, `UserDetailsImpl`, `UserDetailsServiceImpl` (с доработками);
- `GlobalExceptionHandler`, `ErrorResponse`;
- структуру DTO и контроллеров;
- пагинацию;
- `Layout`, `PrivateRoute`, `AuthContext`;
- `Dashboard`, `CatalogScreen`, `LoansScreen` (с доработками);
- `docker-compose` (расширить);
- unit-тесты как образец.

**Переписать:**

- все сущности, связанные со справочниками;
- `DataInitializer`;
- `LoanService` (штрафы, параметры, конкурентность);
- `ItemCopyService`;
- `BookService`, `MagazineService`;
- `UserService`;
- DTO книг/журналов/выдач/пользователей;
- формы книг/журналов;
- `StatisticsScreen` (расширить).

**Удалить/не использовать:**

- enum-жанры и enum-статусы;
- `phoneNumber`;
- `patronimyc`;
- `BORROVED`;
- `System.out.printf` вместо штрафов;
- Electron из основного сценария (опционально).

---

## Приложение E. Риски и как их снизить

| Риск | Последствие | Снижение |
|---|---|---|
| Слишком большой рефакторинг сразу | Приложение не собирается, потеря времени | Идти вертикальными срезами: User → Catalog → Copies → Loans |
| Расхождение сущностей и схемы | `validate` падает | Сначала V1, потом сущности; проверять после каждого блока |
| Flyway не понимает дамп | Миграция не применяется | Очистить psql-команды и `COPY` |
| Потеря старых данных | Невозможно вернуться | `pg_dump` перед пересозданием |
| Опечатки в статусах | Справочники не находятся | `DictionaryCodes`, seed, тесты |
| PDF без кириллицы | Кракозябры в отчёте | DejaVuSans + `IDENTITY_H` |
| Двойная выдача экземпляра | Нарушение целостности | Пессимистическая блокировка + partial unique index |
| Дубли штрафов | Неверные суммы | Идемпотентный планировщик, одна запись на выдачу |
| Секреты в git | Утечка | env-переменные, `.gitignore` |
| Нехватка времени | Не сдать этап | Приоритет P0, сначала рабочая БД и основные сценарии |

---

**Итог:** старый проект — хорошая заготовка, но его нужно «пересадить» на новую схему БД. Начинайте не с новых функций, а с Flyway, справочников и сущностей. Когда приложение снова заработает на новой схеме, добавляйте штрафы, уведомления, аудит, отчёты и админку. Так вы не потеряете работоспособность и успеете к защите.
