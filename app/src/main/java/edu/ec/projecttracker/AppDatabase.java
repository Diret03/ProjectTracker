package edu.ec.projecttracker;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import edu.ec.projecttracker.dao.ProjectDao;
import edu.ec.projecttracker.dao.TaskDao;
import edu.ec.projecttracker.dao.UserDao;
import edu.ec.projecttracker.entity.Project;
import edu.ec.projecttracker.entity.Task;
import edu.ec.projecttracker.entity.User;

@Database(entities = {User.class, Project.class, Task.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "project_tracker_db_2.5";
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, DATABASE_NAME).build();
        }
        return instance;
    }

    public abstract UserDao userDao();
    public abstract ProjectDao projectDao();
    public abstract TaskDao taskDao();
}