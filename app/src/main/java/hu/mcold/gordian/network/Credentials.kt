package hu.mcold.gordian.network

import haart.bme.hit.hu.mcold.gordian.login.UserID

data class Credentials(
    val id: UserID,
    val pass: String,
)
