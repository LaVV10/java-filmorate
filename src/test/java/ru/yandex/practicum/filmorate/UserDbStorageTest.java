package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(UserDbStorage.class)
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserDbStorage userDbStorage;

    @BeforeEach
    void setUp() {
        userDbStorage = new UserDbStorage(jdbcTemplate);

        // Очищаем таблицы перед каждым тестом
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void getAllUsers_shouldReturnAllUsersFromDatabase() {
        // Создаём тестовых пользователей
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user1@filmorate.ru", "user1", "UserOne", LocalDate.of(1995, 12, 28));
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user2@filmorate.ru", "user2", "UserTwo", LocalDate.of(1990, 5, 15));
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "user3@filmorate.ru", "user3", "UserThree", LocalDate.of(1985, 1, 1));

        List<User> users = userDbStorage.getAllUsers();

        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getEmail).contains("user1@filmorate.ru", "user2@filmorate.ru", "user3@filmorate.ru");
    }

    @Test
    void createUser_shouldInsertUserIntoDatabase() {
        User user = User.builder()
                .email("new@user.ru")
                .login("newuser")
                .name("New User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userDbStorage.createUser(user);

        assertThat(created.getId()).isPositive();
        User fromDb = userDbStorage.getUserById(created.getId());
        assertThat(fromDb).isNotNull();
        assertThat(fromDb.getEmail()).isEqualTo("new@user.ru");
        assertThat(fromDb.getLogin()).isEqualTo("newuser");
    }

    @Test
    void updateUser_shouldUpdateExistingUser() {
        // Сначала создаём пользователя через storage
        User user = User.builder()
                .email("update@user.ru")
                .login("updater")
                .name("To Update")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userDbStorage.createUser(user);
        int id = created.getId();

        // Обновляем
        created.setName("Updated Name");
        userDbStorage.updateUser(created);

        User updated = userDbStorage.getUserById(id);
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void getUserById_shouldReturnUserWhenExists() {
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "find@me.ru", "finder", "FoundUser", LocalDate.of(1980, 1, 1));
        Integer id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM users", Integer.class);

        User user = userDbStorage.getUserById(id);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getLogin()).isEqualTo("finder");
    }

    @Test
    void getUserById_shouldReturnNullWhenNotExists() {
        User user = userDbStorage.getUserById(999);

        assertThat(user).isNull();
    }

    @Test
    void addFriend_shouldCreateFriendship() {
        // Создаём пользователей через storage, чтобы получить правильные ID
        User user1 = userDbStorage.createUser(User.builder()
                .email("u1@f.ru").login("u1").birthday(LocalDate.now()).build());
        User user2 = userDbStorage.createUser(User.builder()
                .email("u2@f.ru").login("u2").birthday(LocalDate.now()).build());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        List<Integer> friends = userDbStorage.getUserFriends(user1.getId());

        assertThat(friends).containsExactly(user2.getId());
    }

    @Test
    void removeFriend_shouldDeleteFriendship() {
        User user1 = userDbStorage.createUser(User.builder()
                .email("u1@f.ru").login("u1").birthday(LocalDate.now()).build());
        User user2 = userDbStorage.createUser(User.builder()
                .email("u2@f.ru").login("u2").birthday(LocalDate.now()).build());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.removeFriend(user1.getId(), user2.getId());

        List<Integer> friends = userDbStorage.getUserFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void getCommonFriends_shouldReturnCommonFriends() {
        User user1 = userDbStorage.createUser(User.builder()
                .email("u1@f.ru").login("u1").birthday(LocalDate.now()).build());
        User user2 = userDbStorage.createUser(User.builder()
                .email("u2@f.ru").login("u2").birthday(LocalDate.now()).build());
        User commonFriend = userDbStorage.createUser(User.builder()
                .email("cf@f.ru").login("cf").birthday(LocalDate.now()).build());

        userDbStorage.addFriend(user1.getId(), commonFriend.getId());
        userDbStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> common = userDbStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(common).hasSize(1);
        assertThat(common.get(0).getId()).isEqualTo(commonFriend.getId());
    }
}
