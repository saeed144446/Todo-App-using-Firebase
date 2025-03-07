package com.example.userauth.Activies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.userauth.Adapter.NoteListAdapter;
import com.example.userauth.R;
import com.example.userauth.databinding.ActivityMainBinding;
import com.example.userauth.model.AddNotes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding bindings;
    private ArrayList<AddNotes> notesArrayList = new ArrayList<>();
    private NoteListAdapter adapter;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindings = ActivityMainBinding.inflate(getLayoutInflater());
        View view = bindings.getRoot();
        setContentView(view);

        bindings.recyclerview.setHasFixedSize(true);
        adapter = new NoteListAdapter(this, notesArrayList);
        bindings.recyclerview.setAdapter(adapter);
        bindings.recyclerview.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("AddNotes");

        DatabaseReference getImage = databaseReference.child("image");

        bindings.settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingAct.class));
            }
        });
        bindings.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddNoteAct.class));
            }
        });
        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        Query query = databaseReference.orderByChild("priority");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notesArrayList.clear(); // Clear the list before adding new data

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AddNotes note = snapshot.getValue(AddNotes.class);

                    if (note != null) {
                        notesArrayList.add(note);
                    }
                }

                // Custom sorting
                Collections.sort(notesArrayList, new Comparator<AddNotes>() {
                    @Override
                    public int compare(AddNotes note1, AddNotes note2) {
                        // Define the priority order
                        String[] priorityOrder = {"High", "Medium", "Low"};
                        int index1 = -1;
                        int index2 = -1;
                        for (int i = 0; i < priorityOrder.length; i++) {
                            if (priorityOrder[i].equals(note1.getPriority())) {
                                index1 = i;
                            }
                            if (priorityOrder[i].equals(note2.getPriority())) {
                                index2 = i;
                            }
                        }

                        return Integer.compare(index1, index2);
                    }
                });

                progressDialog.dismiss();
                adapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("MainActivity", "Error fetching data: " + databaseError.getMessage());
                Toast.makeText(MainActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }


}