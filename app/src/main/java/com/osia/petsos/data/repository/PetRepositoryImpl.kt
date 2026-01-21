package com.osia.petsos.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.storageMetadata
import com.osia.petsos.core.config.FirebaseConfig
import com.osia.petsos.data.dto.PetAdDTO
import com.osia.petsos.data.mapper.toDomain
import com.osia.petsos.domain.model.PetAd
import com.osia.petsos.domain.repository.PetRepository
import com.osia.petsos.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.osia.petsos.data.mapper.toDTO
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.osia.petsos.domain.model.PetAdStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

class PetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : PetRepository {

    companion object {
        private const val TAG = "PetRepository"
    }

    override fun getPets(): Flow<Resource<List<PetAd>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection(FirebaseConfig.PETS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    launch(Dispatchers.IO) {
                        try {
                            val pets = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(PetAdDTO::class.java)?.copy(id = doc.id)
                            }

                            val petsWithImages = pets.map { petDto ->
                                async {
                                    try {
                                        val imagesSnapshot = firestore.collection(FirebaseConfig.PETS_COLLECTION)
                                            .document(petDto.id)
                                            .collection("images")
                                            .limit(1)
                                            .get()
                                            .await()

                                        val thumbPath = imagesSnapshot.documents.firstOrNull()?.getString("thumbPath")
                                        if (!thumbPath.isNullOrBlank()) {
                                            petDto.copy(images = listOf(thumbPath))
                                        } else {
                                            petDto
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error fetching image for pet ${petDto.id}", e)
                                        petDto
                                    }
                                }
                            }.awaitAll()

                            withContext(Dispatchers.Main) {
                                trySend(Resource.Success(petsWithImages.map { it.toDomain() }))
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing pets data", e)
                            trySend(Resource.Error(e.localizedMessage ?: "Error processing data"))
                        }
                    }
                }
            }

