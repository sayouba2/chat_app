package com.example.chat_app;

public class Discussion {
    private String status;
    private String nom;
    private String dernierMessage;
    private String heure;
    private String photoUrl;
    private boolean isNonLu;
    private String uid;
    private String type; // Le champ qui manquait dans le constructeur

    // Constructeur vide (Requis par Firebase)
    public Discussion() { }

    // --- C'EST ICI LA CORRECTION ---
    // On ajoute "String type" à la fin des parenthèses
    public Discussion(String nom, String dernierMessage, String heure, String photoUrl, boolean isNonLu, String uid, String type) {
        this.nom = nom;
        this.dernierMessage = dernierMessage;
        this.heure = heure;
        this.photoUrl = photoUrl;
        this.isNonLu = isNonLu;
        this.uid = uid;
        this.type = type; // On assigne la valeur reçue
    }

    // Getters et Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNom() { return nom; }
    public String getDernierMessage() { return dernierMessage; }
    public String getHeure() { return heure; }
    public String getPhotoUrl() { return photoUrl; }
    public boolean isNonLu() { return isNonLu; }
    public String getUid() { return uid; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
