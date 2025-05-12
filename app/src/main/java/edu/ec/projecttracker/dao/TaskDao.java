package edu.ec.projecttracker.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import edu.ec.projecttracker.entity.Task;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks")
    List<Task> getAll();

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task findById(int taskId);

    @Query("SELECT * FROM tasks WHERE id IN (:taskIds)")
    List<Task> loadAllByIds(int[] taskIds);

    @Query("SELECT * FROM tasks WHERE name LIKE :name")
    Task findByName(String name);

    @Query("SELECT * FROM tasks WHERE start_date BETWEEN :startDate AND :endDate")
    Task findByDateRange(String startDate, String endDate);

    @Query("SELECT * FROM tasks WHERE start_date = :date OR end_date = :date")
    Task findByDate(String date);

    @Insert
    void insertAll(Task... tasks);

    @Insert
    void insert(Task task);

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteById(int taskId);
}