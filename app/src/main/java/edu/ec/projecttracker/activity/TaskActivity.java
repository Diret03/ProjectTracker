package edu.ec.projecttracker.activity;

import edu.ec.projecttracker.utils.ProjectProgress;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ec.projecttracker.AppDatabase;
import edu.ec.projecttracker.R;
import edu.ec.projecttracker.adapter.TaskAdapter;
import edu.ec.projecttracker.entity.Project;
import edu.ec.projecttracker.entity.Task;
import edu.ec.projecttracker.utils.TaskStatus;
import edu.ec.projecttracker.utils.UserSession;



public class TaskActivity extends AppCompatActivity {

    private AppDatabase db;
    private ExecutorService executor;
    private RecyclerView rvTasks;
    private TextView tvEmptyTasks;
    private List<Project> userProjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Mis Tareas");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize database
        db = AppDatabase.getInstance(getApplicationContext());
        executor = Executors.newSingleThreadExecutor();

        // Initialize UI components
        rvTasks = findViewById(R.id.rvTasks);
        tvEmptyTasks = findViewById(R.id.tvEmptyTasks);
        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);

        // Set up RecyclerView
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        // Set up FAB click listener
        fabAddTask.setOnClickListener(view -> {
            loadProjects(() -> {
                if (!userProjects.isEmpty()) {
                    showCreateTaskDialog();
                } else {
                    Toast.makeText(TaskActivity.this,
                            "Debes crear al menos un proyecto primero",
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        // Load tasks
        loadTasks();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProjects(Runnable onComplete) {
        executor.execute(() -> {
            // Get current user ID
            int userId = UserSession.getInstance().getCurrentUser().id;

            // Get projects for the current user
            userProjects = db.projectDao().getAllByUserId(userId);

            runOnUiThread(() -> {
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    private void loadTasks() {
        executor.execute(() -> {
            // Get current user's projects
            int userId = UserSession.getInstance().getCurrentUser().id;
            List<Project> projects = db.projectDao().getAllByUserId(userId);

            // Create list to store all tasks from user's projects
            List<Task> allTasks = new ArrayList<>();

            // For each project, get its tasks
            for (Project project : projects) {
                List<Task> projectTasks = db.taskDao().getTasksByProjectId(project.id);
                allTasks.addAll(projectTasks);
            }

            runOnUiThread(() -> {
                if (allTasks.isEmpty()) {
                    rvTasks.setVisibility(View.GONE);
                    tvEmptyTasks.setVisibility(View.VISIBLE);
                } else {
                    rvTasks.setVisibility(View.VISIBLE);
                    tvEmptyTasks.setVisibility(View.GONE);
                    TaskAdapter adapter = new TaskAdapter(TaskActivity.this, allTasks, new TaskAdapter.OnTaskClickListener() {
                        @Override
                        public void onTaskClick(Task task) {
                            loadProjects(() -> showEditTaskDialog(task));
                        }
                    });
                    rvTasks.setAdapter(adapter);
                }
            });
        });
    }

    private void showCreateTaskDialog() {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_task, null);
        builder.setView(dialogView);
        builder.setTitle("Crear Nueva Tarea");

        // Get references to form elements
        EditText etTaskName = dialogView.findViewById(R.id.etTaskName);
        EditText etDescription = dialogView.findViewById(R.id.etTaskDescription);
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);
        Spinner spinnerProject = dialogView.findViewById(R.id.spinnerProject);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);

        // Set up the project spinner
        ArrayAdapter<Project> projectAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                userProjects
        );
        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProject.setAdapter(projectAdapter);

        // Set up the status spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                TaskStatus.ALL_STATUSES
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Add date pickers to date fields
        setupDatePicker(etStartDate);
        setupDatePicker(etEndDate);

        // Add buttons
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String name = etTaskName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String startDate = etStartDate.getText().toString().trim();
            String endDate = etEndDate.getText().toString().trim();
            String status = spinnerStatus.getSelectedItem().toString();

            if (name.isEmpty()) {
                Toast.makeText(this, "El nombre de la tarea es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            Project selectedProject = (Project) spinnerProject.getSelectedItem();
            if (selectedProject == null) {
                Toast.makeText(this, "Debes seleccionar un proyecto", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create and save task
            Task newTask = new Task();
            newTask.name = name;
            newTask.description = description;
            newTask.startDate = startDate;
            newTask.endDate = endDate;
            newTask.projectId = selectedProject.id;
            newTask.status = status;

            saveTask(newTask);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEditTaskDialog(Task task) {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_task, null);
        builder.setView(dialogView);
        builder.setTitle("Editar Tarea");

        // Get references to form elements
        EditText etTaskName = dialogView.findViewById(R.id.etTaskName);
        EditText etDescription = dialogView.findViewById(R.id.etTaskDescription);
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);
        Spinner spinnerProject = dialogView.findViewById(R.id.spinnerProject);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);

        // Set up the project spinner
        ArrayAdapter<Project> projectAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                userProjects
        );
        projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProject.setAdapter(projectAdapter);

        // Set up the status spinner
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                TaskStatus.ALL_STATUSES
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Pre-select the project
        for (int i = 0; i < userProjects.size(); i++) {
            if (userProjects.get(i).id == task.projectId) {
                spinnerProject.setSelection(i);
                break;
            }
        }

        // Pre-select the status
        for (int i = 0; i < TaskStatus.ALL_STATUSES.length; i++) {
            if (TaskStatus.ALL_STATUSES[i].equals(task.status)) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        // Populate fields with task data
        etTaskName.setText(task.name);
        etDescription.setText(task.description);
        etStartDate.setText(task.startDate);
        etEndDate.setText(task.endDate);

        // Add date pickers to date fields
        setupDatePicker(etStartDate);
        setupDatePicker(etEndDate);

        // Add buttons
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String name = etTaskName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String startDate = etStartDate.getText().toString().trim();
            String endDate = etEndDate.getText().toString().trim();
            String status = spinnerStatus.getSelectedItem().toString();

            if (name.isEmpty()) {
                Toast.makeText(this, "El nombre de la tarea es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            Project selectedProject = (Project) spinnerProject.getSelectedItem();
            if (selectedProject == null) {
                Toast.makeText(this, "Debes seleccionar un proyecto", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update task
            task.name = name;
            task.description = description;
            task.startDate = startDate;
            task.endDate = endDate;
            task.projectId = selectedProject.id;
            task.status = status;

            updateTask(task);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        // Add delete button
        builder.setNeutralButton("Eliminar", (dialog, which) -> {
            confirmDeleteTask(task);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setupDatePicker(EditText editText) {
        editText.setInputType(InputType.TYPE_NULL);
        editText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    TaskActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dateStr = selectedYear + "-" +
                                String.format("%02d", selectedMonth + 1) + "-" +
                                String.format("%02d", selectedDay);
                        editText.setText(dateStr);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void saveTask(Task task) {
        executor.execute(() -> {
            try {
                // Insert the task
                db.taskDao().insert(task);

                // Update the project progress
                ProjectProgress.calculateAndUpdateProgress(db, task.projectId, progress -> {});

                runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this,
                            "Tarea creada con éxito", Toast.LENGTH_SHORT).show();
                    loadTasks(); // Refresh list
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this,
                            "Error al crear tarea: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateTask(Task task) {
        executor.execute(() -> {
            try {
                // Update the task
                db.taskDao().update(task);

                // Update the associated project's progress
                ProjectProgress.calculateAndUpdateProgress(db, task.projectId, progress -> {});

                runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this,
                            "Tarea actualizada con éxito", Toast.LENGTH_SHORT).show();
                    loadTasks(); // Refresh list
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this,
                            "Error al actualizar tarea: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void confirmDeleteTask(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Tarea")
                .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
                .setPositiveButton("Sí", (dialog, which) -> deleteTask(task))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteTask(Task task) {
        executor.execute(() -> {
            try {
                // Store projectId before deletion
                int projectId = task.projectId;

                // Delete the task
                db.taskDao().deleteById(task.id);

                // Update project progress
                ProjectProgress.calculateAndUpdateProgress(db, projectId, progress -> {});

                runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this,
                            "Tarea eliminada con éxito", Toast.LENGTH_SHORT).show();
                    loadTasks(); // Refresh list
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(TaskActivity.this,
                            "Error al eliminar tarea: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}