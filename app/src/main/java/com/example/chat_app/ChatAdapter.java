package com.example.chat_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;
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

        // Vérification du type (si null, on considère que c'est du texte pour la compatibilité)
        String type = chat.getType();
        if (type == null) type = "text";

        if (type.equals("image")) {
            // C'est une IMAGE
            holder.show_message.setVisibility(View.GONE); // On cache le texte
            holder.img_message.setVisibility(View.VISIBLE); // On montre l'image

            try {
                // Décodage Base64 -> Bitmap
                byte[] decodedString = Base64.decode(chat.getMessage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.img_message.setImageBitmap(decodedByte);
            } catch (Exception e) {
                // Erreur de décodage
            }

        } else {
            // C'est du TEXTE
            holder.show_message.setVisibility(View.VISIBLE);
            holder.img_message.setVisibility(View.GONE);
            holder.show_message.setText(chat.getMessage());
        }
        if (getItemViewType(position) == MSG_TYPE_RIGHT) {
            // C'est mon message
            if (holder.txt_seen != null) { // Vérification de sécurité
                holder.txt_seen.setVisibility(View.VISIBLE);

                if (chat.isSeen()) {
                    holder.txt_seen.setText("Vu");
                    holder.txt_seen.setTextColor(context.getResources().getColor(android.R.color.holo_blue_light)); // Bleu
                } else {
                    holder.txt_seen.setText("Envoyé");
                    holder.txt_seen.setTextColor(context.getResources().getColor(android.R.color.darker_gray)); // Gris
                }
            }
        } else {
            // C'est le message de l'autre, on n'affiche pas "Vu" ou "Envoyé" en dessous
            if (holder.txt_seen != null) {
                holder.txt_seen.setVisibility(View.GONE);
            }
        }
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
        public ImageView img_message; // Nouvelle variable
        public TextView txt_seen;
        public ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            // Assurez-vous d'avoir ajouté cet ID dans vos XML item_chat_right et left
            img_message = itemView.findViewById(R.id.img_message);
            txt_seen = itemView.findViewById(R.id.txt_seen);
        }
    }
}