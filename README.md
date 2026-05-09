# Catalog Service (Rent Platform)

## Overview

`catalog-service` — микросервис каталога объявлений аренды.

Отвечает за:

* категории
* объявления (items)
* фотографии
* избранное
* модерацию
* календарь доступности
* статистику просмотров
* статусы объявлений
* рекомендации (похожие объявления)
* интеграцию с user-service (owner preview)
* интеграцию с deal-payment-service (deal-info)

---

# Tech Stack

* Java 21
* Spring Boot 4
* Spring Security (JWT Resource Server)
* Spring Data JPA
* PostgreSQL
* Flyway
* MapStruct
* RestClient
* Swagger (OpenAPI)
* Docker

---

## Ports

| Service      | Port |
|-------------|------|
| Gateway     | 8080 |
| Catalog     | 8082 |
| User        | 8081 |
| Deal-Payment | 8083 

---

# Base URL

Через gateway:

```
/api/catalog
```

---

# Domain Model

## Category

Иерархическая структура:

```
Электроника
 └── Фототехника
```
- id (BIGSERIAL)
- categoryName, slug
- parent (self-reference)
- sortOrder, isActive
---

## Item

Основная сущность объявления.

Ключевые поля:

* id (UUID)
* ownerId
* category
* title
* itemDescription
* pricePerDay
* pricePerHour
* depositAmount
* city
* pickupLocation
* status
* moderationComment
* viewsCount
* photos
* isFavorite
* createdAt, updatedAt, deletedAt

---

## Photo

* photoUrl
* sortOrder

Главное фото = минимальный sortOrder

---

## Favorite

Связь:

```
(user_id, item_id)
```

Ограничение:

* один item нельзя добавить дважды

---

### Availability

Календарь доступности товара.

- itemId + availableDate (составной ключ)
- isAvailable

Владелец отмечает дни, когда вещь доступна для аренды.

### ItemView

Фиксация просмотров для защиты от накрутки.

- id (BIGSERIAL)
- itemId, viewerId
- viewedAt

---


# Item Lifecycle

```
DRAFT
 ↓
MODERATION
 ↓      ↘
ACTIVE   REJECTED
 ↓         ↓
ARCHIVED  DRAFT
 ↓
DRAFT
```

---

# Public Endpoints

Работают без авторизации.

---

## Get categories

```
GET /categories
GET /categories/{categoryId}
```

---

## Объявления (поиск + фильтры)

```
GET /items
```

Фильтры:

* categoryId
* city
* query
* minPricePerDay
* maxPricePerDay
* minPricePerHour
* maxPricePerHour
* page
* size

---

## Карточка объявления

```
GET /items/{itemId}
```

Особенности:

- публичный просмотр
- если пользователь авторизован → возвращает `isFavorite`
- владелец, модератор, admin, super_admin видят `viewsCount`
- гость увеличивает счётчик просмотров

---

## Похожие объявления

```
GET /items/{itemId}/similar
```

Логика:

* та же категория
* тот же город
* статус ACTIVE
* исключается текущий item

Пагинация:

```
?page=0&size=10
```

---

## Календарь доступности
`GET /items/{itemId}/availability?startDate=...&endDate=...

- гость видит только для ACTIVE товаров
- владелец видит в любом статусе

## Информация для сделки`

`GET /items/{itemId}/deal-info`

Используется deal-payment-service при создании сделки.

## Фотографии
`GET /items/{itemId}/photos`

Публичный просмотр фотографий объявления.

## Рейтинг товара

Рейтинг запрашивается из deal-payment-service:
`GET /api/reviews/items/{itemId}/summary`

---

# Auth Required Endpoints

Требуют JWT.

---

## Создание объявления

```
POST /items
```

Создаёт в статусе:

```
DRAFT
```

---

## Мои объявления

```
GET /my/items
```

Фильтр по статусу:

```
?status=ACTIVE
```

---

## Обновление объявления

```
PUT /items/{itemId}
```

Только владелец.

---

## Удаление объявления

```
DELETE /items/{itemId}
```

Soft delete. Только владелец.


## Статистика объявления
```
GET /my/items/{itemId}/stats
```

