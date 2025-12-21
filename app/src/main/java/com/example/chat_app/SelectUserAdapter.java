package com.example.chat_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectUserAdapter extends RecyclerView.Adapter<SelectUserAdapter.ViewHolder> {

    private Context context;
    private List<Discussion> userList;
    // Liste pour stocker les ID des utilisateurs sélectionnés
    public List<String> selectedUids = new ArrayList<>();

    public SelectUserAdapter(Context context, List<Discussion> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Discussion user = userList.get(position);

        holder.txtName.setText(user.getNom());

        // Charger l'image avec Glide
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().equals("default")) {
            Glide.with(context).load(user.getPhotoUrl()).into(holder.imgProfile);
        } else {
            holder.imgProfile.setImageResource(R.drawable.profile);
        }

        // Gestion de la case à cocher (CheckBox)
        // 1. On retire le listener précédent pour éviter les bugs de recyclage
        holder.checkBox.setOnCheckedChangeListener(null);

        // 2. On met l'état actuel (coché ou pas ?)
        holder.checkBox.setChecked(selectedUids.contains(user.getUid()));

        // 3. On remet le listener pour écouter les clics
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedUids.contains(user.getUid())) {
                    selectedUids.add(user.getUid());
                }
            } else {
                selectedUids.remove(user.getUid());
            }
        });

        // Permettre de cliquer sur toute la ligne pour cocher
        holder.itemView.setOnClickListener(v -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imgProfile;
        public TextView txtName;
        public CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.img_user_select_avatar);
            txtName = itemView.findViewById(R.id.tv_user_select_name);
            checkBox = itemView.findViewById(R.id.checkbox_select);
        }
    }
}
