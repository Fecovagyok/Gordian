package hu.bme.hit.network

import com.example.szakchat.identity.UserID

data class Credentials(
    val id: UserID,
    val pass: String,
)
