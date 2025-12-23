package com.example.chat_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class navbarActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;

    // Variables pour le Header
    private CircleImageView headerImage;
    private TextView headerPseudo;
    private TextView headerNbrAmis;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Rien de spécial ici car tout se passe dans setContentView
    }

    @Override
    public void setContentView(int layoutResID) {
        // 1. Structure de base du menu
        DrawerLayout fullView = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_navbar, null);
        FrameLayout activityContainer = fullView.findViewById(R.id.base_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(fullView);

        // 2. Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 3. Initialisation des vues
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // 4. Configuration
        setupNavigationDrawer();
        setupNavigationHeader(); // Récupère les vues du header
        loadUserData();          // Charge les vraies données depuis Firestore
    }

    private void setupNavigationHeader() {
        if (navigationView.getHeaderCount() > 0) {
            View header = navigationView.getHeaderView(0);
            headerImage = header.findViewById(R.id.imageHeader);
            headerPseudo = header.findViewById(R.id.pseudo);
            headerNbrAmis = header.findViewById(R.id.nbrAmis);
        }
    }

    // --- CHARGEMENT DES DONNÉES UTILISATEUR ---
    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // 1. Récupérer Pseudo et Image
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String pseudo = documentSnapshot.getString("pseudo");
                            String image = documentSnapshot.getString("image");

                            if (headerPseudo != null) headerPseudo.setText(pseudo);

                            // Gestion Image (URL ou Drawable local)
                            if (headerImage != null && image != null) {
                                if (image.startsWith("http")) {
                                    Glide.with(this).load(image).into(headerImage);
                                } else {
                                    int resId = getResources().getIdentifier(image, "drawable", getPackageName());
                                    if (resId != 0) headerImage.setImageResource(resId);
                                    else headerImage.setImageResource(R.drawable.img);
                                }
                            }
                        }
                    });

            // 2. Récupérer le nombre d'amis (Compter les documents dans la sous-collection)
            db.collection("users").document(uid).collection("Friends").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int count = queryDocumentSnapshots.size();
                        if (headerNbrAmis != null) headerNbrAmis.setText(String.valueOf(count));
                    });
        }
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.home_view) {
                    // --- HOME : Retour à la liste des discussions ---
                    if (!(navbarActivity.this instanceof DiscussionActivity)) {
                        Intent intent = new Intent(navbarActivity.this, DiscussionActivity.class);
                        // flags pour éviter d'empiler les activités
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                } else if (id == R.id.profil_view) {
                    // --- MES INFOS ---
                    // Créez une ProfileActivity si vous en avez une, sinon Toast pour l'instant
                    Toast.makeText(navbarActivity.this, "Page Profil bientôt disponible", Toast.LENGTH_SHORT).show();
                    // Intent intent = new Intent(navbarActivity.this, ProfileActivity.class);
                    // startActivity(intent);

                } else if (id == R.id.logout) {
                    // --- DÉCONNEXION ---
                    updateUserStatus("offline"); // Mettre hors ligne avant de partir
                    mAuth.signOut();
                    Intent intent = new Intent(navbarActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else if (id == R.id.share) {
                    // --- PARTAGER L'APPLICATION ---
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    String shareBody = "Télécharge PingMe, la meilleure app de chat ! Lien : http://play.google.com/store/apps/details?id=" + getPackageName();
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Rejoins-moi sur PingMe");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(shareIntent, "Partager via"));

                } else if (id == R.id.evaluation) {
                    // --- ÉVALUER NOUS (EMAIL) ---
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:")); // Seulement les applis mail
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"infopingme@gmail.com"});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Evaluation PingMe");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Bonjour l'équipe PingMe,\n\nVoici mon avis sur l'application : \n");

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Envoyer un email..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(navbarActivity.this, "Aucune application d'email installée.", Toast.LENGTH_SHORT).show();
                    }
                }

                // Fermer le menu après le clic
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    // Gestion du statut en ligne (Realtime Database)
    private void updateUserStatus(String status) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UsersStatus").child(currentUser.getUid());
            HashMap<String, Object> onlineState = new HashMap<>();
            onlineState.put("status", status);
            onlineState.put("lastSeen", System.currentTimeMillis());
            userRef.updateChildren(onlineState);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
        loadUserData(); // Recharger les données (si on a changé de photo entre temps)
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus("offline");
    }
}
