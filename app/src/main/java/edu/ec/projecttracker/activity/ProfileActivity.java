package edu.ec.projecttracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ec.projecttracker.AppDatabase;
import edu.ec.projecttracker.R;
import edu.ec.projecttracker.entity.User;
import edu.ec.projecttracker.utils.UserSession;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName;
    private TextView tvUserEmail;
    private TextView tvProjectCount;
    private TextView tvTaskCount;
    private TextView tvCompletedTasks;
    private LinearLayout layoutChangePassword;
    private LinearLayout layoutLogout;

    private AppDatabase db;
    private ExecutorService executor;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize database and executor
        db = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        // Get current user
        currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            // If no user is logged in, redirect to login
            Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        initViews();

        // Load user data
        loadUserData();

        // Load statistics
        loadStatistics();

        // Set up click listeners
        setClickListeners();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvProjectCount = findViewById(R.id.tvProjectCount);
        tvTaskCount = findViewById(R.id.tvTaskCount);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        layoutChangePassword = findViewById(R.id.layoutChangePassword);
        layoutLogout = findViewById(R.id.layoutLogout);
    }

    private void loadUserData() {
        if (currentUser != null) {
            String fullName = currentUser.firstName + " " + currentUser.lastName;
            tvUserName.setText(fullName);
            tvUserEmail.setText(currentUser.email);
        }
    }

    private void loadStatistics() {
        executor.execute(() -> {
            try {
                // Count projects
                int projectCount = db.projectDao().getProjectCount(currentUser.id);

                // Count tasks
                int taskCount = db.taskDao().getTaskCount(currentUser.id);

                // Count completed tasks
                int completedTaskCount = db.taskDao().getCompletedTaskCount(currentUser.id);

                runOnUiThread(() -> {
                    tvProjectCount.setText(String.valueOf(projectCount));
                    tvTaskCount.setText(String.valueOf(taskCount));
                    tvCompletedTasks.setText(String.valueOf(completedTaskCount));
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this,
                            "Error al cargar estadísticas: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setClickListeners() {
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            showEditProfileDialog();
        });

        layoutChangePassword.setOnClickListener(v -> {
            showChangePasswordDialog();
        });

        layoutLogout.setOnClickListener(v -> {
            showLogoutConfirmation();
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Clear user session
                    UserSession.getInstance().logout();
                    // Redirect to login screen
                    redirectToLogin();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Add this method in ProfileActivity.java
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);

        EditText etFirstName = view.findViewById(R.id.etFirstName);
        EditText etLastName = view.findViewById(R.id.etLastName);
        EditText etEmail = view.findViewById(R.id.etEmail);

        // Pre-fill with current user data
        etFirstName.setText(currentUser.firstName);
        etLastName.setText(currentUser.lastName);
        etEmail.setText(currentUser.email);

        builder.setView(view)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String firstName = etFirstName.getText().toString().trim();
                    String lastName = etLastName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();

                    // Basic validation
                    if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                        Toast.makeText(ProfileActivity.this,
                                "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update user info
                    updateUserProfile(firstName, lastName, email);
                })
                .setNegativeButton("Cancelar", null)
                .create()
                .show();
    }

    private void updateUserProfile(String firstName, String lastName, String email) {
        executor.execute(() -> {
            try {
                // Check if email is already in use by another user
                User existingUser = db.userDao().findByEmail(email);
                if (existingUser != null && existingUser.id != currentUser.id) {
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this,
                                "Este correo ya está en uso por otra cuenta",
                                Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Update user in database
                currentUser.firstName = firstName;
                currentUser.lastName = lastName;
                currentUser.email = email;

                db.userDao().update(currentUser);

                // Update session
                UserSession.getInstance().setCurrentUser(currentUser);

                runOnUiThread(() -> {
                    // Refresh UI
                    loadUserData();
                    Toast.makeText(ProfileActivity.this,
                            "Perfil actualizado con éxito",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this,
                            "Error al actualizar perfil: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        EditText etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        builder.setView(view)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set the button listener after dialog is shown to prevent automatic dismissal
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validate inputs
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(ProfileActivity.this,
                        "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!currentPassword.equals(currentUser.password)) {
                Toast.makeText(ProfileActivity.this,
                        "La contraseña actual no es correcta", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(ProfileActivity.this,
                        "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(ProfileActivity.this,
                        "Las contraseñas nuevas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            // Change password
            updatePassword(newPassword);
            dialog.dismiss();
        });
    }

    private void updatePassword(String newPassword) {
        executor.execute(() -> {
            try {
                // Update user in database
                currentUser.password = newPassword;
                db.userDao().update(currentUser);

                // Update session
                UserSession.getInstance().setCurrentUser(currentUser);

                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this,
                            "Contraseña actualizada con éxito",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this,
                            "Error al actualizar contraseña: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}