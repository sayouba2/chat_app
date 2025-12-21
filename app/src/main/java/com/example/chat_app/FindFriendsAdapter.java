package com.example.chat_app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.ViewHolder> {

    private Context context;
    private List<Discussion> userList;

    // Ces listes contiendront les UID pour savoir l'état de chaque personne
    private List<String> friendsList;
    private List<String> sentRequests;
    private List<String> receivedRequests;

    private FirebaseFirestore db;
    private String currentUid;

    public FindFriendsAdapter(Context context, List<Discussion> userList,
                              List<String> friendsList, List<String> sentRequests, List<String> receivedRequests) {
        this.context = context;
        this.userList = userList;
        this.friendsList = friendsList;
        this.sentRequests = sentRequests;
        this.receivedRequests = receivedRequests;

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_action, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Discussion user = userList.get(position);
        String otherUid = user.getUid();

        holder.tvName.setText(user.getNom());
        holder.tvStatus.setText("@" + user.getDernierMessage()); // On utilise ce champ pour le pseudo temporairement

        // Gestion Image
        String photoUrl = user.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            if (photoUrl.startsWith("http")) {
                Glide.with(context).load(photoUrl).placeholder(R.drawable.profile).into(holder.imgAvatar);
            } else {
                int resId = context.getResources().getIdentifier(photoUrl, "drawable", context.getPackageName());
                if(resId != 0) holder.imgAvatar.setImageResource(resId);
                else holder.imgAvatar.setImageResource(R.drawable.profile);
            }
        }

        // --- LOGIQUE DES BOUTONS ---

        if (friendsList.contains(otherUid)) {
            // DÉJÀ AMIS
            holder.btnAction.setText("Amis");
            holder.btnAction.setBackgroundColor(Color.GRAY);
            holder.btnAction.setEnabled(false); // On ne peut rien faire de plus ici
        }
        else if (sentRequests.contains(otherUid)) {
            // DEMANDE ENVOYÉE (En attente)
            holder.btnAction.setText("Annuler");
            holder.btnAction.setBackgroundColor(Color.parseColor("#FFC107")); // Orange/Jaune
            holder.btnAction.setEnabled(true);
            holder.btnAction.setOnClickListener(v -> cancelRequest(otherUid));
        }
        else if (receivedRequests.contains(otherUid)) {
            // DEMANDE REÇUE (À accepter)
            holder.btnAction.setText("Accepter");
            holder.btnAction.setBackgroundColor(Color.parseColor("#4CAF50")); // Vert
            holder.btnAction.setEnabled(true);
            holder.btnAction.setOnClickListener(v -> acceptRequest(otherUid));
        }
        else {
            // RIEN DU TOUT (Inconnu)
            holder.btnAction.setText("Ajouter");
            holder.btnAction.setBackgroundColor(Color.parseColor("#009688")); // Bleu/Teal
            holder.btnAction.setEnabled(true);
            holder.btnAction.setOnClickListener(v -> sendRequest(otherUid));
        }
    }

    // 1. Envoyer une demande
    private void sendRequest(String receiverUid) {
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("from", currentUid);
        reqMap.put("to", receiverUid);
        reqMap.put("status", "pending");

        // On utilise l'ID combiné pour éviter les doublons
        String docId = currentUid + "_" + receiverUid;

        db.collection("FriendRequests").document(docId).set(reqMap)
                .addOnSuccessListener(aVoid -> {
                    sentRequests.add(receiverUid);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Demande envoyée", Toast.LENGTH_SHORT).show();
                });
    }

    // 2. Annuler une demande
    private void cancelRequest(String receiverUid) {
        String docId = currentUid + "_" + receiverUid;
        db.collection("FriendRequests").document(docId).delete()
                .addOnSuccessListener(aVoid -> {
                    sentRequests.remove(receiverUid);
                    notifyDataSetChanged();
                });
    }

    // 3. Accepter une demande (Action Complexe)
    private void acceptRequest(String senderUid) {
        // A. Ajouter dans MES amis
        Map<String, Object> friendMap = new HashMap<>();
        friendMap.put("uid", senderUid);
        friendMap.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(currentUid).collection("Friends").document(senderUid).set(friendMap);

        // B. Ajouter dans SES amis
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("uid", currentUid);
        myMap.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(senderUid).collection("Friends").document(currentUid).set(myMap);

        // C. Supprimer la demande (L'ID était sender_me)
        String docId = senderUid + "_" + currentUid;
        db.collection("FriendRequests").document(docId).delete()
                .addOnSuccessListener(aVoid -> {
                    receivedRequests.remove(senderUid);
                    friendsList.add(senderUid);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Vous êtes maintenant amis !", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvName, tvStatus;
        Button btnAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_user_avatar);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvStatus = itemView.findViewById(R.id.tv_user_status);
            btnAction = itemView.findViewById(R.id.btn_action_user);
        }
    }
}
