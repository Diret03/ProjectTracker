package edu.ec.projecttracker.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import edu.ec.projecttracker.entity.Project;

@Dao
public interface ProjectDao {

    @Query("SELECT * FROM projects")
    List<Project> getAll();

    @Query("SELECT * FROM projects WHERE user_id = :userId")
    List<Project> getAllByUserId(int userId);

    @Query("SELECT * FROM projects WHERE id = :projectId")
    Project findById(int projectId);

    @Query("SELECT * FROM projects WHERE id IN (:projectIds)")
    List<Project> loadAllByIds(int[] projectIds);

    @Query("SELECT * FROM projects WHERE name LIKE :name")
    Project findByName(String name);

    @Query("SELECT * FROM projects WHERE start_date BETWEEN :startDate AND :endDate")
    Project findByDateRange(String startDate, String endDate);

    @Query("SELECT * FROM projects WHERE start_date = :date OR end_date = :date")
    Project findByDate(String date);

    @Insert
    void InsertAll(Project... projects);
    @Insert
    void Insert(Project project);

    @Query("DELETE FROM projects WHERE id = :projectId")
    void deleteById(int projectId);

}
