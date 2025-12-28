package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
@Primary
@Qualifier("mpaDbStorage")
public class MPADbStorage {

    private final JdbcTemplate jdbcTemplate;

    public MPADbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MPA> mpaRowMapper = new RowMapper<MPA>() {
        @Override
        public MPA mapRow(ResultSet rs, int rowNum) throws SQLException {
            MPA mpa = new MPA();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            mpa.setDescription(rs.getString("description"));
            return mpa;
        }
    };

    public List<MPA> getAllMPA() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        List<MPA> mpas = jdbcTemplate.query(sql, mpaRowMapper);
        log.info("Загружено MPA: {}", mpas);
        return mpas;
    }

    public MPA getMPAById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, mpaRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA с id " + id + " не найден.");
        }
    }
}
