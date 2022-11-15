package hu.bme.gordian.hu.mcold.gordian.network

import com.example.szakchat.identity.UserID

data class Credentials(
    val id: UserID,
    val pass: String,
)
