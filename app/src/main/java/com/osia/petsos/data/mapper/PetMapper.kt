package com.osia.petsos.data.mapper

import com.google.firebase.Timestamp
import com.osia.petsos.data.dto.*
import com.osia.petsos.domain.model.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

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

        createdAt = createdAt?.toLocalDateTime(),
        updatedAt = updatedAt?.toLocalDateTime(),
        expiresAt = expiresAt?.toLocalDateTime()
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

        createdAt = createdAt?.toTimestamp(),
        updatedAt = updatedAt?.toTimestamp(),
        expiresAt = expiresAt?.toTimestamp()
    )

/* Location */
fun PetLocationDTO.toDomain(): PetLocation =
    PetLocation(lat, lng, address)

fun PetLocation.toDTO(): PetLocationDTO =
    PetLocationDTO(lat, lng, address)

/* Helpers */
private fun Timestamp.toLocalDateTime(): LocalDateTime {
    return this.toDate().toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

private fun LocalDateTime.toTimestamp(): Timestamp {
    val date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
    return Timestamp(date)
}
