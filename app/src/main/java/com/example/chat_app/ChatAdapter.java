package com.example.chat_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    // Constantes pour savoir si c'est gauche ou droite
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<ChatMessage> chatList;
    private String currentUserUrl; // Pour afficher l'image si besoin

    public ChatAdapter(Context context, List<ChatMessage> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            // Layout pour MOI (Droite)
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false);
            return new ViewHolder(view);
        } else {
            // Layout pour L'AUTRE (Gauche)
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage chat = chatList.get(position);
        holder.show_message.setText(chat.getMessage());
        // Tu peux ajouter l'heure ici si tu veux
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Si l'envoyeur c'est moi -> Droite, sinon -> Gauche
        if (chatList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;

        public ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
        }
    }
}