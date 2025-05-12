package edu.ec.projecttracker.activity;

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
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "project_tracker_db").build();

        // initialize views
        btnLogin = findViewById(R.id.btnLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar);

        // sign up listener
        TextView tvSignUpPrompt = findViewById(R.id.tvSignUpPrompt);
        tvSignUpPrompt.setOnClickListener(view -> {
            android.content.Intent intent = new android.content.Intent(LoginActivity.this, SignUpActivity.class);
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

                Toast.makeText(this, "LOGIN EXITOSO", Toast.LENGTH_LONG).show();

//                // Login successful
//                // Navigate to main activity or dashboard
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//                finish(); // Close login activity
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

    private String getEmail() {
        return etEmail.getText().toString();
    }

    private String getPassword() {
        return etPassword.getText().toString();
    }

}