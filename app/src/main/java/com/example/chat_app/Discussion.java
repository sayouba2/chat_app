package com.example.chat_app;

public class Discussion {
    private String nom;
    private String dernierMessage;private String heure;
    private String photoUrl;
    private boolean isNonLu;
    private String uid;

    // Constructeur vide (Requis par Firebase)
    public Discussion() { }

    // --- CONSTRUCTEUR FINAL À 6 PARAMÈTRES ---
    public Discussion(String nom, String dernierMessage, String heure, String photoUrl, boolean isNonLu, String uid) {
        this.nom = nom;
        this.dernierMessage = dernierMessage;
        this.heure = heure;
        this.photoUrl = photoUrl;
        this.isNonLu = isNonLu;
        this.uid = uid;
    }

    // --- Getters ---
    public String getNom() { return nom; }
    public String getDernierMessage() { return dernierMessage; }
    public String getHeure() { return heure; }
    public String getPhotoUrl() { return photoUrl; }
    public boolean isNonLu() { return isNonLu; }
    public String getUid() { return uid; }
}
