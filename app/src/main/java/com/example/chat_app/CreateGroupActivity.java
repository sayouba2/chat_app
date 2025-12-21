package com.example.chat_app;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
public class CreateGroupActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private CircleImageView imgGroup;
    private EditText etGroupName;
    private RecyclerView recyclerView;
    private FloatingActionButton fabCreate;
    private String selectedPresetImage = "groupe_image_1"; // Par défaut
    private boolean isGalleryImageSelected = false; // Pour savoir quoi sauvegarder

    private SelectUserAdapter adapter;
    private List<Discussion> userList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String myUid;
    private Uri imageUri;
    private void showImageSelectionDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_image);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Gestion des clics sur les 6 images prédéfinies
        int[] ids = {R.id.img_preset_1, R.id.img_preset_2, R.id.img_preset_3,
                R.id.img_preset_4, R.id.img_preset_5, R.id.img_preset_6};

        String[] presetNames = {"groupe_image_1", "groupe_image_2", "groupe_image_3",
                "groupe_image_4", "groupe_image_5", "groupe_image_6"};

        for (int i = 0; i < ids.length; i++) {
            final int index = i;
            dialog.findViewById(ids[i]).setOnClickListener(v -> {
                // L'utilisateur a choisi un preset
                selectedPresetImage = presetNames[index];
                isGalleryImageSelected = false;
                imageUri = null;

                // Mettre à jour l'aperçu (On utilise getIdentifier pour trouver le drawable par son nom)
                int resId = getResources().getIdentifier(selectedPresetImage, "drawable", getPackageName());
                imgGroup.setImageResource(resId);

                dialog.dismiss();
            });
        }

        // Gestion du bouton Galerie
        dialog.findViewById(R.id.btn_choose_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            openFileChooser(); // Votre ancienne fonction
        });

        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        myUid = mAuth.getCurrentUser().getUid();

        initViews();
        setupRecyclerView();
        loadUsers();

        // Sélectionner une image pour le groupe
        imgGroup.setOnClickListener(v -> openFileChooser());

        // Créer le groupe
        fabCreate.setOnClickListener(v -> {
            if (validateForm()) {
                createGroup();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back_group);
        imgGroup = findViewById(R.id.image_group_preview);
        etGroupName = findViewById(R.id.et_group_name);
        recyclerView = findViewById(R.id.recycler_add_participants);
        fabCreate = findViewById(R.id.fab_create_group);
    }

    private void setupRecyclerView() {
        userList = new ArrayList<>();
        adapter = new SelectUserAdapter(this, userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadUsers() {
        // Charge tous les utilisateurs sauf moi
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    String uid = doc.getString("uid");
                    // On ne s'affiche pas soi-même dans la liste
                    if (!myUid.equals(uid)) {
                        String nom = doc.getString("name");
                        String image = doc.getString("image");
                        String pseudo = doc.getString("pseudo");

                        // On utilise la classe Discussion pour stocker temporairement les infos user
                        Discussion user = new Discussion(nom, "@" + pseudo, "", image, false, uid);
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgGroup.setImageURI(imageUri);

            // On note que c'est une image galerie
            isGalleryImageSelected = true;
        }
    }

    private boolean validateForm() {
        if (TextUtils.isEmpty(etGroupName.getText())) {
            etGroupName.setError("Nom du groupe requis");
            return false;
        }
        if (adapter.selectedUids.isEmpty()) {
            Toast.makeText(this, "Sélectionnez au moins un participant", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createGroup() {
        fabCreate.setEnabled(false);
        Toast.makeText(this, "Création du groupe...", Toast.LENGTH_SHORT).show();

        if (isGalleryImageSelected && imageUri != null) {
            // CAS 1 : L'utilisateur a choisi une image de sa galerie -> On Upload
            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("group_images/" + System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                            saveGroupToFirestore(uri.toString())
                    )
            ).addOnFailureListener(e -> {
                // Erreur upload -> on met l'image 1 par défaut
                saveGroupToFirestore("groupe_image_1");
            });
        } else {
            // CAS 2 : L'utilisateur a choisi un preset (ou rien changé) -> Pas d'upload
            // On sauvegarde direct le NOM de l'image (ex: "groupe_image_3")
            saveGroupToFirestore(selectedPresetImage);
        }
    }

    private void saveGroupToFirestore(String groupImageUrl) {
        String groupName = etGroupName.getText().toString().trim();
        List<String> participants = new ArrayList<>(adapter.selectedUids);
        participants.add(myUid); // Je m'ajoute aux participants

        // Données du groupe
        Map<String, Object> groupMap = new HashMap<>();
        groupMap.put("groupName", groupName);
        groupMap.put("groupImage", groupImageUrl);
        groupMap.put("createdBy", myUid);
        groupMap.put("participants", participants);
        groupMap.put("lastMessage", "Groupe créé");
        groupMap.put("timestamp", new Timestamp(new Date()));
        groupMap.put("type", "group");

        // Création dans la collection "Groups"
        db.collection("Groups").add(groupMap).addOnSuccessListener(documentReference -> {
            String groupId = documentReference.getId();

            // Ajouter le groupe dans la liste de conversations de CHAQUE participant
            for (String participantId : participants) {
                addConversationToUser(participantId, groupId, groupName, groupImageUrl);
            }

            Toast.makeText(CreateGroupActivity.this, "Groupe créé !", Toast.LENGTH_SHORT).show();
            finish(); // Retour à l'accueil
        });
    }

    private void addConversationToUser(String userId, String groupId, String groupName, String groupImage) {
        Map<String, Object> convMap = new HashMap<>();
        convMap.put("uid", groupId); // L'ID est celui du groupe
        convMap.put("name", groupName);
        convMap.put("imageUrl", groupImage);
        convMap.put("type", "group"); // Important pour la suite
        convMap.put("lastMessage", "Groupe créé");
        convMap.put("timestamp", new Timestamp(new Date()));

        db.collection("Conversations").document(userId)
                .collection("chats").document(groupId)
                .set(convMap);
    }

    // Le reste (saveGroupToFirestore) reste identique.
    // Firestore stockera soit "https://firebasestorage...", soit "groupe_image_X"
}

