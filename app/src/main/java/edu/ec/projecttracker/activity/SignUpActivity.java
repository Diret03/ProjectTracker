package edu.ec.projecttracker.activity;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import edu.ec.projecttracker.R;
import edu.ec.projecttracker.AppDatabase;
import edu.ec.projecttracker.entity.User;

public class SignUpActivity extends AppCompatActivity {

    private Button btnSignUp;
    AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private EditText etName, etLastName, etEmail, etPassword, etConfirmPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // initialize database
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "project_tracker_db").build();

        // initialize views
        btnSignUp = findViewById(R.id.btnSignUp);
        etName = findViewById(R.id.etName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        progressBar = findViewById(R.id.progressBar);

        // sign up listener
        TextView tvLoginPrompt = findViewById(R.id.tvLoginPrompt);
        tvLoginPrompt.setOnClickListener(view -> {
            android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
            startActivity(intent);
        });



        // sign up listener
        btnSignUp.setOnClickListener(view -> {
            // Show progress while validating
            progressBar.setVisibility(View.VISIBLE);

            // Validate inputs
            if (validateInputs(getName(), getLastName(), getEmail(), getPassword(), getConfirmPassword())) {
                // Perform registration
                executorService.execute(this::registerUser);
            } else {
                // Hide progress indicator if validation fails
                progressBar.setVisibility(View.GONE);
            }
        });


    }

    // shut down the executor in onDestroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
    private boolean validateInputs(String name, String lastName, String email,
                                   String password, String confirmPassword) {
        // Check for empty fields
        if (name.isEmpty()) {
            etName.setError("El nombre es requerido");
            etName.requestFocus();
            return false;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("El apellido es requerido");
            etLastName.requestFocus();
            return false;
        }

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

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser() {
        // Show progress bar while processing
        runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));

        try {
            // Check if email already exists
            String email = getEmail();
            User existingUser = db.userDao().findByEmail(email);

            if (existingUser != null) {
                // Email already taken
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    etEmail.setError("Este correo electrónico ya está registrado");
                    etEmail.requestFocus();
                });
                return;
            }

            // Create new user
            User user = new User();
            user.firstName = getName();
            user.lastName = getLastName();
            user.email = email;
            user.password = getPassword();

            // Insert user to database
            db.userDao().insert(user);

            // Registration successful
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                android.widget.Toast.makeText(
                        SignUpActivity.this,
                        "Registro exitoso. Ahora puedes iniciar sesión.",
                        android.widget.Toast.LENGTH_LONG
                ).show();

                // Navigate back to login screen
                android.content.Intent intent = new android.content.Intent(
                        SignUpActivity.this,
                        LoginActivity.class
                );
                startActivity(intent);
                finish(); // Close sign up activity
            });

            Log.d("SignUpActivity", "User registered successfully: " + user.email);

        } catch (Exception e) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                android.widget.Toast.makeText(
                        SignUpActivity.this,
                        "Error al registrar: " + e.getMessage(),
                        android.widget.Toast.LENGTH_LONG
                ).show();
            });
            Log.e("SignUpActivity", "Registration error", e);
        }
    }

    private String getName() {
        return etName.getText().toString();
    }

    private String getLastName() {
        return etLastName.getText().toString();
    }

    private String getEmail() {
        return etEmail.getText().toString();
    }

    private String getPassword() {
        return etPassword.getText().toString();
    }

    private String getConfirmPassword() {
        return etConfirmPassword.getText().toString();
    }
}