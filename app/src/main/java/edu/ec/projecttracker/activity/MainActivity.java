package edu.ec.projecttracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import edu.ec.projecttracker.R;
import edu.ec.projecttracker.utils.UserSession;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Project Tracker");

        // Set up navigation cards
        CardView cardProjects = findViewById(R.id.cardProjects);
        CardView cardTasks = findViewById(R.id.cardTasks);
        CardView cardProfile = findViewById(R.id.cardProfile);

        //Set up welcome message
        welcomeTextView = findViewById(R.id.tvWelcome);
        String welcomeMessage = "Bienvenido " + UserSession.getInstance().getCurrentUser().firstName;
        welcomeTextView.setText(welcomeMessage);

        cardProjects.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProjectActivity.class);
            startActivity(intent);
        });

        // These will be implemented later
        cardTasks.setOnClickListener(view -> {
             Intent intent = new Intent(this, TaskActivity.class);
             startActivity(intent);
        });

        cardProfile.setOnClickListener(view -> {
            // Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            // startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            // Clear session
            UserSession.getInstance().setCurrentUser(null);

            // Return to login screen
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}