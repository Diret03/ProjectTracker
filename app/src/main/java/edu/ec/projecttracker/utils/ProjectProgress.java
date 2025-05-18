package edu.ec.projecttracker.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ec.projecttracker.AppDatabase;
import edu.ec.projecttracker.entity.Project;

// Update the ProjectProgress class to use a better method name
public class ProjectProgress {
    public interface OnProgressCalculatedListener {
        void onProgressCalculated(int progressPercentage);
    }

    public static void calculateAndUpdateProgress(AppDatabase db, int projectId,
                                                  OnProgressCalculatedListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                int totalTasks = db.taskDao().countTasksByProjectId(projectId);
                int completedTasks = db.taskDao().countTasksByProjectIdAndStatus(
                        projectId, TaskStatus.COMPLETED);

                int progressPercentage = 0;
                if (totalTasks > 0) {
                    progressPercentage = (completedTasks * 100) / totalTasks;
                }

                // Update the project in the database
                Project project = db.projectDao().findById(projectId);
                if (project != null) {
                    project.progress = progressPercentage;
                    db.projectDao().update(project);
                }

                int finalProgressPercentage = progressPercentage;
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onProgressCalculated(finalProgressPercentage);
                    executor.shutdown();
                });

            } catch (Exception e) {
                e.printStackTrace();
                executor.shutdown();
            }
        });
    }
}