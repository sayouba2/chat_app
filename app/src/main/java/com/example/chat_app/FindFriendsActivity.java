package com.example.chat_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FindFriendsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FindFriendsAdapter adapter;

    private List<Discussion> allUsers;
    private List<String> friendsListIds;
    private List<String> sentRequestsIds;
    private List<String> receivedRequestsIds;

    private FirebaseFirestore db;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        recyclerView = findViewById(R.id.recycler_find_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allUsers = new ArrayList<>();
        friendsListIds = new ArrayList<>();
        sentRequestsIds = new ArrayList<>();
        receivedRequestsIds = new ArrayList<>();

        // On initialise l'adapter
        adapter = new FindFriendsAdapter(this, allUsers, friendsListIds, sentRequestsIds, receivedRequestsIds);
        recyclerView.setAdapter(adapter);

        // Chargement des données en cascade
        loadMyFriends();
    }

    // 1. Charger mes amis existants
    private void loadMyFriends() {
        if (myUid == null) return;

        db.collection("users").document(myUid).collection("Friends").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    friendsListIds.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        friendsListIds.add(doc.getId());
                    }
                    loadSentRequests();
                });
    }

    // 2. Charger les demandes que j'ai envoyées
    private void loadSentRequests() {
        db.collection("FriendRequests").whereEqualTo("from", myUid).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sentRequestsIds.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        sentRequestsIds.add(doc.getString("to"));
                    }
                    loadReceivedRequests();
                });
    }

    // 3. Charger les demandes que j'ai reçues
    private void loadReceivedRequests() {
        db.collection("FriendRequests").whereEqualTo("to", myUid).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    receivedRequestsIds.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        receivedRequestsIds.add(doc.getString("from"));
                    }
                    loadAllUsers();
                });
    }

    // 4. Charger enfin tous les utilisateurs pour afficher la liste
    private void loadAllUsers() {
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allUsers.clear();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String uid = doc.getString("uid");
                // Vérifier que uid n'est pas null et que ce n'est pas moi
                if (uid != null && !uid.equals(myUid)) {
                    String nom = doc.getString("name");
                    String pseudo = doc.getString("pseudo");
                    String image = doc.getString("image");

                    // --- CORRECTION ICI ---
                    // Ajout de 'null' comme dernier paramètre pour le type
                    Discussion user = new Discussion(nom, "@" + pseudo, "", image, false, uid, null);

                    allUsers.add(user);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }
}
