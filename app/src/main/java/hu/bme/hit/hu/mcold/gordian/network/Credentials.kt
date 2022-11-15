package hu.bme.hit.hu.mcold.gordian.network

import hu.bme.hit.hu.mcold.gordian.identity.UserID

data class Credentials(
    val id: UserID,
    val pass: String,
)
