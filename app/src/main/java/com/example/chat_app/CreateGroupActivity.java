package com.example.chat_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

// Imports pour le Dialog (si vous avez intégré le choix d'image)
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

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

public class CreateGroupActivity extends navbarActivity {

    private ImageButton btnBack;
    private CircleImageView imgGroup;
    private EditText etGroupName;
    private RecyclerView recyclerView;
    private FloatingActionButton fabCreate;

    private SelectUserAdapter adapter;
    private List<Discussion> userList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String myUid;

    private Uri imageUri;

    // Variables pour le choix d'image (Preset)
    private String selectedPresetImage = "groupe_image_1";
    private boolean isGalleryImageSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            myUid = mAuth.getCurrentUser().getUid();
        }

        initViews();
        setupRecyclerView();
        loadUsers();

        // Sélectionner une image pour le groupe (via Dialog)
        imgGroup.setOnClickListener(v -> showImageSelectionDialog());

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
        // Charge tous les utilisateurs (ou seulement les amis si vous avez implémenté la liste d'amis)
        // Pour rester simple ici, on charge "users". Si vous voulez seulement les amis, remplacez par :
        // db.collection("users").document(myUid).collection("Friends").get()... (voir logique DiscussionActivity)

        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    String uid = doc.getString("uid");

                    // On ne s'affiche pas soi-même
                    if (uid != null && !uid.equals(myUid)) {
                        String nom = doc.getString("name");
                        String image = doc.getString("image");
                        String pseudo = doc.getString("pseudo");

                        // --- CORRECTION ICI ---
                        // On ajoute 'null' à la fin car ces users n'ont pas de "type" de discussion spécifique
                        Discussion user = new Discussion(nom, "@" + pseudo, "", image, false, uid, null);
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    // --- Gestion Image (Dialog) ---
    private void showImageSelectionDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_image);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        int[] ids = {R.id.img_preset_1, R.id.img_preset_2, R.id.img_preset_3,
                R.id.img_preset_4, R.id.img_preset_5, R.id.img_preset_6};

        String[] presetNames = {"groupe_image_1", "groupe_image_2", "groupe_image_3",
                "groupe_image_4", "groupe_image_5", "groupe_image_6"};

        for (int i = 0; i < ids.length; i++) {
            final int index = i;
            dialog.findViewById(ids[i]).setOnClickListener(v -> {
                selectedPresetImage = presetNames[index];
                isGalleryImageSelected = false;
                imageUri = null;

                int resId = getResources().getIdentifier(selectedPresetImage, "drawable", getPackageName());
                imgGroup.setImageResource(resId);

                dialog.dismiss();
            });
        }

        dialog.findViewById(R.id.btn_choose_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            openFileChooser();
        });

        dialog.show();
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
            // Upload Image
            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("group_images/" + System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                            saveGroupToFirestore(uri.toString())
                    )
            ).addOnFailureListener(e -> {
                saveGroupToFirestore(selectedPresetImage);
            });
        } else {
            // Preset
            saveGroupToFirestore(selectedPresetImage);
        }
    }

    private void saveGroupToFirestore(String groupImageUrl) {
        String groupName = etGroupName.getText().toString().trim();
        List<String> participants = new ArrayList<>(adapter.selectedUids);
        participants.add(myUid); // Je m'ajoute

        Map<String, Object> groupMap = new HashMap<>();
        groupMap.put("groupName", groupName);
        groupMap.put("groupImage", groupImageUrl);
        groupMap.put("createdBy", myUid);
        groupMap.put("participants", participants);
        groupMap.put("lastMessage", "Groupe créé");
        groupMap.put("timestamp", new Timestamp(new Date()));
        groupMap.put("type", "group");

        db.collection("Groups").add(groupMap).addOnSuccessListener(documentReference -> {
            String groupId = documentReference.getId();

            for (String participantId : participants) {
                addConversationToUser(participantId, groupId, groupName, groupImageUrl);
            }

            Toast.makeText(CreateGroupActivity.this, "Groupe créé !", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void addConversationToUser(String userId, String groupId, String groupName, String groupImage) {
        Map<String, Object> convMap = new HashMap<>();
        convMap.put("uid", groupId);
        convMap.put("name", groupName);
        convMap.put("imageUrl", groupImage);
        convMap.put("type", "group"); // Important pour le tri
        convMap.put("lastMessage", "Groupe créé");
        convMap.put("timestamp", new Timestamp(new Date()));

        db.collection("Conversations").document(userId)
                .collection("chats").document(groupId)
                .set(convMap);
    }
}
