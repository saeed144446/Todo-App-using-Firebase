package com.example.userauth.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.userauth.Activies.AddNoteAct;
import com.example.userauth.R;
import com.example.userauth.model.AddNotes;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.viewHolder> {
    private Context context;
    private ArrayList<AddNotes> noteList;

    public NoteListAdapter(Context context, ArrayList<AddNotes> noteList) {
        this.context = context;
        this.noteList = noteList;
    }
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.savenote_listitem, parent, false);
        return new viewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        AddNotes currentNote = noteList.get(position);

        // Set the data to the views in the view holder
        holder.title_text.setText(currentNote.getTitle());
        holder.description_text.setText(currentNote.getDescription());
        holder.priority_text.setText( currentNote.getPriority());
       // holder.uri_text.setText(currentNote.uriImage());

        Glide.with(context)
                .load(currentNote.getUriImage())
                .placeholder(R.drawable.ic_launcher_background)  // Placeholder image while loading
                .error(R.drawable.ic_dialogerror)  // Error image if loading fails
                .into(holder.imageView);

        holder.morevert.setOnClickListener(v -> {
            // Handle morevert button click
            PopupMenu popupMenu = new PopupMenu(context, holder.morevert);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.update) {
                    // Update logic
                    notifyDataSetChanged();
                    Intent intent = new Intent(context, AddNoteAct.class);
                    intent.putExtra("title", currentNote.getTitle());
                    intent.putExtra("description", currentNote.getDescription());
                    intent.putExtra("priority", currentNote.getPriority());
                    intent.putExtra("key", currentNote.getKey());
                    intent.putExtra("isUpdate", true);
                    intent.putExtra("uriImage", currentNote.getUriImage());
                    context.startActivity(intent);
                } else if (menuItem.getItemId() == R.id.delete) {
                    // Log the key to make sure it's correct
                    Log.d("Delete", "Deleting note with key: " + currentNote.getKey());
                    delete(currentNote.getKey()); // Pass key for deletion
                }
                return true;
            });
            popupMenu.show();
        });

    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }
    public class viewHolder extends RecyclerView.ViewHolder {
        private TextView title_text;
        private TextView description_text;
        private TextView priority_text;
        private ImageView morevert;
        private TextView uri_text;
        private ImageView imageView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            title_text = itemView.findViewById(R.id.title_text);
            description_text = itemView.findViewById(R.id.description_text);
            morevert = itemView.findViewById(R.id.morebtn);
            priority_text = itemView.findViewById(R.id.priotryText);
            uri_text = itemView.findViewById(R.id.uritext);
            imageView = itemView.findViewById(R.id.profile_image);
        }
    }

    private void delete(String key) {
        // Check if the key is null before proceeding
        if (key == null) {
            Log.e("Delete", "Error: key is null");
            Toast.makeText(context, "Error: Unable to delete note, invalid key", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("AddNotes");

        // Proceed to delete if the key is valid
        dbref.child(key).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show();

                    // Optionally, remove it from the local list and notify the adapter
                    for (int i = 0; i < noteList.size(); i++) {
                        if (noteList.get(i).getKey().equals(key)) {
                            noteList.remove(i);
                            notifyItemRemoved(i);  // More efficient than notifyDataSetChanged()
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete note", Toast.LENGTH_SHORT).show();
                });
    }

}
