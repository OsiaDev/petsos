package com.osia.petsos.data.mapper

import com.osia.petsos.data.dto.*
import com.osia.petsos.domain.model.*

/* DTO → DOMAIN */
fun PetAdDTO.toDomain(): PetAd =
    PetAd(
        id = id,

        type = runCatching { AdvertisementType.valueOf(type) }
            .getOrDefault(AdvertisementType.LOST),

        status = runCatching { PetAdStatus.valueOf(status) }
            .getOrDefault(PetAdStatus.ACTIVE),

        category = runCatching { PetCategory.valueOf(category) }
            .getOrDefault(PetCategory.OTHER),

        breed = if (category == PetCategory.OTHER.name) null else breed,
        name = name,

        description = description,
        hasReward = hasReward,
        rewardAmount = rewardAmount,
        phones = phones,

        location = location.toDomain(),
        photoUrls = photoUrls,

        userId = userId,

        createdAt = createdAt,
        updatedAt = updatedAt,
        expiresAt = expiresAt
    )

/* DOMAIN → DTO */
fun PetAd.toDTO(): PetAdDTO =
    PetAdDTO(
        id = id,
        type = type.name,
        status = status.name,

        category = category.name,
        breed = if (category == PetCategory.OTHER) null else breed,
        name = name,

        description = description,
        hasReward = hasReward,
        rewardAmount = rewardAmount,
        phones = phones,

        location = location.toDTO(),
        photoUrls = photoUrls,

        userId = userId,

        createdAt = createdAt,
        updatedAt = updatedAt,
        expiresAt = expiresAt
    )

/* Location */
fun PetLocationDTO.toDomain(): PetLocation =
    PetLocation(lat, lng, address)

fun PetLocation.toDTO(): PetLocationDTO =
    PetLocationDTO(lat, lng, address)
