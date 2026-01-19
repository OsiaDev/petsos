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
import com.google.firebase.storage.FirebaseStorage
import com.osia.petsos.domain.model.PetAdStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class PetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : PetRepository {

    override fun getPets(): Flow<Resource<List<PetAd>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection(FirebaseConfig.PETS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Unknown error"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val pets = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PetAdDTO::class.java)?.copy(id = doc.id)?.toDomain()
                    }
                    trySend(Resource.Success(pets))
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
                    val pets = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PetAdDTO::class.java)?.copy(id = doc.id)?.toDomain()
                    }
                    trySend(Resource.Success(pets))
                }
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun savePet(pet: PetAd, images: List<Uri>): Resource<Boolean> = try {
        val petRef = firestore.collection(FirebaseConfig.PETS_COLLECTION).document()
        val petId = petRef.id

        // Convertir a DTO
        val petDTO = pet.copy(
            id = petId,
            status = PetAdStatus.PROCESSING,
            photoUrls = emptyList(),
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
            "photoUrls" to petDTO.photoUrls,
            "userId" to petDTO.userId,
            // Usar FieldValue.serverTimestamp() para timestamps
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "expiresAt" to petDTO.expiresAt
        )

        // Guardar en Firestore
        petRef.set(petData).await()

        val metadata = storageMetadata {
            contentType = "image/jpeg"
        }

        // Subir imágenes
        images.forEach { uri ->
            val imageId = UUID.randomUUID().toString()
            val filename = "${imageId}_original.jpg"
            val path = "pets/$petId/original/$filename"
            val ref = storage.reference.child(path)

            val jpegBytes = withContext(Dispatchers.IO) {
                compressImageToJpeg(context, uri)
            }

            if (jpegBytes != null) {
                ref.putBytes(jpegBytes, metadata).await()
            } else {
                throw Exception("Failed to process image: $uri")
            }
        }

        Resource.Success(true)
    } catch (e: Exception) {
        e.printStackTrace()
        Resource.Error(e.localizedMessage ?: "Failed to save pet report")
    }

    private fun compressImageToJpeg(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()
            outputStream.close()
            bytes
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}