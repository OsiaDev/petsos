package com.osia.petsos.data.repository

import com.google.firebase.firestore.FirebaseFirestore
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
import java.util.UUID
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: com.google.firebase.storage.FirebaseStorage
) : PetRepository {

    override fun getPets(): Flow<Resource<List<PetAd>>> = callbackFlow {
        trySend(Resource.Loading())

        // Usar la constante de colección centralizada
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

    override suspend fun savePet(pet: PetAd, images: List<android.net.Uri>): Resource<Boolean> = try {
        val petId = if (pet.id.isEmpty()) java.util.UUID.randomUUID().toString() else pet.id
        
        val uploadedUrls = images.map { uri ->
            val imageId = java.util.UUID.randomUUID().toString()
            val filename = "${imageId}_original.jpg"
            val path = "pets/$petId/original/$filename"
            val ref = storage.reference.child(path)
            
            // Upload
            ref.putFile(uri).await()
            // Get URL
            ref.downloadUrl.await().toString()
        }

        val petWithPhotos = pet.copy(
            id = petId,
            photoUrls = uploadedUrls,
            updatedAt = java.time.LocalDateTime.now(),
            createdAt = if (pet.createdAt == null) java.time.LocalDateTime.now() else pet.createdAt
        )
        
        firestore.collection(FirebaseConfig.PETS_COLLECTION)
            .document(petId)
            .set(petWithPhotos.toDTO())
            .await()

        Resource.Success(true)
    } catch (e: Exception) {
        e.printStackTrace()
        Resource.Error(e.localizedMessage ?: "Failed to save pet report")
    }

}