package com.example.chat_app;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DiscussionAdapter extends RecyclerView.Adapter<DiscussionAdapter.ViewHolder> {

    // Interfaces pour les clics
    public interface OnItemClickListener {
        void onItemClick(Discussion discussion);
    }

    // --- NOUVELLE INTERFACE POUR L'APPUI LONG ---
    public interface OnItemLongClickListener {
        void onItemLongClick(Discussion discussion);
    }

    private List<Discussion> discussionList;
    private Context context;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener; // Nouvelle variable

    // --- CONSTRUCTEUR MIS À JOUR ---
    public DiscussionAdapter(List<Discussion> discussionList, Context context, OnItemClickListener clickListener, OnItemLongClickListener longClickListener) {
        this.discussionList = discussionList;
        this.context = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener; // Initialisation
    }

    public void filterList(List<Discussion> filteredList) {
        this.discussionList = filteredList;
        notifyDataSetChanged();
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

        holder.txtNom.setText(discussion.getNom());
        holder.txtMessage.setText(discussion.getDernierMessage());
        holder.txtHeure.setText(discussion.getHeure());

        // --- GESTION IMAGE ---
        String photoUrl = discussion.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            if (photoUrl.startsWith("http")) { // Image de la galerie
                Glide.with(context).load(photoUrl).placeholder(R.drawable.img).into(holder.imgAvatar);
            } else { // Image prédéfinie (ex: "img_1")
                int resId = context.getResources().getIdentifier(photoUrl, "drawable", context.getPackageName());
                holder.imgAvatar.setImageResource(resId != 0 ? resId : R.drawable.img);
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.img); // Image par défaut
        }

        // --- GESTION STATUT EN LIGNE ---
        if (discussion.getUid() != null) {
            FirebaseFirestore.getInstance().collection("users").document(discussion.getUid())
                    .addSnapshotListener((snapshot, error) -> {
                        if (error != null || snapshot == null || !snapshot.exists()) {
                            holder.imgStatusOn.setVisibility(View.GONE);
                            holder.imgStatusOff.setVisibility(View.VISIBLE);
                            return;
                        }

                        String status = snapshot.getString("status");
                        if ("online".equals(status)) {
                            holder.imgStatusOn.setVisibility(View.VISIBLE);
                            holder.imgStatusOff.setVisibility(View.GONE);
                        } else {
                            holder.imgStatusOn.setVisibility(View.GONE);
                            holder.imgStatusOff.setVisibility(View.VISIBLE);
                        }
                    });
        }

        // --- GESTION DES CLICS ---
        holder.itemView.setOnClickListener(v -> clickListener.onItemClick(discussion));

        // --- GESTION DE L'APPUI LONG ---
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(discussion);
                return true; // Indique que le clic long a été géré
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return discussionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtNom, txtMessage, txtHeure;
        public ImageView imgAvatar;
        public View imgStatusOn, imgStatusOff;

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
