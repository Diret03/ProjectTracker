package edu.ec.projecttracker.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ec.projecttracker.AppDatabase;
import edu.ec.projecttracker.R;
import edu.ec.projecttracker.entity.User;
import edu.ec.projecttracker.utils.UserSession;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private EditText etEmail, etPassword;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // initialize database
        db = AppDatabase.getInstance(getApplicationContext());

        // initialize views
        btnLogin = findViewById(R.id.btnLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar);

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(view -> {
            showForgotPasswordDialog();
        });


        // sign up listener
        TextView tvSignUpPrompt = findViewById(R.id.tvSignUpPrompt);
        tvSignUpPrompt.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // login listener
        btnLogin.setOnClickListener(view -> {
            // Validate inputs
            if (validateInputs(getEmail(), getPassword())) {
                // Perform registration
                executorService.execute(this::loginUser);
            } else {
                // Hide progress indicator if validation fails
                progressBar.setVisibility(View.GONE);
            }
        });

    }


    private boolean validateInputs(String email, String password) {

        if (email.isEmpty()) {
            etEmail.setError("El correo electrónico es requerido");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingrese un correo electrónico válido");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("La contraseña es requerida");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser(){
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

        try {
            //check if user exists
            String email = getEmail();
            String password = getPassword();

            //find user by email
            final User user = db.userDao().findByEmail(email);

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);

                if (user == null) {
                    etEmail.setError("Usuario no encontrado");
                    etEmail.requestFocus();
                    return;
                }

                //check password
                if (!password.equals(user.password)) {
                    etPassword.setError("Contraseña incorrecta");
                    etPassword.requestFocus();
                    return;
                }

                //Store user in the singleton
                UserSession.getInstance().setCurrentUser(user);

//                Toast.makeText(this, "LOGIN EXITOSO", Toast.LENGTH_LONG).show();

//                // Login successful
                // Navigate to main activity or dashboard
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish(); // Close login activity
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                android.widget.Toast.makeText(LoginActivity.this,
                        "Error: " + e.getMessage(),
                        android.widget.Toast.LENGTH_LONG).show();
            });
        }
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);

        EditText etEmail = view.findViewById(R.id.etEmail);

        builder.setView(view)
                .setPositiveButton("Enviar", null)
                .setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set the button listener after dialog is shown to prevent automatic dismissal
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            // Validate email
            if (email.isEmpty()) {
                etEmail.setError("El correo electrónico es requerido");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Ingrese un correo electrónico válido");
                return;
            }

            // Process forgot password
            processForgotPassword(email, dialog);
        });
    }

    private void processForgotPassword(String email, AlertDialog currentDialog) {
        progressBar.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            try {
                // Find user by email
                final User user = db.userDao().findByEmail(email);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (user == null) {
                        Toast.makeText(this,
                                "No existe una cuenta con este correo electrónico",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Dismiss the email dialog
                    currentDialog.dismiss();

                    // Show reset password dialog
                    showResetPasswordDialog(user);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showResetPasswordDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);

        // In a real app, we would send a verification code to the user's email.
        // For demo purposes, we'll simulate this with a security question.

        TextView tvUserInfo = view.findViewById(R.id.tvUserInfo);
        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        String userInfo = "Usuario: " + user.firstName + " " + user.lastName;
        tvUserInfo.setText(userInfo);

        builder.setView(view)
//                .setTitle("Restablecer Contraseña")
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Set the button listener after dialog is shown
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validate passwords
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(LoginActivity.this,
                        "Todos los campos son requeridos",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(LoginActivity.this,
                        "La nueva contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(LoginActivity.this,
                        "Las contraseñas nuevas no coinciden",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Update password
            updatePassword(user, newPassword, dialog);
        });
    }

    private void updatePassword(User user, String newPassword, AlertDialog dialog) {
        progressBar.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            try {
                // Update user password in database
                user.password = newPassword;
                db.userDao().update(user);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    dialog.dismiss();
                    Toast.makeText(LoginActivity.this,
                            "Contraseña actualizada con éxito",
                            Toast.LENGTH_LONG).show();

                    // Pre-fill the email field for convenience
                    etEmail.setText(user.email);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this,
                            "Error al actualizar contraseña: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String getEmail() {
        return etEmail.getText().toString();
    }

    private String getPassword() {
        return etPassword.getText().toString();
    }

}