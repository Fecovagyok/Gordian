package hu.mcold.gordian.contacts

import haart.bme.hit.hu.mcold.gordian.login.UserID
import haart.bme.hit.hu.mcold.gordian.security.KeyProviders

data class Contact(
    val id: Long = 0,
    val owner: UserID,
    val uniqueId: UserID? = null,
    val name: String = "Unknown",
    val keys: KeyProviders? = null,
)