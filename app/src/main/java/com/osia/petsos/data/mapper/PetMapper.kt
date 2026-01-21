package com.osia.petsos.data.mapper

import com.google.firebase.Timestamp
import com.osia.petsos.data.dto.*
import com.osia.petsos.domain.model.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/* DTO → DOMAIN */
fun PetAdDTO.toDomain(): PetAd {
    return PetAd(
        id = id,
        geohash = geohash,
        type = AdvertisementType.valueOf(type),
        status = PetAdStatus.valueOf(status),
        category = PetCategory.valueOf(category),
        breed = breed,
        name = name,
        description = description,
        hasReward = hasReward,
        rewardAmount = rewardAmount,
        phones = phones,
        location = location.toDomain(),
        images = images,
        userId = userId,
        createdAt = createdAt?.toLocalDateTime(),
        updatedAt = updatedAt?.toLocalDateTime(),
        expiresAt = expiresAt?.toLocalDateTime()
    )
}

/* DOMAIN → DTO */
fun PetAd.toDTO(): PetAdDTO {
    return PetAdDTO(
        id = id,
        geohash = geohash,
        type = type.name,
        status = status.name,
        category = category.name,
        breed = breed,
        name = name,
        description = description,
        hasReward = hasReward,
        rewardAmount = rewardAmount,
        phones = phones,
        location = location.toDTO(),
        images = images,
        userId = userId,
        createdAt = createdAt?.toTimestamp(),
        updatedAt = updatedAt?.toTimestamp(),
        expiresAt = expiresAt?.toTimestamp()
    )
}

/* Location */
fun PetLocationDTO.toDomain(): PetLocation =
    PetLocation(lat, lng, address)

fun PetLocation.toDTO(): PetLocationDTO =
    PetLocationDTO(lat, lng, address)

/* Helpers */
private fun Any.toLocalDateTime(): LocalDateTime? {
    return when (this) {
        is Timestamp -> this.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        is Map<*, *> -> {
            try {
                val seconds = (this["seconds"] as? Number)?.toLong() ?: 0L
                val nanoseconds = (this["nanoseconds"] as? Number)?.toInt() ?: 0
                Timestamp(seconds, nanoseconds).toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            } catch (e: Exception) {
                null
            }
        }
        else -> null
    }
}

private fun LocalDateTime.toTimestamp(): Timestamp {
    val date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
    return Timestamp(date)
}
