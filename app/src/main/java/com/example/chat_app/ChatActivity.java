package com.example.chat_app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private ValueEventListener seenListener;
    private DatabaseReference userRefForSeen;
    // Vues (maintenant déclarées au niveau de la classe)
    private ImageButton btnBack, btnSend;
    private EditText msgInput;
    private TextView pseudoTv; // <--- Correctement déclaré
    private ImageView profileIv;
    private RecyclerView recyclerView;
    private static final int PICK_IMAGE_REQUEST = 1;
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
    private void seenMessage(String userId) {
        // On cherche les messages envoyés par L'AUTRE dans notre conversation
        // Attention : Firestore facture les écritures. Cette méthode mettra à jour tous les messages non lus.

        db.collection("chats").document(chatRoomId).collection("messages")
                .whereEqualTo("senderId", otherUid) // Seulement ceux que l'autre a envoyé
                .whereEqualTo("isSeen", false)      // Seulement ceux pas encore vus
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            // On met à jour le champ isSeen à true
                            doc.getReference().update("isSeen", true);
                        }
                    }
                });
    }
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
        if (receiverImage != null && !receiverImage.equals("default")) {
            if (receiverImage.startsWith("http")) {
                Glide.with(this).load(receiverImage).into(profileIv);
            } else {
                int resId = getResources().getIdentifier(receiverImage, "drawable", getPackageName());
                if(resId != 0) profileIv.setImageResource(resId);
            }
        }
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
        ImageButton btnFile = findViewById(R.id.file);
        btnFile.setOnClickListener(v -> openFileChooser());
        btnSend.setOnClickListener(v -> {
            String msg = msgInput.getText().toString();
            if (!TextUtils.isEmpty(msg)) {
                sendMessage(myUid, otherUid, msg, "text"); // On précise que c'est du texte
            }
        });

        readMessages();
        seenMessage(otherUid);
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                // Conversion URI -> Bitmap
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // IMPORTANT : Redimensionner l'image car Base64 est très lourd
                // Firestore limite les documents à 1MB. Une photo HD fera planter l'appli.
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);

                // Conversion Bitmap -> String Base64
                String base64Image = encodeImage(resizedBitmap);

                // Envoi immédiat
                sendMessage(myUid, otherUid, base64Image, "image");

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors de l'envoi de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Fonction utilitaire de conversion
    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Compression en JPEG qualité 50% pour réduire la taille
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // MODIFIEZ votre fonction sendMessage pour accepter le TYPE
    private void sendMessage(String sender, String receiver, String message, String type) {
        // On passe le type au constructeur
        ChatMessage chatMessage = new ChatMessage(sender, receiver, message, new Timestamp(new Date()), type, false);

        db.collection("chats").document(chatRoomId).collection("messages")
                .add(chatMessage);

        // Pour la liste des conversations, on affiche "Photo" si c'est une image
        String lastMsgPreview = type.equals("image") ? "📷 Photo" : message;

        // Mise à jour conversations (Sender)
        db.collection("Conversations").document(sender).collection("chats").document(receiver)
                .set(createConversationMap(receiver, receiverName, lastMsgPreview, receiverImage));

        // Mise à jour conversations (Receiver)
        if (myName != null && myImage != null) {
            db.collection("Conversations").document(receiver).collection("chats").document(sender)
                    .set(createConversationMap(sender, myName, lastMsgPreview, myImage));
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
                                ChatMessage msg = dc.getDocument().toObject(ChatMessage.class);
                                if (msg.getSenderId().equals(otherUid)) {
                                    seenMessage(otherUid);
                                }

                                messageList.add(dc.getDocument().toObject(ChatMessage.class));
                                chatAdapter.notifyItemInserted(messageList.size() - 1);
                                recyclerView.smoothScrollToPosition(messageList.size() - 1);
                            }
                        }
                    }
                });

    }

}