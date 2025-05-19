package edu.ec.projecttracker.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import edu.ec.projecttracker.entity.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    List<User> getAll();

    @Query("SELECT * FROM users WHERE id = :userId")
    User findById(int userId);
    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM users WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    User findByName(String first, String last);

    @Query("SELECT * FROM users WHERE email LIKE :email")
    User findByEmail(String email);

    @Insert
    void insertAll(User... users);
    @Insert
    void insert(User user);
    
    @Update
    void update(User user);
    @Delete
    void delete(User user);
}
