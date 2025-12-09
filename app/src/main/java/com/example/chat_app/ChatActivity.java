package com.example.chat_app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChatActivity extends navbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView userName = findViewById(R.id.pseudo);
        ImageView userProfile = findViewById(R.id.image_profile);
        String name = getIntent().getStringExtra("NOM_USER");
        int resImg = getIntent().getIntExtra("IMG_USER", R.drawable.img);
        userName.setText(name);
        userProfile.setImageResource(resImg);
        // 1. On récupère le bouton (les 3 points) via son ID défini dans le XML layout
        ImageView btnMenu = findViewById(R.id.chat_menu);

// 2. On écoute le clic sur l'image
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. On crée le PopupMenu. 'MainActivity.this' est le contexte, 'v' est l'ancre (le bouton)
                PopupMenu popup = new PopupMenu(ChatActivity.this, v);

                // 4. On lie le fichier XML de menu (menu_options.xml)
                popup.getMenuInflater().inflate(R.menu.chat_menu, popup.getMenu());

                // 5. On gère les clics sur les options (Modifier, Supprimer...)
                /*popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // On vérifie quel item a été cliqué via son ID
                        int id = item.getItemId();

                        if (id == R.id.action_edit) {
                            Toast.makeText(MainActivity.this, "Action: Modifier", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (id == R.id.action_delete) {
                            Toast.makeText(MainActivity.this, "Action: Supprimer", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });*/

                // 6. IMPORTANT : Ne pas oublier d'afficher le menu !
                popup.show();
            }
        });

    }
}