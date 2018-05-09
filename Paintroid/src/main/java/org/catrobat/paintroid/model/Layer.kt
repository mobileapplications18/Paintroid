package org.catrobat.paintroid.model

import android.graphics.Bitmap
import org.catrobat.paintroid.PaintroidApplication

class Layer(bitmap: Bitmap) {
	var image: Bitmap = bitmap
		set(image) {
			field = image
			if (selected && PaintroidApplication.drawingSurface != null) {
				PaintroidApplication.drawingSurface.setBitmap(image) // todo this is so bad...
			}
		}

	var selected: Boolean = false

	@Deprecated("always 255")
	val scaledOpacity: Int
		get() = 255

}
