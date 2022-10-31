package com.example.szakchat.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["uniqueId"], unique = true),
        Index(value = ["owner"], unique = false),
    ]
)
data class RoomContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uniqueId: String,
    val owner: String,
    val name: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val sendKey: ByteArray,
    val sendNumber: Int,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val receiveKey: ByteArray,
    val receiveNumber: Int,

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomContact

        if (id != other.id) return false
        if (uniqueId != other.uniqueId) return false
        if (owner != other.owner) return false
        if (name != other.name) return false
        if (!sendKey.contentEquals(other.sendKey)) return false
        if (sendNumber != other.sendNumber) return false
        if (!receiveKey.contentEquals(other.receiveKey)) return false
        if (receiveNumber != other.receiveNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uniqueId.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + sendKey.contentHashCode()
        result = 31 * result + sendNumber
        result = 31 * result + receiveKey.contentHashCode()
        result = 31 * result + receiveNumber
        return result
    }

}