        awaitClose { subscription.remove() }
    }

    override fun getUserPets(userId: String): Flow<Resource<List<PetAd>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection(FirebaseConfig.PETS_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    launch(Dispatchers.IO) {
                        try {
                            val pets = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(PetAdDTO::class.java)?.copy(id = doc.id)
                            }

                            val petsWithImages = pets.map { petDto ->
                                async {
                                    try {
                                        val imagesSnapshot = firestore.collection(FirebaseConfig.PETS_COLLECTION)
                                            .document(petDto.id)
                                            .collection("images")
                                            .limit(1)
                                            .get()
                                            .await()

                                        val thumbPath = imagesSnapshot.documents.firstOrNull()?.getString("thumbPath")
                                        if (!thumbPath.isNullOrBlank()) {
                                            petDto.copy(images = listOf(thumbPath))
                                        } else {
                                            petDto
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error fetching image for pet ${petDto.id}", e)
                                        petDto
                                    }
                                }
                            }.awaitAll()

                            withContext(Dispatchers.Main) {
                                trySend(Resource.Success(petsWithImages.map { it.toDomain() }))
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing user pets data", e)
                            trySend(Resource.Error(e.localizedMessage ?: "Error processing data"))
                        }
                    }
                }
            }

        awaitClose { subscription.remove() }
    }

    override fun getPet(petId: String): Flow<Resource<PetAd>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection(FirebaseConfig.PETS_COLLECTION)
            .document(petId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    launch(Dispatchers.IO) {
                        try {
                            val petDto = snapshot.toObject(PetAdDTO::class.java)?.copy(id = snapshot.id)

                            if (petDto != null) {
                                try {
                                    val imagesSnapshot = firestore.collection(FirebaseConfig.PETS_COLLECTION)
                                        .document(petDto.id)
                                        .collection("images")
                                        .get()
                                        .await()

                                    val images = imagesSnapshot.documents.mapNotNull {
                                        it.getString("path") ?: it.getString("thumbPath")
                                    }

                                    // If no images found in subcollection, check if there are images in the root document (legacy/alternative)
                                    val finalImages = if (images.isNotEmpty()) images else petDto.images

                                    withContext(Dispatchers.Main) {
                                        trySend(Resource.Success(petDto.copy(images = finalImages).toDomain()))
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error fetching images for pet ${petDto.id}", e)
                                    withContext(Dispatchers.Main) {
                                        trySend(Resource.Success(petDto.toDomain()))
                                    }
                                }
                            } else {
                                trySend(Resource.Error("Pet data is invalid"))
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing pet detail data", e)
                            trySend(Resource.Error(e.localizedMessage ?: "Error processing data"))
                        }
                    }
                } else {
                    trySend(Resource.Error("Pet not found"))
                }
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun savePet(pet: PetAd, images: List<Uri>): Resource<Boolean> {
        return try {
            // Verificar autenticación
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "❌ User not authenticated")
                return Resource.Error("Usuario no autenticado")
            }

            Log.d(TAG, "✅ Starting savePet for user: ${currentUser.uid}")
            Log.d(TAG, "📸 Number of images to upload: ${images.size}")

            val petRef = firestore.collection(FirebaseConfig.PETS_COLLECTION).document()
            val petId = petRef.id

            Log.d(TAG, "📄 Created pet document with ID: $petId")

            // Convertir a DTO
            val petDTO = pet.copy(
                id = petId,
                userId = currentUser.uid, // Asegurar que sea el UID correcto
                status = PetAdStatus.PROCESSING,
                images = emptyList(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ).toDTO()

            // Crear un mapa mutable para poder modificar los timestamps
            val petData = mutableMapOf<String, Any?>(
                "id" to petDTO.id,
                "type" to petDTO.type,
                "status" to petDTO.status,
                "category" to petDTO.category,
                "breed" to petDTO.breed,
                "name" to petDTO.name,
                "description" to petDTO.description,
                "hasReward" to petDTO.hasReward,
                "rewardAmount" to petDTO.rewardAmount,
                "phones" to petDTO.phones,
                "location" to petDTO.location,
                "images" to petDTO.images,
                "userId" to petDTO.userId,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "expiresAt" to petDTO.expiresAt
            )

            // Guardar en Firestore
            petRef.set(petData).await()
            Log.d(TAG, "✅ Pet saved to Firestore successfully")

            val metadata = storageMetadata {
                contentType = "image/jpeg"
            }

            // Subir imágenes
            Log.d(TAG, "🔄 Starting image upload process...")
            images.forEachIndexed { index, uri ->
                try {
                    val imageId = UUID.randomUUID().toString()
                    val filename = "${imageId}_original.jpg"
                    val path = "pets/$petId/original/$filename"

                    Log.d(TAG, "📤 Uploading image ${index + 1}/${images.size}")
                    Log.d(TAG, "   Path: $path")
                    Log.d(TAG, "   URI: $uri")

                    val ref = storage.reference.child(path)

                    val jpegBytes = withContext(Dispatchers.IO) {
                        compressImageToJpeg(context, uri)
                    }

                    if (jpegBytes != null) {
                        val sizeInMB = jpegBytes.size / (1024.0 * 1024.0)
                        Log.d(TAG, "   ✅ Image compressed: ${String.format("%.2f", sizeInMB)} MB")

                        // Verificar el tamaño (10MB = 10,485,760 bytes)
                        if (jpegBytes.size > 10 * 1024 * 1024) {
                            throw Exception("Image too large: ${String.format("%.2f", sizeInMB)} MB")
                        }

                        Log.d(TAG, "   🚀 Uploading to Firebase Storage...")
                        val uploadTask = ref.putBytes(jpegBytes, metadata)
                        uploadTask.await()

                        Log.d(TAG, "   ✅ Image ${index + 1} uploaded successfully!")
                    } else {
                        throw Exception("Failed to compress image: $uri")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error uploading image ${index + 1}", e)
                    Log.e(TAG, "   Error type: ${e.javaClass.simpleName}")
                    Log.e(TAG, "   Error message: ${e.message}")
                    throw e
                }
            }

            Log.d(TAG, "🎉 All images uploaded successfully!")
            Resource.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ FATAL ERROR in savePet", e)
            Log.e(TAG, "   Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Error message: ${e.message}")
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Failed to save pet report")
        }
    }

    private fun compressImageToJpeg(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI: $uri")
                return null
            }

            // Redimensionar si es muy grande (max 1920px en cualquier dimensión)
            val maxDimension = 1920
            val bitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                val ratio = minOf(
                    maxDimension.toFloat() / originalBitmap.width,
                    maxDimension.toFloat() / originalBitmap.height
                )
                val newWidth = (originalBitmap.width * ratio).toInt()
                val newHeight = (originalBitmap.height * ratio).toInt()

                Log.d(TAG, "Resizing image from ${originalBitmap.width}x${originalBitmap.height} to ${newWidth}x${newHeight}")

                originalBitmap.scale(newWidth, newHeight).also {
                    if (it != originalBitmap) originalBitmap.recycle()
                }
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()
            outputStream.close()

            if (bitmap != originalBitmap) bitmap.recycle()

            Log.d(TAG, "Image compressed to ${bytes.size} bytes")
            bytes
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            e.printStackTrace()
            null
        }
    }

}