package com.example.chat_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DiscussionActivity extends navbarActivity {

    // --- 1. LE BOUTON "ON" (La méthode onCreate) ---
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // IMPORTANT : On relie d'abord le code au fichier XML (la vue)
        setContentView(R.layout.activity_discussion);

        // --- 2. MAINTENANT, ON PEUT DONNER DES ORDRES ---

        // A. On trouve le RecyclerView dans la salle (XML)
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDiscussions);

        // B. On organise les tables (LayoutManager)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // C. On prépare le Menu (Les Données)
        List<Discussion> maListeDeDiscussions = new ArrayList<>();

        // Ajout de Josiah (Non lu -> true)
        maListeDeDiscussions.add(new Discussion(
                "Josiah Zayner",
                "Salut ! Comment vas-tu aujourd'hui ? Je voulais faire un point...",
                "09:56",
                R.drawable.img_4,
                true
        ));

        // Ajout de Jillian (Lu -> false)
        maListeDeDiscussions.add(new Discussion(
                "Jillian Jacob",
                "Ça fait longtemps. J'espère que tout va bien...",
                "Hier",
                R.drawable.img_1,
                false
        ));

        // Ajout de Victoria (Lu -> false)
        maListeDeDiscussions.add(new Discussion(
                "Victoria Hanson",
                "Photos de vacances. Salut, j'ai rassemblé quelques photos...",
                "5 Mar",
                R.drawable.img_3,
                false
        ));
        maListeDeDiscussions.add(new Discussion(
                "John_32",
                "C'est Mieux tu abandonne l'ecole",
                "5 Mar",
                R.drawable.img_2,
                true
        ));maListeDeDiscussions.add(new Discussion(
                "John_32",
                "C'est Mieux tu abandonne l'ecole",
                "5 Mar",
                R.drawable.img_2,
                true
        ));maListeDeDiscussions.add(new Discussion(
                "John_32",
                "C'est Mieux tu abandonne l'ecole",
                "5 Mar",
                R.drawable.img_2,
                true
        ));maListeDeDiscussions.add(new Discussion(
                "John_32",
                "C'est Mieux tu abandonne l'ecole",
                "5 Mar",
                R.drawable.img_2,
                true
        ));maListeDeDiscussions.add(new Discussion(
                "John_32",
                "C'est Mieux tu abandonne l'ecole",
                "5 Mar",
                R.drawable.img_2,
                true
        ));maListeDeDiscussions.add(new Discussion(
                "John_32",
                "C'est Mieux tu abandonne l'ecole, Tous ca c rien jdjff mais un jur . donc j'ai decider de sortir les poubelle",
                "5 Mar",
                R.drawable.img_2,
                true
        ));

        // D. On engage le Serveur (L'Adapter)
// On ajoute le "new DiscussionAdapter.OnItemClickListener() { ... }" à la fin
        DiscussionAdapter adapter = new DiscussionAdapter(maListeDeDiscussions, this, new DiscussionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Discussion discussion) {
                // --- C'EST ICI QUE TU DÉCIDES QUOI FAIRE ---

                // Exemple 1 : Afficher un petit message (Toast)
                 //Toast.makeText(DiscussionActivity.this, "Tu parles avec : " + discussion.getNom(), Toast.LENGTH_SHORT).show();

                // Exemple 2 : Ouvrir l'écran de Chat (Le plus probable pour ton projet)
                Intent intent = new Intent(DiscussionActivity.this, ChatActivity.class);

                // On passe des infos à l'autre page (ex: le nom de la personne)
                intent.putExtra("NOM_USER", discussion.getNom());
                intent.putExtra("IMG_USER", discussion.getPhotoResId());

                // On lance la nouvelle page
                startActivity(intent);
            }
        });
        // E. On dit au Serveur de servir les tables
        recyclerView.setAdapter(adapter);
    }
}