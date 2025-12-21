package com.example.chat_app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DiscussionActivity extends AppCompatActivity {

    private RecyclerView recyclerDiscussions;
    private DiscussionAdapter discussionAdapter;
    private List<Discussion> discussionList;
    private List<Discussion> fullDiscussionList;

    // Nouveaux composants UI
    private ImageButton btnMenu, btnAddFriend;
    private FloatingActionButton btnCompose; // Changé en FAB
    private EditText etSearch;

    // Composants de la barre du bas
    private CircleImageView bottomProfileImage;
    private TextView bottomPseudo;
    private TextView bottomFriendsCount;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        myUid = currentUser.getUid();

        initViews();
        setupRecyclerView();
        setupListeners();

        // Chargement des données
        chargerDiscussions();
        loadUserProfileData(); // Charger le profil en bas
        updateUserStatus("online");
    }

    private void initViews() {
        recyclerDiscussions = findViewById(R.id.recyclerDiscussions);
        etSearch = findViewById(R.id.etSearch);
        btnCompose = findViewById(R.id.btnCompose);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnMenu = findViewById(R.id.btnMenu);

        // Liaison de la barre du bas
        bottomProfileImage = findViewById(R.id.bottomProfileImage);
        bottomPseudo = findViewById(R.id.bottomPseudo);
        bottomFriendsCount = findViewById(R.id.bottomFriendsCount);
    }

    // --- NOUVELLE FONCTION : Charger les infos du bas ---
    private void loadUserProfileData() {
        // 1. Récupérer Pseudo et Photo
        db.collection("users").document(myUid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String pseudo = doc.getString("pseudo");
                String image = doc.getString("image");

                if (pseudo != null) bottomPseudo.setText("@" + pseudo);
                else bottomPseudo.setText("Moi");

                // Gestion image (URL ou Drawable)
                if (image != null && !image.isEmpty()) {
                    if (image.startsWith("http")) {
                        Glide.with(this).load(image).placeholder(R.drawable.img).into(bottomProfileImage);
                    } else {
                        int resId = getResources().getIdentifier(image, "drawable", getPackageName());
                        bottomProfileImage.setImageResource(resId != 0 ? resId : R.drawable.img);
                    }
                }
            }
        });

        // 2. Récupérer le nombre d'amis
        db.collection("users").document(myUid).collection("Friends").get()
                .addOnSuccessListener(snapshots -> {
                    int count = snapshots.size();
                    bottomFriendsCount.setText(String.valueOf(count));
                });
    }

    private void setupRecyclerView() {
        discussionList = new ArrayList<>();
        fullDiscussionList = new ArrayList<>();
        recyclerDiscussions.setLayoutManager(new LinearLayoutManager(this));

        discussionAdapter = new DiscussionAdapter(discussionList, this, this::openChat, this::handleDeleteConversation);
        recyclerDiscussions.setAdapter(discussionAdapter);
    }

    private void handleDeleteConversation(Discussion discussion) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Supprimer la discussion avec " + discussion.getNom() + " ?")
                .setPositiveButton("Oui", (dialog, which) -> deleteConversationFromFirestore(discussion))
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteConversationFromFirestore(Discussion discussion) {
        if (myUid == null || discussion.getUid() == null) return;
        db.collection("Conversations").document(myUid).collection("chats").document(discussion.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Supprimé", Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        // Déconnexion
        btnMenu.setOnClickListener(v -> {
            updateUserStatus("offline");
            mAuth.signOut();
            Intent intent = new Intent(DiscussionActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Nouveau message (Bouton flottant)
        btnCompose.setOnClickListener(v -> showFriendsDialog());

        // Ajouter ami (Header)
        btnAddFriend.setOnClickListener(v -> {
            startActivity(new Intent(DiscussionActivity.this, FindFriendsActivity.class));
        });

        // Recherche
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) { filter(s.toString()); }
        });
    }

    private void updateUserStatus(String status) {
        if (myUid != null) {
            db.collection("users").document(myUid).update("status", status);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
        loadUserProfileData(); // Recharger les infos (ex: si nbr amis change)
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            updateUserStatus("offline");
        }
    }

    private void chargerDiscussions() {
        if (myUid == null) return;
        db.collection("Conversations").document(myUid).collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    discussionList.clear();
                    fullDiscussionList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        if ("group".equals(doc.getString("type"))) continue;

                        String uidDestinataire = doc.getString("uid");
                        String nom = doc.getString("name");
                        String dernierMessage = doc.getString("lastMessage");
                        String imageUrl = doc.getString("imageUrl");
                        String heureFormattee = "";
                        Timestamp timestampObj = doc.getTimestamp("timestamp");
                        if (timestampObj != null) {
                            Date date = timestampObj.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            heureFormattee = sdf.format(date);
                        }

                        Discussion discussion = new Discussion(
                                nom,
                                dernierMessage != null ? dernierMessage : "",
                                heureFormattee,
                                imageUrl,
                                false,
                                uidDestinataire
                        );
                        discussionList.add(discussion);
                    }
                    fullDiscussionList.addAll(discussionList);
                    discussionAdapter.notifyDataSetChanged();
                });
    }

    private void openChat(Discussion discussion) {
        if (discussion == null || discussion.getUid() == null) return;
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("uid_destinataire", discussion.getUid());
        intent.putExtra("nom_destinataire", discussion.getNom());
        intent.putExtra("image_destinataire", discussion.getPhotoUrl());
        startActivity(intent);
    }

    private void filter(String text) {
        List<Discussion> filteredList = new ArrayList<>();
        for (Discussion item : fullDiscussionList) {
            if (item.getNom().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        discussionAdapter.filterList(filteredList);
    }

    private void showFriendsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new_chat);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView recyclerUsers = dialog.findViewById(R.id.recycler_all_users);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        List<Discussion> friendUserList = new ArrayList<>();
        DiscussionAdapter userAdapter = new DiscussionAdapter(friendUserList, this, discussion -> {
            dialog.dismiss();
            openChat(discussion);
        }, null);
        recyclerUsers.setAdapter(userAdapter);

        db.collection("users").document(myUid).collection("Friends").get()
                .addOnSuccessListener(friendDocs -> {
                    if (friendDocs.isEmpty()) {
                        Toast.makeText(this, "Ajoutez des amis d'abord !", Toast.LENGTH_SHORT).show();
                    } else {
                        List<String> friendIds = new ArrayList<>();
                        for (DocumentSnapshot doc : friendDocs) friendIds.add(doc.getId());
                        if (!friendIds.isEmpty()) {
                            db.collection("users").whereIn("uid", friendIds).get()
                                    .addOnSuccessListener(userDocs -> {
                                        for(DocumentSnapshot userDoc : userDocs) {
                                            Discussion friend = new Discussion(
                                                    userDoc.getString("name"),
                                                    "@" + userDoc.getString("pseudo"),
                                                    "",
                                                    userDoc.getString("image"),
                                                    false,
                                                    userDoc.getString("uid")
                                            );
                                            friendUserList.add(friend);
                                        }
                                        userAdapter.notifyDataSetChanged();
                                    });
                        }
                    }
                });
        dialog.findViewById(R.id.btn_cancel_dialog).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_find_new_friends).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, FindFriendsActivity.class));
        });
        dialog.show();
    }
}
