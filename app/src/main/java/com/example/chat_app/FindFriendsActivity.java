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

public class FindFriendsActivity extends navbarActivity {

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

        adapter = new FindFriendsAdapter(this, allUsers, friendsListIds, sentRequestsIds, receivedRequestsIds);
        recyclerView.setAdapter(adapter);

        loadMyFriends();
    }

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

    private void loadAllUsers() {
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allUsers.clear();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String uid = doc.getString("uid");

                if (uid != null && !uid.equals(myUid)) {
                    String nom = doc.getString("name");
                    String pseudo = doc.getString("pseudo");
                    String image = doc.getString("image");

                    // --- CORRECTION ICI ---
                    // On utilise le constructeur à 6 paramètres, sans le type.
                    Discussion user = new Discussion(nom, "@" + pseudo, "", image, false, uid);

                    allUsers.add(user);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }
}
