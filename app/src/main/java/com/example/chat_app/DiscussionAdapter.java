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

        if (photoUrl != null && !photoUrl.isEmpty() && !photoUrl.equals("default")) {
            Glide.with(context)
                    .load(photoUrl)
                    .placeholder(R.drawable.profile) // Image pendant le chargement
                    .circleCrop() // Arrondir l'image
                    .into(holder.imgAvatar);
        } else {
            // Si pas d'image sur Firebase, on met celle par défaut des ressources
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
    }

    @Override
    public int getItemCount() {
        return discussionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtNom, txtMessage, txtHeure;
        public ImageView imgAvatar;

        public ViewHolder(View itemView) {
            super(itemView);
            txtNom = itemView.findViewById(R.id.txtNom);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtHeure = itemView.findViewById(R.id.txtHeure);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}