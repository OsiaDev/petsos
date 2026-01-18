package com.osia.petsos.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.osia.petsos.data.dto.PetAdDTO
import com.osia.petsos.data.mapper.toDomain
import com.osia.petsos.domain.model.PetAd
import com.osia.petsos.domain.repository.PetRepository
import com.osia.petsos.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PetRepository {

    override fun getPets(): Flow<Resource<List<PetAd>>> = callbackFlow {
        trySend(Resource.Loading())

        val subscription = firestore.collection("pets")
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

}
