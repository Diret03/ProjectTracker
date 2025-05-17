package edu.ec.projecttracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ec.projecttracker.AppDatabase;
import edu.ec.projecttracker.R;
import edu.ec.projecttracker.adapter.ProjectAdapter;
import edu.ec.projecttracker.entity.Project;
import edu.ec.projecttracker.utils.UserSession;

public class ProjectActivity extends AppCompatActivity {

    private RecyclerView rvProjects;
    private TextView tvEmptyProjects;
    private AppDatabase db;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        // Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvProjects = findViewById(R.id.rvProjects);
        tvEmptyProjects = findViewById(R.id.tvEmptyProjects);
        FloatingActionButton fabAddProject = findViewById(R.id.fabAddProject);

        // Setup RecyclerView
        rvProjects.setLayoutManager(new LinearLayoutManager(this));

        // Initialize database
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "project-tracker-db").build();

        executor = Executors.newSingleThreadExecutor();

        // FAB click listener
        fabAddProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start activity to create new project
                // Intent intent = new Intent(ProjectActivity.this, CreateProjectActivity.class);
                // startActivity(intent);

                // For now just show a message
                // Snackbar.make(view, "Create new project feature coming soon", Snackbar.LENGTH_LONG).show();
            }
        });

        // Load projects
        loadProjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload projects when returning to this activity
        loadProjects();
    }

    private void loadProjects() {
        executor.execute(() -> {
            // Get current user ID
            int userId = UserSession.getInstance().getCurrentUser().id;

            // Get projects for the current user
            List<Project> projects = db.projectDao().getAllByUserId(userId);

            runOnUiThread(() -> {
                if (projects.isEmpty()) {
                    rvProjects.setVisibility(View.GONE);
                    tvEmptyProjects.setVisibility(View.VISIBLE);
                } else {
                    rvProjects.setVisibility(View.VISIBLE);
                    tvEmptyProjects.setVisibility(View.GONE);
                    ProjectAdapter adapter = new ProjectAdapter(projects);
                    rvProjects.setAdapter(adapter);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}