package com.example.userauth.Activies;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.userauth.R;
import com.example.userauth.databinding.ActivityAddNoteBinding;
import com.example.userauth.model.AddNotes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddNoteAct extends AppCompatActivity {

    private ActivityAddNoteBinding bindings;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private AddNotes addNotes;
    private ProgressDialog progressDialog;
    private String noteKey; // To store the unique key for update
    private boolean isUpdate = false;
    private String selectedPriority = "Low"; // Default priority
    private Uri uri;
    private ProgressDialog dialog;
    private  String downloadUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindings = ActivityAddNoteBinding.inflate(getLayoutInflater());
        View view = bindings.getRoot();
        setContentView(view);
        // Get instance of Firebase database
        firebaseDatabase = FirebaseDatabase.getInstance();
        // Get reference for the database
        databaseReference = firebaseDatabase.getReference("AddNotes");
        addNotes = new AddNotes();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        // Check if it's an update or add operation
        Intent intent = getIntent();


        if (intent.hasExtra("isUpdate") && intent.getBooleanExtra("isUpdate", false)) {
            isUpdate = true;
            bindings.toolbartext.setText("Update Note");
            noteKey = intent.getStringExtra("key");
            bindings.tileText.setText(intent.getStringExtra("title"));
            bindings.descriptionText.setText(intent.getStringExtra("description"));
            selectedPriority = intent.getStringExtra("priority");
            downloadUri = intent.getStringExtra("uriImage");
            bindings.uritext.setText(downloadUri);

        } else {
            bindings.toolbartext.setText("Add Note");
            isUpdate = false;
        }
        bindings.backbtn.setOnClickListener(v -> onBackPressed());

        bindings.pickImage.setOnClickListener(view1 -> {

            Intent intent1 =new Intent();
            intent1.setType("image/*");
            intent1.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent1,5);
            dialog=new ProgressDialog(AddNoteAct.this);
        });


        // Set up the priority spinner
        Spinner prioritySpinner = bindings.spinner;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        // Set the default selection if it's an update
        if (isUpdate) {
            int spinnerPosition = adapter.getPosition(selectedPriority);
            prioritySpinner.setSelection(spinnerPosition);
        }
        // Handle spinner selection
        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriority = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        bindings.saveNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = bindings.tileText.getText().toString().trim();
                String description = bindings.descriptionText.getText().toString().trim();
                String uri = bindings.uritext.getText().toString().trim();

                if (title.isEmpty() || description.isEmpty()) {
                    Toast.makeText(AddNoteAct.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.show(); // Show the dialog here, before starting the operation
                    if (isUpdate) {
                        updateDataToFirebase(title, description, selectedPriority, downloadUri);
                    } else {
                        addDataToFirebase(title, description, selectedPriority, downloadUri);
                    }
                }
            }
        });
    }
    private void addDataToFirebase(String title, String description, String priority, String uri) {
        addNotes.setTitle(title);
        addNotes.setDescription(description);
        addNotes.setPriority(priority);
        addNotes.setUriImage(uri);

        String key = databaseReference.push().getKey();
        addNotes.setKey(key);

        databaseReference.child(key).setValue(addNotes)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddNoteAct.this, "Data added successfully!", Toast.LENGTH_SHORT).show();
                    bindings.tileText.setText("");
                    bindings.descriptionText.setText("");
                    bindings.uritext.setText("");
                })
                .addOnFailureListener(error -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddNoteAct.this, "Failed to add data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateDataToFirebase(String title, String description, String priority, String uri) {
        // Ensure you're using the correct noteKey for updating the note.
        addNotes.setTitle(title);
        addNotes.setDescription(description);
        addNotes.setPriority(priority);
        addNotes.setUriImage(uri);

        // Ensure you're using the right reference in the database
        databaseReference.child(noteKey).setValue(addNotes)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddNoteAct.this, "Data updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();  // Finish and return to the previous screen
                })
                .addOnFailureListener(error -> {
                    progressDialog.dismiss();
                    Toast.makeText(AddNoteAct.this, "Failed to update data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 5 && resultCode==RESULT_OK){
            uri= data.getData();
            dialog.setTitle("uploading...");
            dialog.show();
            uploadImage();
        }
    }
    private void uploadImage() {
        if (uri != null) {
            final StorageReference reference = FirebaseStorage.getInstance().getReference("Files/" + System.currentTimeMillis() + "." + getfiletype(uri));

            reference.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;

                            // Get the download URL
                            downloadUri = uriTask.getResult().toString();
                            bindings.uritext.setText(downloadUri);

                            // Also save the download URI to the Firebase database
                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("images");
                            HashMap<String, String> map = new HashMap<>();
                            map.put("image", downloadUri);
                            reference1.child("" + System.currentTimeMillis()).setValue(map);

                            dialog.dismiss();
                            Toast.makeText(AddNoteAct.this, "Image Uploaded!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(AddNoteAct.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            dialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    private String getfiletype(Uri videouri) {
        ContentResolver r = getContentResolver();
        // get the file type ,in this case its mp4
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videouri));
    }

}