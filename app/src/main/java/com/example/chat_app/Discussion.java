package com.example.chat_app;

public class Discussion {
    private String nom;
    private String dernierMessage;
    private String heure;
    private String photoUrl;
    private boolean isNonLu;
    private String uid;
    private String type;

    // Constructeur vide (Requis par Firebase)
    public Discussion() { }

    // Constructeur à 6 paramètres (sans type)
    public Discussion(String nom, String dernierMessage, String heure, String photoUrl, boolean isNonLu, String uid) {
        this.nom = nom;
        this.dernierMessage = dernierMessage;
        this.heure = heure;
        this.photoUrl = photoUrl;
        this.isNonLu = isNonLu;
        this.uid = uid;
        this.type = null;
    }

    // Constructeur à 7 paramètres (avec type, ex: "group")
    public Discussion(String nom, String dernierMessage, String heure, String photoUrl, boolean isNonLu, String uid, String type) {
        this.nom = nom;
        this.dernierMessage = dernierMessage;
        this.heure = heure;
        this.photoUrl = photoUrl;
        this.isNonLu = isNonLu;
        this.uid = uid;
        this.type = type;
    }

    // --- Getters ---
    public String getNom() { return nom; }
    public String getDernierMessage() { return dernierMessage; }
    public String getHeure() { return heure; }
    public String getPhotoUrl() { return photoUrl; }
    public boolean isNonLu() { return isNonLu; }
    public String getUid() { return uid; }
    public String getType() { return type; }
}
