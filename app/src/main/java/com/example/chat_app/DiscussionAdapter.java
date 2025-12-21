package com.example.chat_app;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // <--- IMPORTANT : Import Glide
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DiscussionAdapter extends RecyclerView.Adapter<DiscussionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Discussion discussion);
    }

    private List<Discussion> discussionList;
    private Context context;
    private OnItemClickListener listener;

    public DiscussionAdapter(List<Discussion> discussionList, Context context, OnItemClickListener listener) {
        this.discussionList = discussionList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discussion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Discussion discussion = discussionList.get(position);

        // 1. Textes
        holder.txtNom.setText(discussion.getNom());
        holder.txtMessage.setText(discussion.getDernierMessage());
        holder.txtHeure.setText(discussion.getHeure());

        // 2. Gestion de l'Image avec GLIDE (Internet)
        // On vérifie si l'URL est valide, sinon on met l'image par défaut
        String photoUrl = discussion.getPhotoUrl();

        if (photoUrl != null && !photoUrl.isEmpty()) {
            if (photoUrl.startsWith("http")) {
                // C'est une URL Internet (Galerie ou Profil user)
                Glide.with(context).load(photoUrl).placeholder(R.drawable.profile).into(holder.imgAvatar);
            } else {
                // C'est une image locale (groupe_image_1, etc.)
                // On cherche l'ID de la ressource drawable à partir du nom string
                int resId = context.getResources().getIdentifier(photoUrl, "drawable", context.getPackageName());

                if (resId != 0) {
                    // L'image existe dans vos drawables
                    holder.imgAvatar.setImageResource(resId);
                } else {
                    // Image introuvable, on met celle par défaut
                    holder.imgAvatar.setImageResource(R.drawable.groupe_image_1);
                }
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.profile);
        }

        // 3. Gestion du "Non Lu"
        if (discussion.isNonLu()) { // J'ai corrigé "isEstNonLu" par "isNonLu" selon ton modèle
            holder.txtMessage.setTypeface(null, Typeface.BOLD);
            holder.txtMessage.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.txtNom.setTypeface(null, Typeface.BOLD);
        } else {
            holder.txtMessage.setTypeface(null, Typeface.NORMAL);
            holder.txtMessage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.txtNom.setTypeface(null, Typeface.BOLD);
        }

        // 4. Clic
        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(discussion);
        });
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UsersStatus").child(discussion.getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("online".equals(status)) {
                        // Affichez une petite pastille verte (imageView) que vous devez ajouter dans item_discussion.xml
                        holder.imgStatusOn.setVisibility(View.VISIBLE);
                        holder.imgStatusOff.setVisibility(View.GONE);
                    } else {
                        holder.imgStatusOn.setVisibility(View.GONE);
                        holder.imgStatusOff.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public int getItemCount() {
        return discussionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtNom, txtMessage, txtHeure;
        public ImageView imgAvatar;
        public View imgStatusOn;
        public View imgStatusOff;
        public ViewHolder(View itemView) {
            super(itemView);
            txtNom = itemView.findViewById(R.id.txtNom);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtHeure = itemView.findViewById(R.id.txtHeure);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgStatusOn = itemView.findViewById(R.id.img_status_on);
            imgStatusOff = itemView.findViewById(R.id.img_status_off);
        }
    }
}