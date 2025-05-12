package edu.ec.projecttracker;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import edu.ec.projecttracker.dao.ProjectDao;
import edu.ec.projecttracker.dao.TaskDao;
import edu.ec.projecttracker.dao.UserDao;
import edu.ec.projecttracker.entity.Project;
import edu.ec.projecttracker.entity.Task;
import edu.ec.projecttracker.entity.User;

@Database(entities = {User.class, Project.class, Task.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ProjectDao projectDao();
    public abstract TaskDao taskDao();
}