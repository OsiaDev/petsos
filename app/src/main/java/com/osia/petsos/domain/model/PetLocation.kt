package com.osia.petsos.domain.model

data class PetLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val address: String = ""
)
