package hu.mcold.gordian

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import hu.mcold.gordian.common.toBase64String
import hu.mcold.gordian.common.toData
import hu.mcold.gordian.database.Database
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom

class ChatApplication : Application() {
    companion object {
        lateinit var database: Database
            private set
        lateinit var safePrefs: SharedPreferences
            private set
        private const val MASTER_PASS_KEY = "MASTER_PASS_KEY"
    }

    private fun getSecureRandom() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            SecureRandom.getInstanceStrong()
         else
            SecureRandom()

    override fun onCreate() {
        super.onCreate()

        safePrefs = createSafePref()
        val masterPass = safePrefs.getString(MASTER_PASS_KEY, null)?.toData()?: run {
            val random = getSecureRandom()
            // Create random length between 20 and 60
            val randomLength = random.nextInt(40) + 20
            val resultBytes = ByteArray(randomLength)
            random.nextBytes(resultBytes)
            val asString = resultBytes.toBase64String()
            safePrefs.edit().putString(MASTER_PASS_KEY, asString).apply()
            return@run resultBytes
        }

        val factory = SupportFactory(masterPass)
        database = Room.databaseBuilder(
            applicationContext,
            Database::class.java,
            "chat_data"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    private fun createSafePref(): SharedPreferences {
        val masterKey = MasterKey.Builder(applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val msg = masterKey.isStrongBoxBacked
        return EncryptedSharedPreferences.create(
            applicationContext,
            "Gordian_secret",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

}