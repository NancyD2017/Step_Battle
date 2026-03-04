# StepBattle Backend - Полная документация

## Структура проекта

```
src/main/kotlin/com/example/demo/
├── StepCatsApplication.kt          # Точка входа
├── configuration/
│   ├── SecurityConfig.kt           # Настройки безопасности
│   └── SecurityBeans.kt            # Бины для Spring Security
├── controller/
│   ├── AuthController.kt           # Аутентификация (регистрация/вход)
│   ├── UserController.kt           # Профиль пользователя
│   ├── StepController.kt           # Работа с шагами
│   ├── CatController.kt            # Магазин и коллекция котиков
│   ├── LeaderboardController.kt    # Рейтинги
│   └── GlobalExceptionHandler.kt   # Обработка ошибок
├── dto/
│   └── Dto.kt                      # Все DTO классы
├── init/
│   └── DataInitializer.kt          # Начальные данные (котики)
├── jwt/
│   └── JwtAuthenticationFilter.kt  # JWT фильтр
├── model/
│   ├── User.kt                     # Пользователь
│   ├── Cat.kt                      # Котик
│   ├── DailyStep.kt                # Шаги за день
│   └── UserCat.kt                  # Котик пользователя
├── repository/
│   ├── UserRepository.kt
│   ├── CatRepository.kt
│   ├── DailyStepRepository.kt
│   └── UserCatRepository.kt
└── service/
    ├── JwtService.kt
    ├── CustomUserDetailsService.kt
    ├── StepService.kt
    ├── CatService.kt
    └── LeaderboardService.kt
```

---

## API Endpoints

### Аутентификация (`/auth`)

| Метод | Endpoint | Описание | Тело запроса |
|-------|----------|----------|--------------|
| POST | `/auth/register` | Регистрация | `{username, email, password}` |
| POST | `/auth/login` | Вход | `{username, password}` |
| POST | `/auth/validate` | Проверка токена | Header: `Authorization: Bearer <token>` |

### Пользователь (`/api/users`)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/users/me` | Полный профиль |
| GET | `/api/users/me/brief` | Краткая информация |
| GET | `/api/users/{id}` | Профиль другого пользователя |

### Шаги (`/api/steps`)

| Метод | Endpoint | Описание | Тело запроса |
|-------|----------|----------|--------------|
| POST | `/api/steps/add` | Добавить шаги | `{steps: int}` |
| GET | `/api/steps/today` | Статистика за сегодня |
| GET | `/api/steps/week` | Статистика за неделю |
| GET | `/api/steps/snail-check` | Проверить улитку |
| PUT | `/api/steps/goal` | Установить цель | `{goal: int}` |

### Котики (`/api/cats`)

| Метод | Endpoint | Описание | Тело запроса |
|-------|----------|----------|--------------|
| GET | `/api/cats/shop` | Все котики (магазин) |
| GET | `/api/cats/available` | Доступные для покупки |
| GET | `/api/cats/my` | Мои котики |
| GET | `/api/cats/{id}` | Информация о котике |
| POST | `/api/cats/buy` | Купить котика | `{catId: long}` |
| POST | `/api/cats/upgrade` | Улучшить котика | `{userCatId: long}` |
| GET | `/api/cats/total-power` | Общая сила |
| GET | `/api/cats/strongest` | Самый сильный |

### Рейтинг (`/api/leaderboard`)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/leaderboard/weekly` | Недельный рейтинг |
| GET | `/api/leaderboard/all-time` | Общий рейтинг |
| GET | `/api/leaderboard/compare/{friendId}` | Сравнение с другом |
| GET | `/api/leaderboard/search?query=name` | Поиск пользователей |
| GET | `/api/leaderboard/my-rank?type=weekly` | Моя позиция |
| GET | `/api/leaderboard/stats` | Глобальная статистика |

---

## Примеры запросов

### 1. Регистрация

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Ответ:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "coins": 100
  },
  "message": "Registration successful! Welcome to StepBattle!"
}
```

### 2. Добавление шагов

```bash
curl -X POST http://localhost:8080/api/steps/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"steps": 5000}'
```

**Ответ:**
```json
{
  "date": "2025-08-30",
  "steps": 5000,
  "coinsEarned": 50,
  "goalReached": false,
  "progress": 62.5
}
```

### 3. Покупка котика

```bash
curl -X POST http://localhost:8080/api/cats/buy \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"catId": 1}'
```

**Ответ:**
```json
{
  "success": true,
  "message": "Поздравляем! Вы купили Барсик!",
  "cat": {
    "id": 1,
    "catId": 1,
    "name": "Барсик",
    "rarity": "common",
    "power": 5,
    "level": 1,
    "totalPower": 5
  },
  "remainingCoins": 50
}
```

---

## Игровая механика

### Шаги и монеты
- **10 монет** за каждые 1000 шагов
- **50 монет** бонус за достижение дневной цели
- Стандартная цель: 8000 шагов (можно изменить)

### Улитки
- Меньше **100 шагов** за день = улитка
- **3 улитки** за 7 дней = потеря случайного котика

### Котики

**Редкости:**
| Редкость | Сила | Цена |
|----------|------|------|
| Common | 4-6 | 40-60 |
| Uncommon | 14-18 | 180-250 |
| Rare | 30-35 | 500-600 |
| Epic | 55-65 | 950-1100 |
| Legendary | 100-130 | 3000-4000 |
| Mythic | 200-250 | 10000-15000 |

**Прокачка:**
- Базовая стоимость: 50 монет
- Каждый уровень умножает стоимость на 2
- Сила = базовая сила × уровень

---

## Запуск проекта

### 1. Клонировать репозиторий

```bash
git clone https://github.com/NancyD2017/Step_Battle.git
cd Step_Battle/demo
```

### 2. Добавить новые файлы

Скопировать все файлы из этой папки в соответствующие директории:
- `controller/*.kt` → `src/main/kotlin/com/example/demo/controller/`
- `service/*.kt` → `src/main/kotlin/com/example/demo/service/`
- и т.д.

### 3. Обновить build.gradle.kts

Заменить содержимое файла `build.gradle.kts` на новое.

### 4. Запустить

```bash
./gradlew bootRun
```

Сервер запустится на `http://localhost:8080`

### 5. Проверить

- H2 Console: `http://localhost:8080/h2-console`
    - JDBC URL: `jdbc:h2:mem:stepbattle_db`
    - User: `sa`
    - Password: (пусто)

---

## Подключение к PostgreSQL (продакшен)

1. Добавить зависимость в `build.gradle.kts`:
```kotlin
runtimeOnly("org.postgresql:postgresql")
```

2. Изменить `application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/stepbattle
    driver-class-name: org.postgresql.Driver
    username: your_username
    password: your_password
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

---

## Структура базы данных

### Таблица `users`
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER',
    coins INT DEFAULT 0,
    weekly_steps INT DEFAULT 0,
    total_steps INT DEFAULT 0,
    record_week INT DEFAULT 0,
    goal INT DEFAULT 8000,
    snail_count INT DEFAULT 0
);
```

### Таблица `cats`
```sql
CREATE TABLE cats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    rarity VARCHAR(50),
    power INT,
    price INT,
    description TEXT
);
```

### Таблица `daily_steps`
```sql
CREATE TABLE daily_steps (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    date DATE,
    steps INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Таблица `user_cats`
```sql
CREATE TABLE user_cats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    cat_id BIGINT,
    level INT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (cat_id) REFERENCES cats(id)
);
```