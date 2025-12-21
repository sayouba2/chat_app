package com.example.chat_app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends navbarActivity {

    // Vues (maintenant déclarées au niveau de la classe)
    private ImageButton btnBack, btnSend;
    private EditText msgInput;
    private TextView pseudoTv; // <--- Correctement déclaré
    private ImageView profileIv;
    private RecyclerView recyclerView;

    // Données de l'utilisateur actuel (MOI)
    private String myName;
    private String myImage;

    // Données du destinataire (Maintenant globales pour sendMessage)
    private String receiverName; // <--- Correctement déclaré
    private String receiverImage; // <--- Correctement déclaré

    // Firebase & Data
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String myUid, otherUid, chatRoomId;

    // Adapter
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Init Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Gérer le cas où l'utilisateur n'est pas connecté
            finish();
            return;
        }
        myUid = currentUser.getUid();

        // Récup des infos (envoyées par DiscussionActivity)
        otherUid = getIntent().getStringExtra("uid_destinataire");
        receiverName = getIntent().getStringExtra("nom_destinataire"); // Attribution à la variable globale
        receiverImage = getIntent().getStringExtra("image_destinataire"); // Attribution à la variable globale

        // Générer ID conversation unique (A_B ou B_A)
        if (myUid.compareTo(otherUid) < 0) chatRoomId = myUid + "_" + otherUid;
        else chatRoomId = otherUid + "_" + myUid;

        initViews();

        // Remplir header immédiatement
        pseudoTv.setText(receiverName);
        if (receiverImage != null && !receiverImage.equals("default")) {
            Glide.with(this).load(receiverImage).into(profileIv);
        }

        // Charger les données MOI en arrière-plan (myName, myImage)
        loadCurrentUserData();

        // Setup RecyclerView
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true); // Commencer par le bas
        recyclerView.setLayoutManager(linearLayoutManager);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(ChatActivity.this, messageList);
        recyclerView.setAdapter(chatAdapter);

        // Actions
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String msg = msgInput.getText().toString();
            if (!TextUtils.isEmpty(msg)) {
                sendMessage(myUid, otherUid, msg);
            }
        });

        readMessages();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnSend = findViewById(R.id.send);
        msgInput = findViewById(R.id.message_input);
        pseudoTv = findViewById(R.id.pseudo); // <--- Correspond à la variable de classe
        profileIv = findViewById(R.id.image_profile);
        recyclerView = findViewById(R.id.recycler_chat);
    }

    // Récupère mon nom et ma photo pour mettre à jour la liste des conversations de l'autre
    private void loadCurrentUserData() {
        db.collection("users").document(myUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        myName = documentSnapshot.getString("name");
                        myImage = documentSnapshot.getString("image");
                        // Les variables globales sont maintenant remplies
                    }
                });
    }

    private Map<String, Object> createConversationMap(String otherUid, String otherName, String lastMessage, String otherImage) {
        Map<String, Object> convMap = new HashMap<>();
        convMap.put("uid", otherUid);
        convMap.put("name", otherName);
        convMap.put("lastMessage", lastMessage);
        convMap.put("imageUrl", otherImage);
        convMap.put("timestamp", new Timestamp(new Date()));
        return convMap;
    }

    private void sendMessage(String sender, String receiver, String message) {
        ChatMessage chatMessage = new ChatMessage(sender, receiver, message, new Timestamp(new Date()));

        // 1. Ajouter message dans la collection "chats -> [roomID] -> messages"
        db.collection("chats").document(chatRoomId).collection("messages")
                .add(chatMessage);

        // 2. Mise à jour de la liste de conversations pour MOI (Sender)
        db.collection("Conversations").document(sender).collection("chats").document(receiver)
                .set(createConversationMap(receiver, receiverName, message, receiverImage)); // Utilise les variables globales receiverName/Image

        // 3. Mise à jour de la liste de conversations pour L'AUTRE (Receiver)
        // Note: myName et myImage sont utilisés ici, ils doivent avoir été chargés par loadCurrentUserData
        if (myName != null && myImage != null) {
            db.collection("Conversations").document(receiver).collection("chats").document(sender)
                    .set(createConversationMap(sender, myName, message, myImage));
        } else {
            Toast.makeText(this, "Erreur: Mes données ne sont pas encore chargées. Réessayez.", Toast.LENGTH_SHORT).show();
        }


        msgInput.setText("");
    }

    private void readMessages() {
        db.collection("chats").document(chatRoomId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                messageList.add(dc.getDocument().toObject(ChatMessage.class));
                                chatAdapter.notifyItemInserted(messageList.size() - 1);
                                recyclerView.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                });
    }
}