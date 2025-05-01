package com.example.onlykids_mobile_application;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class Mistake_memo extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Mistake_memo_adapter adapter;
    private Mistake_database db;

    private ActivityResultLauncher<Intent> retryLauncher;

    private List<Mistake_entity> backupMistakes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mistake_memo);

        Toolbar toolbar = findViewById(R.id.mistake_memo_toolbar);
        setSupportActionBar(toolbar);


        recyclerView = findViewById(R.id.recyclerMistakes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //creates a Room database instance that allows main thread queries for demo purpose
        db = Room.databaseBuilder(getApplicationContext(),
                Mistake_database.class, "mistake_db").allowMainThreadQueries().build();

        // Load existing mistakes
        List<Mistake_entity> mistakes = db.mistakeDao().getAllMistakes();
        adapter = new Mistake_memo_adapter(mistakes, this::launchRetry);
        recyclerView.setAdapter(adapter);

        // Register activity result launcher
        retryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String removedId = result.getData().getStringExtra("removed_id");

                        if (removedId != null) {
                            db.mistakeDao().clearById(removedId);

                            // Reload updated list
                            List<Mistake_entity> updatedList = db.mistakeDao().getAllMistakes();
                            adapter = new Mistake_memo_adapter(updatedList, this::launchRetry);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });
    }

    private void launchRetry(Mistake_entity mistake) {
        Intent intent = new Intent(this, Retry_mistake_memo.class);
        intent.putExtra("mistake_id", mistake.id);
        intent.putExtra("question", mistake.question);
        intent.putExtra("optionsJson", mistake.optionsJson);
        intent.putExtra("answer", mistake.answer);
        intent.putExtra("topic", mistake.topic);
        intent.putExtra("level", mistake.level);
        retryLauncher.launch(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.mistake_memo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all) {

            // Backup before deleting
            backupMistakes = db.mistakeDao().getAllMistakes();

            // Delete all
            db.mistakeDao().clearAll();
            adapter = new Mistake_memo_adapter(new ArrayList<>(), this::launchRetry);
            recyclerView.setAdapter(adapter);

            Snackbar.make(recyclerView, "🗑️ All memos deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo", v -> {
                        // Restore the backup list
                        for (Mistake_entity mistake : backupMistakes) {
                            db.mistakeDao().insert(mistake);
                        }
                        adapter = new Mistake_memo_adapter(db.mistakeDao().getAllMistakes(), this::launchRetry);
                        recyclerView.setAdapter(adapter);
                    }).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
