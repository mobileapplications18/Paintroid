package org.catrobat.paintroid.model

import android.graphics.Bitmap
import android.graphics.Paint

class BitmapFactory {

    fun createBitmap(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        return Bitmap.createBitmap(width, height, config)
    }

}
