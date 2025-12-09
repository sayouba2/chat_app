package com.example.chat_app;
// Vérifie que c'est le bon nom de ton package

public class Discussion {

    // --- 1. LES INGRÉDIENTS (Attributs privés) ---
    private String nom;
    private String dernierMessage;
    private String heure;
    private int photoResId; // L'ID de l'image (ex: R.drawable.avatar)
    private boolean estNonLu; // Vrai si le message n'est pas lu

    // --- 2. LES ARCHITECTES (Constructeurs) ---

    // Architecte A : Le Perfectionniste (Tu lui donnes TOUTES les infos)
    public Discussion(String nom, String dernierMessage, String heure, int photoResId, boolean estNonLu) {
        this.nom = nom;
        this.dernierMessage = dernierMessage;
        this.heure = heure;
        this.photoResId = photoResId;
        this.estNonLu = estNonLu;
    }

    // Architecte B : Le "Cool" (C'est lui qui CORRIGE ton erreur !)
    // Si tu ne précises pas "estNonLu", il décide tout seul que c'est faux (message lu).
    public Discussion(String nom, String dernierMessage, String heure, int photoResId) {
        this.nom = nom;
        this.dernierMessage = dernierMessage;
        this.heure = heure;
        this.photoResId = photoResId;
        this.estNonLu = false; // Valeur par défaut
    }

    // --- 3. LES ACCÈS (Getters) ---
    public String getNom() {
        return nom;
    }

    public String getDernierMessage() {
        return dernierMessage;
    }

    public String getHeure() {
        return heure;
    }

    public int getPhotoResId() {
        return photoResId;
    }

    public boolean isEstNonLu() {
        return estNonLu;
    }

    // --- 4. LES MODIFICATIONS (Setters) ---
    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setDernierMessage(String dernierMessage) {
        this.dernierMessage = dernierMessage;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public void setPhotoResId(int photoResId) {
        this.photoResId = photoResId;
    }

    public void setEstNonLu(boolean estNonLu) {
        this.estNonLu = estNonLu;
    }
}