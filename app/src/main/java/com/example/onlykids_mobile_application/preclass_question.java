package com.example.onlykids_mobile_application;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class preclass_question extends AppCompatActivity {

    private EditText nameInput, ageInput;
    private Spinner spinnerLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if entering with the first time
        SharedPreferences prefs = getSharedPreferences("OnlyKidsPrefs", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);



        if (!isFirstTime) {
            // go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_preclass_question);

        nameInput = findViewById(R.id.editName);
        ageInput = findViewById(R.id.editAge);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        Button continueButton = findViewById(R.id.buttonContinue);

        // Set up Spinner
        String[] levels = {"Primary 1", "Primary 2", "Primary 3", "Primary 4", "Primary 5", "Primary 6"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                levels
        );
        spinnerLevel.setAdapter(adapter);

        continueButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String age = ageInput.getText().toString().trim();
            String level = spinnerLevel.getSelectedItem().toString();

            if (name.isEmpty() || age.isEmpty()) {
                Toast.makeText(this, "Please enter all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putString("user_name", name)
                    .putString("user_age", age)
                    .putString("user_level", level)
                    .putBoolean("isFirstTime", false)
                    .apply();

            // Navigate to main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
