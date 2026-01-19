package com.osia.petsos.domain.model

enum class PetAdStatus {
    ACTIVE,     // visible
    RESOLVED,   // mascota encontrada
    EXPIRED,    // vencido por tiempo
    WITHDRAWN,  // retirado por el usuario
    PROCESSING  // subiendo imagenes/procesando
}
