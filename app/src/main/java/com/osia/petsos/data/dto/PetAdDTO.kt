package com.osia.petsos.data.dto

import com.google.firebase.Timestamp

data class PetAdDTO(
    val id: String = "",

    val type: String = "",       // LOST | FOUND
    val status: String = "",     // ACTIVE | RESOLVED | EXPIRED | WITHDRAWN

    val category: String = "",   // DOG | CAT | OTHER
    val breed: String? = null,
    val name: String? = null,

    val description: String = "",

    val hasReward: Boolean = false,
    val rewardAmount: Int? = null,

    val phones: List<String> = emptyList(),

    val location: PetLocationDTO = PetLocationDTO(),
    val photoUrls: List<String> = emptyList(),

    val userId: String = "",

    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val expiresAt: Timestamp? = null
)
