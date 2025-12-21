package com.example.chat_app;
// Vérifie que c'est le bon nom de ton package

public class Discussion {
    private String nom;
    private String dernierMessage;
    private String heure;
    private String photoUrl; // C'était int avant, c'est String maintenant !
    private boolean isNonLu;
    private String uid; // Utile pour savoir à qui on parle

    // Constructeur vide (Requis par Firebase parfois)
    public Discussion() { }

    public Discussion(String nom, String dernierMessage, String heure, String photoUrl, boolean isNonLu, String uid) {
        this.nom = nom;
        this.dernierMessage = dernierMessage;
        this.heure = heure;
        this.photoUrl = photoUrl;
        this.isNonLu = isNonLu;
        this.uid = uid;
    }

    // Getters
    public String getNom() { return nom; }
    public String getDernierMessage() { return dernierMessage; }
    public String getHeure() { return heure; }
    public String getPhotoUrl() { return photoUrl; }
    public boolean isNonLu() { return isNonLu; }
    public String getUid() { return uid; }
}