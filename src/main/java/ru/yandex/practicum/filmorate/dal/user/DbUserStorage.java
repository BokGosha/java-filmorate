package ru.yandex.practicum.filmorate.dal.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.user.LikeDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dal.BaseRepository;

import java.util.*;

@Slf4j
@Repository
public class DbUserStorage extends BaseRepository<User> implements UserStorage {

    private static final String FIND_ALL_QUERY =
            "SELECT * " +
                    "FROM users";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * " +
                    "FROM users " +
                    "WHERE id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO users (name, login, email, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE users SET login = ?, name = ?, email = ?, birthday = ? " +
                    "WHERE id = ?";
    private static final String INSERT_FRIEND_WITH_STATUS =
            "INSERT INTO user_friends (user_id, friend_id, friend_status) VALUES (?, ?, ?)";
    private static final String FIND_FRIENDS_QUERY =
            "SELECT u.* " +
                    "FROM users u " +
                    "JOIN user_friends uf ON (uf.user_id = ? AND u.id = uf.friend_id) " +
                    "  OR (uf.friend_id = ? AND u.id = uf.user_id AND uf.friend_status = 'CONFIRMED')";
    private static final String SELECT_FRIEND_STATUS =
            "SELECT friend_status " +
                    "FROM user_friends " +
                    "WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_FRIEND_QUERY =
            "DELETE FROM user_friends " +
                    "WHERE user_id = ? AND friend_id = ?";
    private static final String DOWNGRADE_TO_UNCONFIRMED_QUERY =
            "UPDATE user_friends SET friend_status = 'UNCONFIRMED' " +
                    "WHERE user_id = ? AND friend_id = ? AND friend_status = 'CONFIRMED'";
    private static final String FIND_COMMON_FRIENDS_QUERY =
            "SELECT u.* " +
                    "FROM users u " +
                    "JOIN user_friends uf1 ON uf1.friend_id = u.id " +
                    "JOIN user_friends uf2 ON uf2.friend_id = u.id " +
                    "WHERE uf1.user_id = ? AND uf2.user_id = ? ";
    private static final String UPDATE_STATUS_CONFIRMED =
            "UPDATE user_friends SET friend_status = 'CONFIRMED' " +
                    "WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_USER =
            "DELETE FROM users WHERE id = ?";
    private static final String FIND_BY_FILM_ID_QUERY =
            "SELECT u.* " +
                    "FROM users u " +
                    "JOIN film_likes fl ON u.id = fl.user_id " +
                    "WHERE fl.film_id = ?";
    private static final String FIND_FRIENDS_FOR_USER_IDS_QUERY =
            "SELECT uf.user_id, u.* " +
                    "FROM user_friends uf " +
                    "JOIN users u ON u.id = uf.friend_id " +
                    "WHERE uf.user_id IN (%s)";
    private static final String FIND_LIKES_FOR_FILM_IDS_QUERY =
            "SELECT fl.film_id, u.id, u.login " +
                    "FROM film_likes fl " +
                    "JOIN users u ON fl.user_id = u.id " +
                    "WHERE fl.film_id IN (%s)";

    public DbUserStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Set<User> findAllFriends(long userId) {
        Set<User> friends = new HashSet<>(findMany(FIND_FRIENDS_QUERY, userId, userId));
        log.info("Друзья пользователя с id={} получены, кол-во друзей: {}", userId, friends.size());
        return friends;
    }

    @Override
    public List<User> findAllLikes(long filmId) {
        return findMany(FIND_BY_FILM_ID_QUERY, filmId);
    }

    @Override
    public Map<Long, Set<LikeDto>> findLikesForFilmsIds(List<Long> filmIds) {
        String placeholders = setPlaceholders(filmIds.size());
        String sql = FIND_LIKES_FOR_FILM_IDS_QUERY.formatted(placeholders);

        return jdbc.query(sql, rs -> {
            Map<Long, Set<LikeDto>> result = new HashMap<>();
            while (rs.next()) {
                long filmId = rs.getLong("film_id");
                LikeDto like = new LikeDto();
                like.setId(rs.getLong("id"));
                like.setLogin(rs.getString("login"));
                result.computeIfAbsent(filmId, id -> new HashSet<>()).add(like);
            }
            return result;
        }, filmIds.toArray());
    }

    @Override
    public Map<Long, Set<User>> findFriendsForUserIds(List<Long> userIds) {
        String placeholders = setPlaceholders(userIds.size());
        String sql = FIND_FRIENDS_FOR_USER_IDS_QUERY.formatted(placeholders);

        return jdbc.query(sql, rs -> {
            Map<Long, Set<User>> result = new HashMap<>();

            while (rs.next()) {
                long ownerId = rs.getLong("user_id");
                User friend = mapper.mapRow(rs, rs.getRow());
                result.computeIfAbsent(ownerId, id -> new HashSet<>()).add(friend);
            }
            return result;
        }, userIds.toArray());
    }

    private String setPlaceholders(int size) {
        return String.join(", ", Collections.nCopies(size, "?"));
    }

    @Override
    public Set<User> findCommonFriends(long user1Id, long user2Id) {
        List<User> friends = findMany(FIND_COMMON_FRIENDS_QUERY, user1Id, user2Id);

        log.info("Общие друзья пользователей с id={} и id={} получены, количество друзей: {}",
                user1Id, user2Id, friends.size());
        return new HashSet<>(friends);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
        jdbc.update(DOWNGRADE_TO_UNCONFIRMED_QUERY, friendId, userId);

        log.info("Пользователь {} удалил из друзей {}: связь {}->{} удалена, {}->{} переведён в UNCONFIRMED (если был CONFIRMED)",
                userId, friendId, userId, friendId, friendId, userId);
    }

    @Override
    public void saveFriend(long userId, long friendId) {
        List<String> statuses = jdbc.query(
                SELECT_FRIEND_STATUS,
                (rs, rowNum) -> rs.getString("friend_status"),
                friendId, userId
        );

        if (!statuses.isEmpty()) {
            String status = statuses.getFirst();
            if (!"CONFIRMED".equalsIgnoreCase(status)) {
                jdbc.update(UPDATE_STATUS_CONFIRMED, friendId, userId);
                int updated = jdbc.update(UPDATE_STATUS_CONFIRMED, userId, friendId);
                if (updated == 0) {
                    update(INSERT_FRIEND_WITH_STATUS, userId, friendId, "CONFIRMED");
                }
                log.info("Дружба {} <-> {} подтверждена", userId, friendId);
            } else {
                log.info("Дружба {} <-> {} уже CONFIRMED", userId, friendId);
            }
        } else {
            jdbc.update(INSERT_FRIEND_WITH_STATUS, userId, friendId, "UNCONFIRMED");
            log.info("Заявка в друзья от {} к {} создана (UNCONFIRMED)", userId, friendId);
        }
    }

    @Override
    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findById(long userId) {
        log.info("Пользователь с id={} получен", userId);
        return findOne(FIND_BY_ID_QUERY, userId);
    }

    @Override
    public User save(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday()
        );

        user.setId(id);

        log.info("Пользователь с id={} создан и сохранён", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        update(UPDATE_QUERY,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getId()
        );

        log.info("Пользователь с id={} обновлён", user.getId());
        return user;
    }

    @Override
    public boolean delete(long userId) {
        log.info("Пользователь с id={} удалён", userId);
        return delete(DELETE_USER, userId);
    }
}
