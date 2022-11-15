package hu.bme.hit.exchange

import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

object MyQR {
    private const val width = 120
    private const val height = 120
    private const val scale = 8

    fun createQR(content: String): Bitmap? {
        // this is a small sample use of the QRCodeEncoder class from zxing
        try {
            // generate a 150x150 QR code
            val writer = QRCodeWriter()
            val qr = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
            Log.d("FECO", "ECC: ")

            val (newHeight, newWidth) = qr.height* scale to qr.width* scale

            val pixels = IntArray(newWidth* newWidth) { i: Int ->
                val column = i % newWidth
                val oldColumn = column / scale
                val row = i / newWidth
                val oldRow = row / scale
                if(qr.get(oldColumn, oldRow)) BLACK else WHITE
            }

            val bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, newWidth, 0, 0, newWidth, newHeight)

            return bitmap

        } catch (e: WriterException) { //eek }
            Log.e("FECO", "Qr error: ${e.message}")
            return null
        }
    }
}