package hu.bme.hit.hu.mcold.gordian.contacts

import hu.bme.hit.hu.mcold.gordian.identity.UserID
import hu.bme.hit.hu.mcold.gordian.security.KeyProviders

data class Contact(
    val id: Long = 0,
    val owner: UserID,
    val uniqueId: UserID? = null,
    val name: String = "Unknown",
    val keys: KeyProviders? = null,
)
