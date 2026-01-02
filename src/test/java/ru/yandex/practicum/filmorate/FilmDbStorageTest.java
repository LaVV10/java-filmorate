package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(FilmDbStorage.class)
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FilmDbStorage filmDbStorage;
    private int testFilmId;

    @BeforeEach
    void setUp() {
        filmDbStorage = new FilmDbStorage(jdbcTemplate);

        // Очищаем таблицу
        jdbcTemplate.update("DELETE FROM films");

        // Вставляем тестовый фильм через JDBC напрямую
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                "FilmOne", "Description1", LocalDate.of(1995, 12, 28), 120, 1);

        // Получаем ID вставленного фильма
        testFilmId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM films", Integer.class);
        System.out.println("Создан тестовый фильм с ID: " + testFilmId);
    }

    @Test
    void getFilmById_shouldReturnFilmWhenExists() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM films", Integer.class);
        System.out.println("Количество фильмов в базе: " + count);

        Integer mpaCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mpa_ratings", Integer.class);
        System.out.println("Количество MPA: " + mpaCount);

        Film film = filmDbStorage.getFilmById(testFilmId);
        assertThat(film).isNotNull();
        assertThat(film.getId()).isEqualTo(testFilmId);
        assertThat(film.getName()).isEqualTo("FilmOne");
    }

    @Test
    void getAllFilms_shouldReturnAllFilms() {
        jdbcTemplate.update(
                "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                "FilmTwo", "Description2", LocalDate.of(2000, 1, 1), 180, 2
        );

        var films = filmDbStorage.getAllFilms();
        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName).containsExactlyInAnyOrder("FilmOne", "FilmTwo");
    }

    @Test
    void createFilm_shouldInsertFilmAndSetId() {
        Film film = new Film();
        film.setName("New Film");
        film.setDescription("Cool");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(90);

        MPA mpa = new MPA();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        Film created = filmDbStorage.createFilm(film);
        assertThat(created.getId()).isPositive();

        Film fromDb = filmDbStorage.getFilmById(created.getId());
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getName()).isEqualTo("New Film");
    }

    @Test
    void updateFilm_shouldModifyExistingFilm() {
        Film film = new Film();
        film.setName("Original");
        film.setDescription("Desc");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);

        MPA mpa = new MPA();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        Film created = filmDbStorage.createFilm(film);
        created.setName("Updated Film");
        filmDbStorage.updateFilm(created);

        Film updated = filmDbStorage.getFilmById(created.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Updated Film");
    }

    @Test
    void getFilmById_shouldReturnNullWhenNotFound() {
        Film film = filmDbStorage.getFilmById(999);
        assertThat(film).isNull();
    }
}