package hu.bme.hit

import com.example.szakchat.extensions.toByteArray
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun longShiftCorrect() {
        val test = Long.MAX_VALUE
        val bytes = test.toByteArray()
        org.junit.Assert.assertArrayEquals(bytes, byteArrayOf(127, -1, -1, -1, -1, -1, -1, -1))
    }
}