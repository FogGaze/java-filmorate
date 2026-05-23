package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    public User addUser(User user);

    public User updateUser(User user);

    public void deleteUser(long id);

    public Collection<User> getAllUsers();

    public User getUserById(long id);

    public void clearStorage();
}
