package com.osia.petsos.domain.repository

import android.net.Uri
import com.osia.petsos.domain.model.PetAd
import kotlinx.coroutines.flow.Flow
import com.osia.petsos.utils.Resource

interface PetRepository {

    fun getPets(): Flow<Resource<List<PetAd>>>

    fun getUserPets(userId: String): Flow<Resource<List<PetAd>>>

    fun getPet(petId: String): Flow<Resource<PetAd>>

    fun getNearbyPets(lat: Double, lng: Double, radiusKm: Double): Flow<Resource<List<PetAd>>>


    suspend fun savePet(pet: PetAd, images: List<Uri>): Resource<Boolean>

}
