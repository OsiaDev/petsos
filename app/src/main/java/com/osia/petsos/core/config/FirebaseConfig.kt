package com.osia.petsos.core.config

import android.net.Uri

/**
 * Configuración centralizada para Firebase
 * Contiene todas las constantes y URLs relacionadas con Firebase
 */
object FirebaseConfig {

    /**
     * Web Client ID para Google Sign-In
     * Obtenido de google-services.json (client_type: 3)
     */
    const val GOOGLE_WEB_CLIENT_ID = "608658580372-o1n51bgs1t4noknuivsbgdf29dej0uaj.apps.googleusercontent.com"

    /**
     * Bucket de Firebase Storage
     */
    private const val STORAGE_BUCKET = "petsos-project-app.firebasestorage.app"

    /**
     * URL base de Firebase Storage
     */
    private const val STORAGE_BASE_URL = "https://firebasestorage.googleapis.com/v0/b/$STORAGE_BUCKET/o/"

    /**
     * Nombre de la colección de mascotas en Firestore
     */
    const val PETS_COLLECTION = "pets"

    /**
     * Convierte un path relativo de Storage a una URL completa
     *
     * @param path Ruta relativa en Storage (ej: "pets/123/thumb/abc.webp")
     * @return URL completa para acceder al archivo
     */
    fun getStorageUrl(path: String): String {
        return if (path.startsWith("http")) {
            // Ya es una URL completa
            path
        } else {
            // Construir URL desde path relativo
            "$STORAGE_BASE_URL${Uri.encode(path)}?alt=media"
        }
    }

}