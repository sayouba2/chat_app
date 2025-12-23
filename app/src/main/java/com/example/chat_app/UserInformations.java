package com.example.chat_app;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInformations extends navbarActivity {

    private CircleImageView profileImg;
    private TextInputEditText editName, editPseudo;
    private Button btnSave;
    private FirebaseFirestore db;
    private String currentUid;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_informations);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        profileImg = findViewById(R.id.profile_image_edit);
        editName = findViewById(R.id.edit_name);
        editPseudo = findViewById(R.id.edit_pseudo);
        btnSave = findViewById(R.id.btn_save_profile);

        loadUserData();

        btnSave.setOnClickListener(v -> updateProfile());

        findViewById(R.id.btn_change_photo).setOnClickListener(v -> {
            // Appelle ta fonction openFileChooser() ici
        });
    }

    private void loadUserData() {
        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        editName.setText(doc.getString("name"));
                        editPseudo.setText(doc.getString("pseudo"));
                        // Utilise Glide pour charger l'image doc.getString("image")
                    }
                });
    }

    private void updateProfile() {
        String newName = editName.getText().toString();
        String newPseudo = editPseudo.getText().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("pseudo", newPseudo);

        db.collection("users").document(currentUid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil mis à jour !", Toast.LENGTH_SHORT).show();
                    finish(); // Retourne à l'écran précédent
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show());
    }
}