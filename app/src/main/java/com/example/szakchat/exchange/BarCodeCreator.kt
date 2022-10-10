package com.example.szakchat.exchange

import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.util.Base64
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.encoder.QRCode
import kotlin.math.max

object MyQR {
    private const val width = 120
    private const val height = 120
    private const val scale = 8

    private inline fun times5(iterator: Int, maxSize: Int, block: (Int) -> Unit){
        for(i in 0 until 5){
            block(i* maxSize + iterator)
        }
    }

    fun createQR(byteArray: ByteArray): Bitmap? {
        // this is a small sample use of the QRCodeEncoder class from zxing
        try {
            // generate a 150x150 QR code
            val writer = QRCodeWriter()
            val content = Base64.encodeToString(byteArray, Base64.DEFAULT)
            Log.d("FECO", "Content: $content")
            val qr = writer.encode(content, BarcodeFormat.QR_CODE, width, height)

            val (newHeight, newWidth) = qr.height* scale to qr.width* scale

            val pixels = IntArray(newWidth* newWidth) { i: Int ->
                val column = i % newWidth
                val oldColumn = column / scale
                val row = i / newWidth
                val oldRow = row / scale
                if(qr.get(oldColumn, oldRow)) BLACK else WHITE
            }

            /*for (y in 0 until qr.height) {
                times5(y, newHeight) { ny: Int ->
                    val offset = ny * qr.width
                    for (x in 0 until qr.width) {
                        times5(x, newWidth) { nx: Int ->
                            pixels[offset + nx] = if (qr.get(x, y)) BLACK else WHITE
                        }
                    }
                }
            }*/

            val bitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, newWidth, 0, 0, newWidth, newHeight)

            return bitmap

        } catch (e: WriterException) { //eek }
            Log.e("FECO", "Qr error: ${e.message}")
            return null
        }
    }
}