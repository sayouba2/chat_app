package com.example.chat_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.hdodenhof.circleimageview.CircleImageView;

public class navbarActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    // Variables pour le Header (pour pouvoir les modifier plus tard)
    protected CircleImageView headerImage;
    protected TextView headerPseudo;
    protected TextView headerNbrAmis;
    // Variable base de donnée :
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Rien de spécial ici car tout se passe dans setContentView
    }

    @Override
    public void setContentView(int layoutResID) {
        // 1. On charge le layout principal (celui qui contient le menu)
        // Note: Assure-toi que ton fichier XML s'appelle bien activity_main
        DrawerLayout fullView = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_navbar, null);

        // 2. On trouve le conteneur vide où le contenu des pages ira
        // Note: Assure-toi d'avoir un FrameLayout avec l'ID base_content dans activity_main.xml
        FrameLayout activityContainer = fullView.findViewById(R.id.base_content);

        // 3. On injecte le layout de l'enfant (layoutResID) dans ce conteneur
        getLayoutInflater().inflate(layoutResID, activityContainer, true);

        // 4. On définit le tout comme la vue de l'activité
        super.setContentView(fullView);

        // 5. Initialisation des vues
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // 6. Gestion des clics du menu
        setupNavigationDrawer();
        // 7. Modification des element Entete
        setupNavigationHeader();



    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // En Java moderne sur Android, on utilise if/else pour les IDs
                // car "switch" pose parfois problème avec les dernières versions de Gradle
                if (id == R.id.home_view) {
                    Intent intent =new Intent(navbarActivity.this, DiscussionActivity.class);
                    startActivity(intent);
                    Toast.makeText(navbarActivity.this, "Home", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.profil_view) {
                    Intent intent =new Intent(navbarActivity.this, UserInformations.class);
                    startActivity(intent);
                    Toast.makeText(navbarActivity.this, "Mes Infos", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.logout) {
                    // 1. Appel à Firebase pour déconnecter la session
                    FirebaseAuth.getInstance().signOut();

// 2. Redirection vers l'écran de connexion (MainActivity par exemple)
                    Intent intent = new Intent(navbarActivity.this, LoginActivity.class);

// 3. IMPORTANT : Vider la pile d'activités
// Cela empêche l'utilisateur de cliquer sur "Retour" et de revenir dans le chat sans être connecté.
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);
                    finish();

                    Toast.makeText(navbarActivity.this, "Déconnecter", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.share) {
                    Toast.makeText(navbarActivity.this, "Partager", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.evaluation) {
                    Toast.makeText(navbarActivity.this, "Évaluation", Toast.LENGTH_SHORT).show();
                }

                // Fermer le menu après le clic
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void setupNavigationHeader() {
        // On vérifie qu'il y a bien un header pour éviter un crash
        if (navigationView.getHeaderCount() > 0) {
            View header = navigationView.getHeaderView(0);
            headerImage = header.findViewById(R.id.imageHeader);
            headerPseudo = header.findViewById(R.id.pseudo);
            headerNbrAmis = header.findViewById(R.id.nbrAmis);

            // 1. Récupérer l'utilisateur actuel
            com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
            // Image
//            de.hdodenhof.circleimageview.CircleImageView image = header.findViewById(R.id.imageHeader);
//            image.setImageResource(R.drawable.img);
//
//            // Pseudo
//            TextView pseudo = header.findViewById(R.id.pseudo);
//            pseudo.setText("Pseudo_m_09");
//
//            // Amis
//            TextView nbrAmis = header.findViewById(R.id.nbrAmis);
//            nbrAmis.setText("230");
            headerImage.setImageResource(R.drawable.profile);
            if (user != null) {
                String uid = user.getUid();

                // 2. Récupérer le pseudo depuis Firestore (Collection "users")
                db.collection("users").document(uid).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String pseudoFirebase = documentSnapshot.getString("pseudo");
                                headerPseudo.setText(pseudoFirebase != null ? pseudoFirebase : "Utilisateur");

                                // Si tu as une URL d'image dans Firestore, tu peux charger l'image ici avec Glide ou Picasso
                                // String imageUrl = documentSnapshot.getString("image");
                            }
                        });

                // 3. Récupérer le nombre de discussions
                // Hypothèse : Tes discussions sont dans une collection "discussions"
                // où l'UID de l'utilisateur est présent dans une liste "participants"
                db.collection("chatRooms")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // task.getResult().size() renvoie le nombre total de documents dans la collection
                                int totalDocuments = task.getResult().size();
                                headerNbrAmis.setText(String.valueOf(totalDocuments));
                                Log.d("DEBUG_COUNT", "Total de documents dans chatRooms : " + totalDocuments);
                            } else {
                                Log.e("DEBUG_COUNT", "Erreur : ", task.getException());
                            }
                        });
            }
        }
    }

    // --- FONCTION 2 : MISE A JOUR (UPDATE) Les infos du hrader de la navbar---
    // Cette fonction est publique pour être appelée depuis d'autres endroits si besoin
    public void updateHeaderInfo(String pseudo, String nbAmis, int imageResId) {
        // On vérifie que les vues ont bien été initialisées pour éviter les crashs (NullPointerException)
        if (headerPseudo != null) {
            headerPseudo.setText(pseudo);
        }
        if (headerNbrAmis != null) {
            headerNbrAmis.setText(nbAmis);
        }
        if (headerImage != null && imageResId != 0) {
            headerImage.setImageResource(imageResId);
        }
    }
}