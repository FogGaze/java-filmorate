package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    public Film addFilm(Film film);

    public Film updateFilm(Film film);

    public void deleteFilm(long id);

    public Collection<Film> getAllFilms();

    public Film getFilmById(long id);

    public void clearStorage();
}