Возвращает `viewsCount`. Только для владельца.

---

# Status Management

---

## Отправка на модерацию

```
POST /items/{itemId}/send-to-moderation
```
Проверяет обязательные поля: категория, заголовок, город, pickupLocation, цена, фото.

---

## Возврат из REJECTED в DRAFT

```
POST /items/{itemId}/return-to-draft
```

---

## Архивирование

```
POST /items/{itemId}/archive
```
Только для ACTIVE.

---

## Восстановление из архива в draft

```
POST /items/{itemId}/restore
```


---

# Moderation (Moderator, Admin, Super Admin)

---

## Очередь модерации

```
GET /admin/items/moderation
```

---

## Одобрение

```
POST /admin/items/{itemId}/approve
```

---

## Отклонение

```
POST /admin/items/{itemId}/reject
```

## Календарь доступности

### Настройка календаря (владелец)
```
PUT /items/{itemId}/availability
```

```json
{
  "slots": [
    {"date": "2026-05-10", "isAvailable": true},
    {"date": "2026-05-11", "isAvailable": false}
  ]
}
```
Только для статусов DRAFT и ACTIVE. 
Нельзя менять прошедшие даты. Повторная установка того же статуса запрещена.

### Удаление слотов

```
DELETE /items/{itemId}/availability
```

```json
{
"startDate": "2026-05-01",
"endDate": "2026-05-31"
}
```

---

# Photos API

---

## Добавление фото

```
POST /items/{itemId}/photos
```

Ограничения:

* нельзя дублировать URL

---

## Переупорядочивание

```
PUT /items/{itemId}/photos/order
```

---

## Удаление фото

```
DELETE /items/{itemId}/photos/{photoId}
```

---

# Favorites

---

## Добавить в избранное

```
POST /favorites/{itemId}
```

Ошибка если уже добавлено.

---

## Удалить из избранного

```
DELETE /favorites/{itemId}
```

---

## Проверить

```
GET /favorites/{itemId}/status
```

---

## Моё избранное

```
GET /favorites/my
```

---

# Интеграции
## User Service

```
GET /api/users/{userId}/public
```

Возвращает nickname, avatarUrl, overallRating владельца.

## Deal-Payment Service

```
GET /api/catalog/items/{itemId}/deal-info
```
Вызывается из deal-payment-service для получения информации о товаре при создании сделки.

---

## Роли и доступ к просмотрам

| Роль                      | Видит viewsCount |
|--------------------------|:---:|
| Гость                     | ❌ |
| Пользователь (не владелец) | ❌ |
| Владелец                  | ✅ |
| moderator                 | ✅ |
| admin                     | ✅ |
| super_admin               | ✅ |

---

## Environment Variables

| Переменная               | Описание                  | По умолчанию |
|--------------------------|--------------------------|-------------|
| PG_HOST                  | PostgreSQL хост           | localhost   |
| PG_PORT                  | PostgreSQL порт           | 5433        |
| PG_DATABASE              | Имя БД                   | catalog_db  |
| PG_USER                  | Пользователь БД           | postgres    |
| PG_PASSWORD              | Пароль БД                | 12345       |

---

# Error Handling

## 400

* invalid JSON
* invalid status
* duplicate favorite
* duplicate photo
* validation errors

## 403

* access denied

## 404

* not found

## 500

* unexpected error

---

# Pagination

Используется Spring Pageable:

```
?page=0&size=10
```

Сортировка:

```
?sort=pricePerDay,asc
```

---

# Run

## Build

```
./gradlew build -x test
```

## Docker

```
docker compose up --build
```

---

## MVP Features

- Каталог с поиском и фильтрами
- Категории (иерархические)
- Фотографии (CRUD + порядок)
- Избранное
- Модерация (approve/reject)
- Статусы объявлений (DRAFT → MODERATION → ACTIVE → ARCHIVED)
- Похожие объявления
- Календарь доступности от арендодателя
- Статистика просмотров (защита от накрутки)
- Роли: user, moderator, admin, super_admin
- Owner preview (интеграция с user-service)
- Deal-info (интеграция с deal-payment-service)