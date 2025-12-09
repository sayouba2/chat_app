package com.example.chat_app; // <--- ATTENTION : Remplace "tonapplication" par le vrai nom de ton dossier

// 1. VOICI LES IMPORTATIONS QUI MANQUAIENT (La boîte à outils)
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Début de la classe Adapter
public class DiscussionAdapter extends RecyclerView.Adapter<DiscussionAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Discussion discussion);
    }
    // Liste des données (Le Menu)
    private List<Discussion> discussionList;
    private Context context;
    private OnItemClickListener listener; // --- 2. VARIABLE DU LISTENER ---
    // Constructeur
    public DiscussionAdapter(List<Discussion> discussionList, Context context,OnItemClickListener listener) {
        this.discussionList = discussionList;
        this.context = context;
        this.listener=listener;
    }

    // Étape 1 : Création de la vue (Sortir l'assiette vide)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // On "gonfle" (inflate) le fichier XML item_discussion pour en faire un objet Java
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discussion, parent, false);
        return new ViewHolder(view);
    }

    // Étape 2 : Remplissage (Mettre la nourriture dans l'assiette)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // On récupère l'objet Discussion à la position actuelle
        Discussion discussion = discussionList.get(position);

        // On remplit les champs avec les getters
        holder.txtNom.setText(discussion.getNom());
        holder.txtMessage.setText(discussion.getDernierMessage());
        holder.txtHeure.setText(discussion.getHeure());
        holder.imgAvatar.setImageResource(discussion.getPhotoResId());

        // --- GESTION DU "NON LU" (Gras vs Normal) ---
        // Si le message est NON LU, on met le texte en GRAS et noir foncé
        if (discussion.isEstNonLu()) {
            holder.txtMessage.setTypeface(null, Typeface.BOLD);
            holder.txtMessage.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.txtNom.setTypeface(null, Typeface.BOLD);
        } else {
            // Sinon, style normal et gris
            holder.txtMessage.setTypeface(null, Typeface.NORMAL);
            holder.txtMessage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.txtNom.setTypeface(null, Typeface.BOLD); // Le nom reste souvent en gras
        }
        // --- 4. GESTION DU CLIC ---
        // Quand on clique sur l'élément entier (itemView)
        holder.itemView.setOnClickListener(v -> {
            // On déclenche la méthode de l'interface
            listener.onItemClick(discussion);
        });
    }

    // Étape 3 : Compter les éléments
    @Override
    public int getItemCount() {
        return discussionList.size();
    }

    // --- CLASSE INTERNE VIEWHOLDER (Le support d'assiette) ---
    // Elle sert à mémoriser les liens vers les éléments graphiques pour ne pas les rechercher à chaque fois
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtNom, txtMessage, txtHeure;
        public ImageView imgAvatar;

        public ViewHolder(View itemView) {
            super(itemView);
            // On fait le lien avec les IDs définis dans item_discussion.xml
            txtNom = itemView.findViewById(R.id.txtNom);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtHeure = itemView.findViewById(R.id.txtHeure);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}