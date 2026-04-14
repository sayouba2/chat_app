package com.example.chat_app;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

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

    // Écouteur temps réel pour les demandes reçues
    private ListenerRegistration receivedRequestsListener;

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
        startReceivedRequestsListener();
    }

    // Écouteur temps réel : dès qu'une demande arrive, la liste se met à jour
    private void startReceivedRequestsListener() {
        if (myUid == null) return;
        receivedRequestsListener = db.collection("FriendRequests")
                .whereEqualTo("to", myUid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    receivedRequestsIds.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String from = doc.getString("from");
                        if (from != null) receivedRequestsIds.add(from);
                    }
                    sortAndNotify();
                });
    }

    private void loadMyFriends() {
        if (myUid == null) return;
        db.collection("users").document(myUid).collection("Friends").get()
                .addOnSuccessListener(snapshots -> {
                    friendsListIds.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        friendsListIds.add(doc.getId());
                    }
                    loadSentRequests();
                });
    }

    private void loadSentRequests() {
        db.collection("FriendRequests").whereEqualTo("from", myUid).get()
                .addOnSuccessListener(snapshots -> {
                    sentRequestsIds.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        sentRequestsIds.add(doc.getString("to"));
                    }
                    loadAllUsers();
                });
    }

    private void loadAllUsers() {
        db.collection("users").get().addOnSuccessListener(snapshots -> {
            allUsers.clear();
            for (DocumentSnapshot doc : snapshots) {
                String uid = doc.getString("uid");
                if (uid != null && !uid.equals(myUid)) {
                    String nom = doc.getString("name");
                    String pseudo = doc.getString("pseudo");
                    String image = doc.getString("image");
                    allUsers.add(new Discussion(nom, "@" + pseudo, "", image, false, uid));
                }
            }
            sortAndNotify();
        });
    }

    // Trie la liste : demandes reçues en tête, puis le reste
    private void sortAndNotify() {
        allUsers.sort((a, b) -> {
            boolean aReceived = receivedRequestsIds.contains(a.getUid());
            boolean bReceived = receivedRequestsIds.contains(b.getUid());
            if (aReceived && !bReceived) return -1;
            if (!aReceived && bReceived) return 1;
            return 0;
        });
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receivedRequestsListener != null) {
            receivedRequestsListener.remove();
        }
    }
}
