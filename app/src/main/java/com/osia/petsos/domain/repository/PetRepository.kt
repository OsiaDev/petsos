package com.osia.petsos.domain.repository

import com.osia.petsos.domain.model.PetAd
import kotlinx.coroutines.flow.Flow
import com.osia.petsos.utils.Resource

interface PetRepository {

    fun getPets(): Flow<Resource<List<PetAd>>>

    fun getUserPets(userId: String): Flow<Resource<List<PetAd>>>

    suspend fun savePet(pet: PetAd, images: List<android.net.Uri>): Resource<Boolean>

}
