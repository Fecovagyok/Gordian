package hu.mcold.gordian.database

import androidx.room.*

@Entity(
    tableName = "messages",
    foreignKeys = [
            ForeignKey(
            entity = RoomContact::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("contact_id"),
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["owner", "contact_id"], unique = false),
        Index(value = ["contact_id"], unique = false),
    ]
)
data class RoomMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "contact_id")
    val contactId: Long,
    val date: Long,
    val owner: String,
    val text: String,
    val incoming: Boolean,
    val sent: Boolean,
)
