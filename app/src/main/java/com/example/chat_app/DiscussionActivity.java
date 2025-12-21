package com.example.chat_app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Attention ImageButton maintenant dans le XML
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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

public class DiscussionActivity extends navbarActivity {

    // On sépare les vues
    private RecyclerView recyclerPrivate, recyclerGroups;
    private DiscussionAdapter adapterPrivate, adapterGroups;

    // On sépare les listes
    private List<Discussion> privateList;
    private List<Discussion> groupList;

    // Listes complètes pour la recherche (copie de sauvegarde)
    private List<Discussion> fullPrivateList;
    private List<Discussion> fullGroupList;

    private ImageButton btnAddFriend;
    private Button btnCompose;
    private EditText etSearch;
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
        if (currentUser == null) return;
        myUid = currentUser.getUid();

        initViews();
        setupRecyclerViews();

        // Actions Boutons
        btnCompose.setOnClickListener(v -> showAllUsersDialog());

        // NOUVEAU : Action Ajouter Ami
        btnAddFriend.setOnClickListener(v -> {
            Intent intent = new Intent(DiscussionActivity.this, FindFriendsActivity.class);
            startActivity(intent);
        });

        // Recherche
        setupSearch();

        // Chargement des données
        chargerLesDiscussionsExistantes();
    }

    private void initViews() {
        recyclerPrivate = findViewById(R.id.recyclerPrivate);
        recyclerGroups = findViewById(R.id.recyclerGroups);
        etSearch = findViewById(R.id.etSearch);
        btnCompose = findViewById(R.id.btnCompose);     // Cast automatique en Button
        btnAddFriend = findViewById(R.id.btnAddFriend);
    }

    private void setupRecyclerViews() {
        // Initialisation des listes
        privateList = new ArrayList<>();
        groupList = new ArrayList<>();
        fullPrivateList = new ArrayList<>();
        fullGroupList = new ArrayList<>();

        // Config Recycler Privé
        recyclerPrivate.setLayoutManager(new LinearLayoutManager(this));
        adapterPrivate = new DiscussionAdapter(privateList, this, discussion -> openChat(discussion));
        recyclerPrivate.setAdapter(adapterPrivate);

        // Config Recycler Groupes
        recyclerGroups.setLayoutManager(new LinearLayoutManager(this));
        adapterGroups = new DiscussionAdapter(groupList, this, discussion -> openChat(discussion));
        recyclerGroups.setAdapter(adapterGroups);
    }

    private void openChat(Discussion discussion) {
        Intent intent = new Intent(DiscussionActivity.this, ChatActivity.class);
        intent.putExtra("uid_destinataire", discussion.getUid()); // Pour un groupe, ce sera le GroupID
        intent.putExtra("nom_destinataire", discussion.getNom());
        intent.putExtra("image_destinataire", discussion.getPhotoUrl());

        // Si c'est un groupe, on peut passer un extra pour l'indiquer (optionnel si ChatActivity gère déjà par ID)
        if ("group".equals(discussion.getType())) {
            intent.putExtra("groupId", discussion.getUid());
        }

        startActivity(intent);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void filter(String text) {
        // Filtre Liste Privée
        List<Discussion> filteredPrivate = new ArrayList<>();
        if (fullPrivateList != null) {
            for (Discussion item : fullPrivateList) {
                if (item.getNom().toLowerCase().contains(text.toLowerCase())) {
                    filteredPrivate.add(item);
                }
            }
            adapterPrivate.filterList(filteredPrivate);
        }

        // Filtre Liste Groupes
        List<Discussion> filteredGroups = new ArrayList<>();
        if (fullGroupList != null) {
            for (Discussion item : fullGroupList) {
                if (item.getNom().toLowerCase().contains(text.toLowerCase())) {
                    filteredGroups.add(item);
                }
            }
            adapterGroups.filterList(filteredGroups);
        }
    }

    private void chargerLesDiscussionsExistantes() {
        if (myUid == null) return;

        db.collection("Conversations")
                .document(myUid)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        privateList.clear();
                        groupList.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String uidDestinataire = doc.getString("uid");
                            String nom = doc.getString("name");
                            String dernierMessage = doc.getString("lastMessage");
                            String imageUrl = doc.getString("imageUrl");
                            String type = doc.getString("type"); // Récupère le type (chat ou group)

                            // Gestion Heure
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
                                    uidDestinataire,
                                    type // On passe le type
                            );

                            // TRI : GROUPE ou PRIVÉ ?
                            if ("group".equals(type)) {
                                groupList.add(discussion);
                            } else {
                                privateList.add(discussion);
                            }
                        }

                        // Mettre à jour les copies complètes pour la recherche
                        fullPrivateList = new ArrayList<>(privateList);
                        fullGroupList = new ArrayList<>(groupList);

                        adapterPrivate.notifyDataSetChanged();
                        adapterGroups.notifyDataSetChanged();
                    }
                });
    }

    // Gardez votre méthode showAllUsersDialog existante telle quelle
    // ...
    private void showAllUsersDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new_chat);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView recyclerUsers = dialog.findViewById(R.id.recycler_all_users);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_dialog);

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        List<Discussion> userList = new ArrayList<>();

        DiscussionAdapter userAdapter = new DiscussionAdapter(userList, this, new DiscussionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Discussion discussion) {
                dialog.dismiss();
                openChat(discussion); // Utiliser la méthode centralisée
            }
        });
        recyclerUsers.setAdapter(userAdapter);

        // Charger TOUS les utilisateurs
        db.collection("users").document(myUid).collection("Friends").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        List<String> friendIds = new ArrayList<>();

                        // 1. Récupérer les IDs des amis
                        for (DocumentSnapshot doc : task.getResult()) {
                            friendIds.add(doc.getId());
                        }

                        if (friendIds.isEmpty()) {
                            Toast.makeText(DiscussionActivity.this, "Vous n'avez pas encore d'amis.", Toast.LENGTH_SHORT).show();
                            userAdapter.notifyDataSetChanged();
                            return;
                        }

                        // 2. Charger les détails de ces amis (Nom, Image...)
                        db.collection("users").get().addOnSuccessListener(allDocs -> {
                            for (DocumentSnapshot doc : allDocs) {
                                String uid = doc.getString("uid");
                                if (friendIds.contains(uid)) { // SI C'EST UN AMI
                                    String nom = doc.getString("name");
                                    String image = doc.getString("image");
                                    String pseudo = doc.getString("pseudo");

                                    // Note : type null pour les users dans cette liste
                                    userList.add(new Discussion(nom, "@" + pseudo, "", image, false, uid, null));
                                }
                            }
                            userAdapter.notifyDataSetChanged();
                        });
                    }
                });

        // Bouton pour aller chercher de nouveaux amis (Optionnel si déjà sur la page principale)
        Button btnFindNew = dialog.findViewById(R.id.btn_find_new_friends);
        if(btnFindNew != null) {
            btnFindNew.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(DiscussionActivity.this, FindFriendsActivity.class));
            });
        }

        if(btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
