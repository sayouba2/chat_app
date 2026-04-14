package com.example.chat_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInformations extends navbarActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

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

        findViewById(R.id.btn_change_photo).setOnClickListener(v -> openFileChooser());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImg.setImageURI(imageUri);
        }
    }

    private void loadUserData() {
        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        editName.setText(doc.getString("name"));
                        editPseudo.setText(doc.getString("pseudo"));

                        String image = doc.getString("image");
                        if (image != null && !image.isEmpty()) {
                            if (image.startsWith("http")) {
                                Glide.with(this).load(image).placeholder(R.drawable.img).into(profileImg);
                            } else {
                                int resId = getResources().getIdentifier(image, "drawable", getPackageName());
                                profileImg.setImageResource(resId != 0 ? resId : R.drawable.img);
                            }
                        }
                    }
                });
    }

    private void updateProfile() {
        String newName = editName.getText().toString().trim();
        String newPseudo = editPseudo.getText().toString().trim();

        if (newName.isEmpty() || newPseudo.isEmpty()) {
            Toast.makeText(this, "Le nom et le pseudo sont requis", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        if (imageUri != null) {
            // Upload de la nouvelle photo, puis mise à jour du profil
            StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                    .child("profile_images/" + currentUid + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(snap -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> saveProfileData(newName, newPseudo, uri.toString())))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur upload photo : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                    });
        } else {
            saveProfileData(newName, newPseudo, null);
        }
    }

    private void saveProfileData(String name, String pseudo, String newImageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("pseudo", pseudo);
        if (newImageUrl != null) {
            updates.put("image", newImageUrl);
        }

        db.collection("users").document(currentUid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profil mis à jour !", Toast.LENGTH_SHORT).show();
                    imageUri = null;
                    btnSave.setEnabled(true);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                });
    }
}
