package ru.yandex.practicum.filmorate.dal.user;

import ru.yandex.practicum.filmorate.dto.user.LikeDto;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    List<User> findAll();

    Optional<User> findById(long userId);

    User save(User user);

    User update(User user);

    boolean delete(long userId);

    Set<User> findAllFriends(long userId);

    Set<User> findCommonFriends(long user1Id, long user2Id);

    void deleteFriend(long userId, long friendId);

    void saveFriend(long userId, long friendId);

    List<User> findAllLikes(long filmId);

    Map<Long, Set<User>> findFriendsForUserIds(List<Long> userIds);

    Map<Long, Set<LikeDto>> findLikesForFilmsIds(List<Long> filmIds);
}
