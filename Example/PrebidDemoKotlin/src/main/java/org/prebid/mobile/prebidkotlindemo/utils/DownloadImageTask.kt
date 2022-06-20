package org.prebid.mobile.prebidkotlindemo.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.LogUtil
import java.net.URL

class DownloadImageTask(
    private val image: ImageView,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    fun execute(url: String) = coroutineScope.launch {
        val bitmap = downloadImage(imageUrl = url)
        launch(Dispatchers.Main) {
            image.setImageBitmap(bitmap)
        }
    }

    private fun downloadImage(imageUrl: String): Bitmap? {
        return try {
            val image = URL(imageUrl).openStream()
            BitmapFactory.decodeStream(image)
        } catch (exc: Exception) {
            LogUtil.error("Error",exc.message)
            null
        }
    }

}