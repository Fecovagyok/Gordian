package hu.bme.hit.database

import androidx.room.Embedded
import androidx.room.Relation

data class ContactWithMessage(
    @Embedded val contact: RoomContact,
    @Relation(
        parentColumn = "id",
        entityColumn = "contact_id"
    )
    val messages: List<RoomMessage>
)
