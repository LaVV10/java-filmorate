package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Component
@Qualifier
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        MPA mpa = new MPA();
        Integer mpaId = rs.getInt("mpa_id");
        if (rs.wasNull()) {
            mpa.setId(null);
        } else {
            mpa.setId(mpaId);
        }
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        return film;
    };

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "m.id AS mpa_id, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "ORDER BY f.id";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        films.forEach(this::setGenresForFilm);
        films.forEach(this::setLikesForFilm);
        return films;
    }

    private void setLikesForFilm(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.queryForList(sql, Integer.class, film.getId());
        film.getLikes().clear();
        film.getLikes().addAll(likes);
    }

    @Override
    public Film createFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new RuntimeException("Не удалось создать фильм: не получен ID.");
        }
        film.setId(generatedId.intValue());

        insertFilmGenres(film.getId(), film.getGenres());

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден.");
        }

        // Обновляем жанры
        deleteFilmGenres(film.getId());
        insertFilmGenres(film.getId(), film.getGenres());

        return film;
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                "m.id AS mpa_id, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            setGenresForFilm(film);
            setLikesForFilm(film);
            return film;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void insertFilmGenres(int filmId, List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            if (genre != null && genre.getId() != null) {
                if (genreExists(genre.getId())) {
                    jdbcTemplate.update(sql, filmId, genre.getId());
                } else {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден.");
                }
            }
        }
    }

    private boolean genreExists(Integer genreId) {
        String sql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreId);
        return count != null && count > 0;
    }

    private void deleteFilmGenres(int filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private void setGenresForFilm(Film film) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());
        film.setGenres(genres);
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new IllegalArgumentException("Пользователь уже поставил лайк этому фильму.");
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }
}
