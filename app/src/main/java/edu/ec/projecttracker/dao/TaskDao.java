package edu.ec.projecttracker.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import edu.ec.projecttracker.entity.Task;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks")
    List<Task> getAll();

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task findById(int taskId);

    @Query("SELECT * FROM tasks WHERE project_id = :projectId")
    List<Task> getTasksByProjectId(int projectId);

    @Query("SELECT * FROM tasks WHERE id IN (:taskIds)")
    List<Task> loadAllByIds(int[] taskIds);

    @Query("SELECT * FROM tasks WHERE name LIKE :name")
    Task findByName(String name);

    @Query("SELECT * FROM tasks WHERE start_date BETWEEN :startDate AND :endDate")
    Task findByDateRange(String startDate, String endDate);

    @Query("SELECT * FROM tasks WHERE start_date = :date OR end_date = :date")
    Task findByDate(String date);

    @Query("SELECT COUNT(*) FROM tasks WHERE project_id = :projectId")
    int countTasksByProjectId(int projectId);

    @Query("SELECT COUNT(*) FROM tasks WHERE project_id = :projectId AND status = :status")
    int countTasksByProjectIdAndStatus(int projectId, String status);

    @Query("SELECT COUNT(*) FROM tasks t " +
            "INNER JOIN projects p ON t.project_id = p.id " +
            "WHERE p.user_id = :userId")
    int getTaskCount(int userId);

    @Query("SELECT COUNT(*) FROM tasks t " +
            "INNER JOIN projects p ON t.project_id = p.id " +
            "WHERE p.user_id = :userId AND t.status = 'Realizado'")
    int getCompletedTaskCount(int userId);

    @Insert
    void insertAll(Task... tasks);

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteById(int taskId);
}