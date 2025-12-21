package com.example.chat_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; // INDISPENSABLE pour le design XML

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    // Vues
    private CircleImageView profileImageView;
    private TextInputLayout nomLayout, pseudoLayout, emailLayout, passwordLayout;
    private TextInputEditText nomEditText, pseudoEditText, emailEditText, passwordEditText;
    private Button registerButton;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    // Variables pour l'image
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        initViews();

        // Clic sur l'image pour ouvrir la galerie
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        // Clic sur le bouton d'inscription
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    createAccount();
                }
            }
        });
    }

    private void initViews() {
        profileImageView = findViewById(R.id.image_register);

        // Layouts (pour gérer les erreurs visuelles)
        nomLayout = findViewById(R.id.nom_layout);
        pseudoLayout = findViewById(R.id.pseudo_layout);
        emailLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);

        // EditTexts (pour récupérer le texte)
        nomEditText = findViewById(R.id.nometprenom_edit_text);
        pseudoEditText = findViewById(R.id.pseudo_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);

        registerButton = findViewById(R.id.login_button);
    }

    // Ouvre la galerie
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Récupère l'image choisie
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri); // Affiche l'image choisie
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(nomEditText.getText())) {
            nomLayout.setError("Le nom est requis");
            isValid = false;
        } else { nomLayout.setError(null); }

        if (TextUtils.isEmpty(pseudoEditText.getText())) {
            pseudoLayout.setError("Le pseudo est requis");
            isValid = false;
        } else { pseudoLayout.setError(null); }

        if (TextUtils.isEmpty(emailEditText.getText())) {
            emailLayout.setError("L'email est requis");
            isValid = false;
        } else { emailLayout.setError(null); }

        if (TextUtils.isEmpty(passwordEditText.getText())) {
            passwordLayout.setError("Le mot de passe est requis");
            isValid = false;
        } else { passwordLayout.setError(null); }

        return isValid;
    }

    private void createAccount() {
        // Afficher un état de chargement
        registerButton.setEnabled(false);
        registerButton.setText("Création en cours...");

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // 1. Créer l'utilisateur dans Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Succès Auth -> On passe à l'image
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();

                            if (imageUri != null) {
                                uploadImageToStorage(uid);
                            } else {
                                saveUserToFirestore(uid, "default");
                            }
                        } else {
                            // Erreur Auth
                            registerButton.setEnabled(true);
                            registerButton.setText("Créer mon compte");
                            Toast.makeText(RegisterActivity.this, "Erreur: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void uploadImageToStorage(String uid) {
        // Utilisation d'un nom unique avec timestamp pour éviter les conflits
        final StorageReference fileRef = storageReference.child("profile_images/" + uid + ".jpg");

        // 2. Upload de l'image
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // 3. Récupérer l'URL de téléchargement
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        // 4. Sauvegarder dans Firestore
                        saveUserToFirestore(uid, downloadUrl);
                    }
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(RegisterActivity.this, "Erreur Upload Image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Si l'image échoue, on sauvegarde quand même avec l'image par défaut
            saveUserToFirestore(uid, "default");
        });
    }

    private void saveUserToFirestore(String uid, String profileImageUrl) {
        String nom = nomEditText.getText().toString().trim();
        String pseudo = pseudoEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("name", nom);
        userMap.put("pseudo", pseudo);
        userMap.put("email", email);
        userMap.put("image", profileImageUrl);

        // 5. Écriture dans la base de données
        db.collection("users").document(uid).set(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show();
                            // On redirige vers DiscussionActivity au lieu de MainActivity
                            Intent intent = new Intent(RegisterActivity.this, DiscussionActivity.class);

                            // Ces flags empêchent l'utilisateur de revenir sur l'écran d'inscription en faisant "Retour"
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            registerButton.setEnabled(true);
                            registerButton.setText("Créer mon compte");
                            Toast.makeText(RegisterActivity.this, "Erreur Firestore: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}