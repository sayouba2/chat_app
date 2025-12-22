package com.example.chat_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ImageButton btnBack, btnSend, btnFile;
    private EditText msgInput;
    private TextView pseudoTv;
    private ImageView profileIv;
    private RecyclerView recyclerView;

    private String myName, myImage;
    private String receiverName, receiverImage;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String myUid, otherUid, chatRoomId;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        myUid = currentUser.getUid();

        // Récupération des infos envoyées par DiscussionActivity
        otherUid = getIntent().getStringExtra("uid_destinataire");
        receiverName = getIntent().getStringExtra("nom_destinataire");
        receiverImage = getIntent().getStringExtra("image_destinataire");

        // Génération de l'ID de la conversation (unique et constant)
        if (myUid.compareTo(otherUid) < 0) {
            chatRoomId = myUid + "_" + otherUid;
        } else {
            chatRoomId = otherUid + "_" + myUid;
        }

        initViews();
        setupHeader(); // Nouvelle fonction pour charger le pseudo et l'image
        loadCurrentUserData();
        setupRecyclerView();
        setupListeners();
        readMessages();
        seenMessage(otherUid);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnSend = findViewById(R.id.send);
        btnFile = findViewById(R.id.file);
        msgInput = findViewById(R.id.message_input);
        pseudoTv = findViewById(R.id.pseudo);
        profileIv = findViewById(R.id.image_profile);
        recyclerView = findViewById(R.id.recycler_chat);
    }

    private void setupHeader() {
        // Afficher le pseudo du destinataire
        pseudoTv.setText(receiverName);

        // --- GESTION DE L'IMAGE DE PROFIL (LOGIQUE CORRIGÉE) ---
        if (receiverImage != null && !receiverImage.isEmpty()) {
            if (receiverImage.startsWith("http")) {
                // Si l'image est une URL (téléchargée depuis la galerie), on utilise Glide
                Glide.with(this).load(receiverImage).placeholder(R.drawable.img).into(profileIv);
            } else {
                // Si c'est un nom d'image prédéfinie (ex: "img_1"), on la cherche dans les drawables
                int resId = getResources().getIdentifier(receiverImage, "drawable", getPackageName());
                if (resId != 0) {
                    profileIv.setImageResource(resId);
                } else {
                    profileIv.setImageResource(R.drawable.img); // Image par défaut si non trouvée
                }
            }
        } else {
            // Si aucune image n'est définie, on met celle par défaut
            profileIv.setImageResource(R.drawable.img);
        }
    }

    private void loadCurrentUserData() {
        db.collection("users").document(myUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        myName = documentSnapshot.getString("name");
                        myImage = documentSnapshot.getString("image");
                    }
                });
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true); // Affiche les messages en partant du bas
        recyclerView.setLayoutManager(linearLayoutManager);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(ChatActivity.this, messageList);
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFile.setOnClickListener(v -> openFileChooser());
        btnSend.setOnClickListener(v -> {
            String msg = msgInput.getText().toString().trim();
            if (!TextUtils.isEmpty(msg)) {
                sendMessage(myUid, otherUid, msg, "text");
            }
        });
    }

    private void sendMessage(String sender, String receiver, String message, String type) {
        ChatMessage chatMessage = new ChatMessage(sender, receiver, message, new Timestamp(new Date()), type, false);

        db.collection("chats").document(chatRoomId).collection("messages")
                .add(chatMessage);

        String lastMsgPreview = type.equals("image") ? "📷 Photo" : message;

        // Mise à jour de ma liste de conversations
        db.collection("Conversations").document(sender).collection("chats").document(receiver)
                .set(createConversationMap(receiver, receiverName, lastMsgPreview, receiverImage));

        // Mise à jour de la liste de conversations du destinataire
        if (myName != null && myImage != null) {
            db.collection("Conversations").document(receiver).collection("chats").document(sender)
                    .set(createConversationMap(sender, myName, lastMsgPreview, myImage));
        }

        msgInput.setText("");
    }

    // Le reste du fichier (readMessages, seenMessage, onActivityResult, etc.) ne change pas.
    // ...
    private void seenMessage(String userId) {
        db.collection("chats").document(chatRoomId).collection("messages")
                .whereEqualTo("senderId", otherUid)
                .whereEqualTo("isSeen", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            doc.getReference().update("isSeen", true);
                        }
                    }
                });
    }

    private void readMessages() {
        db.collection("chats").document(chatRoomId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        switch (dc.getType()) {
                            // CAS 1 : NOUVEAU MESSAGE (Ce que vous aviez déjà)
                            case ADDED:
                                ChatMessage msg = dc.getDocument().toObject(ChatMessage.class);
                                msg.setDocumentId(dc.getDocument().getId()); // IMPORTANT : On stocke l'ID
                                messageList.add(msg);
                                chatAdapter.notifyItemInserted(messageList.size() - 1);
                                recyclerView.smoothScrollToPosition(messageList.size() - 1);
                                break;

                            // CAS 2 : MESSAGE MODIFIÉ (C'est ce qui manquait !)
                            // C'est ici que le statut passe de "non vu" à "vu"
                            case MODIFIED:
                                ChatMessage changedMsg = dc.getDocument().toObject(ChatMessage.class);
                                String changedDocId = dc.getDocument().getId();

                                // On cherche le message modifié dans notre liste locale
                                for (int i = 0; i < messageList.size(); i++) {
                                    ChatMessage existingMsg = messageList.get(i);

                                    // On compare les IDs pour trouver le bon message
                                    if (existingMsg.getDocumentId() != null && existingMsg.getDocumentId().equals(changedDocId)) {
                                        // On met à jour le statut dans la liste
                                        existingMsg.setSeen(changedMsg.isSeen());

                                        // On dit à l'adaptateur de rafraîchir SEULEMENT cette ligne
                                        chatAdapter.notifyItemChanged(i);
                                        break; // On a trouvé, on arrête de chercher
                                    }
                                }
                                break;
                        }
                    }
                });
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400, true);
                String base64Image = encodeImage(resizedBitmap);
                sendMessage(myUid, otherUid, base64Image, "image");
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors de l'envoi de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
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
}
