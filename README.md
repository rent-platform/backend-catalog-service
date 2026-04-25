# Catalog Service (Rent Platform)

## Overview

`catalog-service` — микросервис каталога объявлений аренды.

Отвечает за:

* категории
* объявления (items)
* фотографии
* избранное
* модерацию
* статусы объявлений
* рекомендации (похожие объявления)
* интеграцию с user-service (owner preview)

---

# Tech Stack

* Java 21
* Spring Boot
* Spring Security (JWT Resource Server)
* Spring Data JPA
* PostgreSQL
* Flyway
* MapStruct
* OpenFeign
* Swagger (OpenAPI)
* Docker

---

# Ports

| Service | Port |
| ------- | ---- |
| Gateway | 8080 |
| Catalog | 8082 |
| User    | 8081 |

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

---

## Item

Основная сущность объявления.

Ключевые поля:

* id (UUID)
* ownerId
* category
* title
* description
* pricePerDay
* pricePerHour
* deposit
* city
* pickupLocation
* status
* photos
* isFavorite

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
```

---

## Get items (поиск + фильтры)

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

## Get item (карточка)

```
GET /items/{itemId}
```

Особенности:

* доступен без авторизации
* если пользователь авторизован → возвращает isFavorite

---

## Similar items

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

# Auth Required Endpoints

Требуют JWT.

---

## Create item

```
POST /items
```

Создаёт в статусе:

```
DRAFT
```

---

## My items

```
GET /my/items
```

Фильтр по статусу:

```
?status=ACTIVE
```

---

## Update item

```
PUT /items/{itemId}
```

---

## Delete item

```
DELETE /items/{itemId}
```

Soft delete.

---

# Status Management

---

## Send to moderation

```
POST /items/{itemId}/send-to-moderation
```

---

## Return rejected → draft

```
POST /items/{itemId}/return-to-draft
```

---

## Archive

```
POST /items/{itemId}/archive
```

---

## Restore archived → draft

```
POST /items/{itemId}/restore
```

---

# Moderation (Admin)

---

## Moderation queue

```
GET /admin/items/moderation
```

---

## Approve

```
POST /admin/items/{itemId}/approve
```

---

## Reject

```
POST /admin/items/{itemId}/reject
```

---

# Photos API

---

## Add photo

```
POST /items/{itemId}/photos
```

Ограничения:

* нельзя дублировать URL

---

## Get photos

```
GET /items/{itemId}/photos
```

---

## Reorder

```
PUT /items/{itemId}/photos/order
```

---

## Delete photo

```
DELETE /items/{itemId}/photos/{photoId}
```

---

# Favorites

---

## Add

```
POST /favorites/{itemId}
```

Ошибка если уже добавлено.

---

## Remove

```
DELETE /favorites/{itemId}
```

---

## Check

```
GET /favorites/{itemId}/status
```

---

## My favorites

```
GET /favorites/my
```

---

# Owner Preview

Через user-service:

```
GET /api/users/{userId}/public
```

Возвращает:

* nickname
* avatarUrl
* rating

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

# MVP Features

✅ Каталог

✅ Категории

✅ Фото

✅ Избранное

✅ Модерация

✅ Статусы

✅ Фильтры

✅ Похожие объявления

✅ Owner preview

---

# Future Improvements

* S3 storage
* роли ADMIN/MODERATOR
* рейтинги и отзывы
* бронирование
* Elasticsearch

---

# Notes

* Публичные endpoints поддерживают работу без JWT
* Если JWT есть → возвращается isFavorite
* Все изменения статусов строго валидируются

---