package com.osia.petsos.domain.model

import java.time.LocalDateTime

data class PetAd(
    val id: String = "",
    val geohash: String? = null,


    // Qué pasó con la mascota
    val type: AdvertisementType = AdvertisementType.LOST,

    // Estado del aviso
    val status: PetAdStatus = PetAdStatus.ACTIVE,

    // Qué animal es
    val category: PetCategory = PetCategory.DOG,
    val breed: String? = null,          // null si OTHER
    val name: String? = null,           // solo LOST normalmente

    val description: String = "",

    val hasReward: Boolean = false,
    val rewardAmount: Int? = null,

    val phones: List<String> = emptyList(),

    val location: PetLocation = PetLocation(),
    val images: List<String> = emptyList(),

    val userId: String = "",

    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val expiresAt: LocalDateTime? = null
)
