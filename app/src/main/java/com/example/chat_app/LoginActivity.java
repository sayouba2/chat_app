package com.example.chat_app;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerLink;

    // 1. Déclarer l'instance Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 2. Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        initViews();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    performLogin();
                }
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // Vérifier si l'utilisateur est déjà connecté au démarrage (Auto-login)
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // L'utilisateur est déjà connecté, on va direct au Chat
            goToChatActivity();
        }
    }

    private void initViews() {
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);
    }

    private boolean validateInputs() {
        // ... (Même code de validation que précédemment) ...
        // Pour gagner de la place ici, je garde la logique :
        // Si vide -> layout.setError("Requis"), return false.
        // Sinon -> layout.setError(null), return true.

        boolean isValid = true;
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("L'e-mail est requis");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Le mot de passe est requis");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Afficher un état de chargement (désactiver bouton)
        loginButton.setEnabled(false);
        loginButton.setText("Connexion...");

        // 3. Connexion Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loginButton.setEnabled(true);
                        loginButton.setText(R.string.button_login); // Remettre le texte original

                        if (task.isSuccessful()) {
                            // Succès
                            Toast.makeText(LoginActivity.this, "Connexion réussie !", Toast.LENGTH_SHORT).show();
                            goToChatActivity();
                        } else {
                            // Échec
                            String errorCode = "";
                            if (task.getException() != null) {
                                errorCode = task.getException().getMessage();
                            }

                            // Afficher l'erreur sur le champ mot de passe ou email
                            passwordLayout.setError("Échec : " + errorCode);
                            // Ou simplement : "Email ou mot de passe incorrect"
                        }
                    }
                });
    }

    private void goToChatActivity() {
        // Remplacez MainActivity.class par votre activité de liste de discussion
        Intent intent = new Intent(LoginActivity.this, DiscussionActivity.class);
        startActivity(intent);
        finish(); // Empêche de revenir au login en faisant "Retour"
    }
}