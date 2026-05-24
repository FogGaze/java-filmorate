# java-filmorate

## База данных
Диаграмма базы данных (db.png) расположена в корне проекта

### Схема Базы данных

таблица пользователей
user { 
    user_id int PK
    email varchar UNIQUE
    login varchar
    name varchar
    birthday date
}

таблица фильмов
film {
    film_id int PK
    name varchar
    description text
    release_date date
    duration int
    rating_id int FK >- mpa_ratings.rating_id
}

рейтинги MPA
mpa_ratings {
    rating_id int PK
    code varchar
    description text
}

жанры
genres {
    genre_id int PK
    name varchar
    description text
}

связь фильмов с жанрами
film_genres {
    film_id int PK FK >- film.film_id
    genre_id int PK FK >- genres.genre_id
}

дружба между пользователями с указанием статуса
user_friends {
    user_id int PK FK >- user.user_id
    friend_id int PK FK >- user.user_id
    status enum('unconfirmed','confirmed')
}

лайки фильмов
film_likes {
    user_id int PK FK >- user.user_id
    film_id int PK FK >- film.film_id
}

### Примеры запросов

#### Получить все фильмы с их рейтингом и списком жанров
SELECT f.*,
    r.code AS mpa_rating,
    g.name AS genres
FROM film f
LEFT JOIN mpa_ratings r ON f.rating_id = r.rating_id
LEFT JOIN film_genres fg ON f.film_id = fg.film_id
LEFT JOIN genres g ON fg.genre_id = g.genre_id
GROUP BY f.film_id;

#### Топ-10 популярных фильмов по количеству лайков
SELECT f.film_id, f.name, COUNT(fl.user_id) AS likes_count
FROM film f
LEFT JOIN film_likes fl ON f.film_id = fl.film_id
GROUP BY f.film_id
ORDER BY likes_count DESC
LIMIT 10;

#### Найти общих друзей для двух пользователей
SELECT u.*
FROM user u
JOIN user_friends uf1 ON ((uf1.user_id = 1 AND uf1.friend_id = u.user_id) OR (uf1.friend_id = 1 AND uf1.user_id = u.user_id))
JOIN user_friends uf2 ON ((uf2.user_id = 2 AND uf2.friend_id = u.user_id) OR (uf2.friend_id = 2 AND uf2.user_id = u.user_id))
WHERE uf1.status = 'confirmed' AND uf2.status = 'confirmed';