package com.osia.petsos.data.dto

import java.time.LocalDateTime

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

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime? = null
)
