package edu.ec.projecttracker.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ec.projecttracker.AppDatabase;
import edu.ec.projecttracker.R;
import edu.ec.projecttracker.adapter.ProjectAdapter;
import edu.ec.projecttracker.entity.Project;
import edu.ec.projecttracker.entity.User;
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

        // In ProjectActivity.java's onCreate method:
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize database
        db = AppDatabase.getInstance(getApplicationContext());

        executor = Executors.newSingleThreadExecutor();

        // FAB click listener
        fabAddProject.setOnClickListener(view -> showCreateProjectDialog());

        // Load projects
        loadProjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload projects when returning to this activity
        loadProjects();
    }

   // And add this method:
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    ProjectAdapter adapter = new ProjectAdapter(this, projects, new ProjectAdapter.OnProjectClickListener() {
                        @Override
                        public void onProjectClick(Project project) {
                            showEditProjectDialog(project);
                        }
                    });
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

    private void showCreateProjectDialog() {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_project, null);
        builder.setView(dialogView);
//        builder.setTitle("Crear Nuevo Proyecto");

        // Get references to form elements
        EditText etProjectName = dialogView.findViewById(R.id.etProjectName);
        EditText etDescription = dialogView.findViewById(R.id.etProjectDescription);
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);

        // Add date pickers to date fields
        setupDatePicker(etStartDate);
        setupDatePicker(etEndDate);

        // Add buttons
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String name = etProjectName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String startDate = etStartDate.getText().toString().trim();
            String endDate = etEndDate.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "El nombre del proyecto es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create and save project
            Project newProject = new Project();
            newProject.name = name;
            newProject.description = description;
            newProject.startDate = startDate;
            newProject.endDate = endDate;
            newProject.userId = UserSession.getInstance().getCurrentUser().id;

            saveProject(newProject);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEditProjectDialog(Project project) {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_project, null);
        builder.setView(dialogView);
//        builder.setTitle("Editar Proyecto");

        // Get references to form elements
        EditText etProjectName = dialogView.findViewById(R.id.etProjectName);
        EditText etDescription = dialogView.findViewById(R.id.etProjectDescription);
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);

        // Populate fields with project data
        etProjectName.setText(project.name);
        etDescription.setText(project.description);
        etStartDate.setText(project.startDate);
        etEndDate.setText(project.endDate);

        // Add date pickers to date fields
        setupDatePicker(etStartDate);
        setupDatePicker(etEndDate);

        // Add buttons
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String name = etProjectName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String startDate = etStartDate.getText().toString().trim();
            String endDate = etEndDate.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "El nombre del proyecto es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update project
            project.name = name;
            project.description = description;
            project.startDate = startDate;
            project.endDate = endDate;

            updateProject(project);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        // Add delete button
        builder.setNeutralButton("Eliminar", (dialog, which) -> {
            confirmDeleteProject(project);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateProject(Project project) {
        executor.execute(() -> {
            try {
                // Update the project
                db.projectDao().update(project);

                runOnUiThread(() -> {
                    Toast.makeText(ProjectActivity.this,
                            "Proyecto actualizado con éxito", Toast.LENGTH_SHORT).show();
                    loadProjects(); // Refresh list
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ProjectActivity.this,
                            "Error al actualizar proyecto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void confirmDeleteProject(Project project) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Proyecto")
                .setMessage("¿Estás seguro de que deseas eliminar este proyecto?")
                .setPositiveButton("Sí", (dialog, which) -> deleteProject(project))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteProject(Project project) {
        executor.execute(() -> {
            try {
                // Delete the project
                db.projectDao().deleteById(project.id);

                runOnUiThread(() -> {
                    Toast.makeText(ProjectActivity.this,
                            "Proyecto eliminado con éxito", Toast.LENGTH_SHORT).show();
                    loadProjects(); // Refresh list
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ProjectActivity.this,
                            "Error al eliminar proyecto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void setupDatePicker(EditText editText) {
        editText.setInputType(InputType.TYPE_NULL);
        editText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ProjectActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dateStr = selectedYear + "-" +
                                String.format("%02d", selectedMonth + 1) + "-" +
                                String.format("%02d", selectedDay);
                        editText.setText(dateStr);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void saveProject(Project project) {
        executor.execute(() -> {
            try {
                // First verify the user exists
                int userId = UserSession.getInstance().getCurrentUser().id;
                User user = db.userDao().findById(userId);

                if (user == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(ProjectActivity.this,
                                "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Now insert the project
                db.projectDao().Insert(project);
                runOnUiThread(() -> {
                    Toast.makeText(ProjectActivity.this,
                            "Proyecto creado con éxito", Toast.LENGTH_SHORT).show();
                    loadProjects(); // Refresh list
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ProjectActivity.this,
                            "Error al crear proyecto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }



}