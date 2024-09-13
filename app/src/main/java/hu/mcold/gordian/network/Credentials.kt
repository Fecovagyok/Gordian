package hu.mcold.gordian.network

import hu.mcold.gordian.login.UserID

data class Credentials(
    val id: UserID,
    val pass: String,
)
