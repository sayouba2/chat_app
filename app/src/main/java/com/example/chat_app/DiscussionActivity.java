package com.example.chat_app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.firebase.Timestamp;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DiscussionActivity extends navbarActivity {

    private RecyclerView recyclerView;
    private DiscussionAdapter adapter;
    private List<Discussion> maListeDeDiscussions;
    private MaterialButton btnCompose;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Assurez-vous que l'utilisateur est bien connecté
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Rediriger vers l'écran de connexion si non connecté
            return;
        }
        myUid = currentUser.getUid();

        // 2. Setup RecyclerView Principal (Conversations en cours)
        recyclerView = findViewById(R.id.recyclerViewDiscussions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        maListeDeDiscussions = new ArrayList<>();

        adapter = new DiscussionAdapter(maListeDeDiscussions, this, new DiscussionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Discussion discussion) {
                // Ouverture du Chat
                Intent intent = new Intent(DiscussionActivity.this, ChatActivity.class);
                intent.putExtra("uid_destinataire", discussion.getUid());
                intent.putExtra("nom_destinataire", discussion.getNom());
                intent.putExtra("image_destinataire", discussion.getPhotoUrl());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        // 3. Gestion du bouton "Écrire"
        btnCompose = findViewById(R.id.btnCompose);
        btnCompose.setOnClickListener(v -> showAllUsersDialog());

        // 4. Lancer le chargement des discussions existantes
        chargerLesDiscussionsExistantes(); // <--- NOUVEAU CHARGEMENT
    }

    // NOUVELLE MÉTHODE POUR CHARGER LA VRAIE LISTE DE CONVERSATIONS
    private void chargerLesDiscussionsExistantes() {
        if (myUid == null) return;

        // Écoutez la sous-collection où sont stockées vos conversations récentes
        db.collection("Conversations")
                .document(myUid)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Trie par le plus récent
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return; // Gérer l'erreur silencieusement ou avec un Log
                    }

                    if (value != null) {
                        maListeDeDiscussions.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String uidDestinataire = doc.getString("uid");
                            String nom = doc.getString("name");
                            String dernierMessage = doc.getString("lastMessage");
                            String imageUrl = doc.getString("imageUrl");

                            // --- MODIFICATION ICI : GESTION DE L'HEURE ---
                            String heureFormattee = "";
                            Timestamp timestampObj = doc.getTimestamp("timestamp");

                            if (timestampObj != null) {
                                Date date = timestampObj.toDate();
                                // Format simple : "14:30"
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                heureFormattee = sdf.format(date);
                            }
                            // ---------------------------------------------

                            // Création de l'objet Discussion avec la vraie heure
                            Discussion discussion = new Discussion(
                                    nom,
                                    dernierMessage != null ? dernierMessage : "",
                                    heureFormattee, // On passe la variable ici au lieu de "Heure"
                                    imageUrl,
                                    false, // Logique "non lu" à venir
                                    uidDestinataire
                            );

                            maListeDeDiscussions.add(discussion);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // Votre méthode showAllUsersDialog reste inchangée
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

                Intent intent = new Intent(DiscussionActivity.this, ChatActivity.class);
                intent.putExtra("uid_destinataire", discussion.getUid());
                intent.putExtra("nom_destinataire", discussion.getNom());
                intent.putExtra("image_destinataire", discussion.getPhotoUrl());
                startActivity(intent);
            }
        });
        recyclerUsers.setAdapter(userAdapter);

        // Charger TOUS les utilisateurs
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    String uid = doc.getString("uid");
                    String pseudo = doc.getString("pseudo");

                    if (!myUid.equals(uid)) {
                        String nom = doc.getString("name");
                        String image = doc.getString("image");

                        userList.add(new Discussion(
                                nom,
                                "@" + pseudo,
                                "",
                                image,
                                false,
                                uid
                        ));
                    }
                }
                userAdapter.notifyDataSetChanged();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}