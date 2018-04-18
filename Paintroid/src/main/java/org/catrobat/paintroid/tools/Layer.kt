package org.catrobat.paintroid.tools

import android.graphics.Bitmap
import org.catrobat.paintroid.PaintroidApplication

class Layer(layerId: Int, bitmap: Bitmap?) {

	val layerID: Int = layerId

	var image: Bitmap? = bitmap
		set(image) {
			field = image
			if (selected && PaintroidApplication.drawingSurface != null) {
				PaintroidApplication.drawingSurface.setBitmap(image) // todo this is so bad...
			}
		}

	var selected: Boolean = false

	/* remove if not required
	var name: String? = LAYER_PREFIX + layerId
		set(nameTo) {
			if (nameTo != null && nameTo.isNotEmpty()) {
				field = nameTo
			}
		}
	*/

	var locked: Boolean = false
	var visible: Boolean = true
	var opacity: Int = 100

	val scaledOpacity: Int
		get() = Math.round((opacity * 255 / 100).toFloat())

	/*
	companion object {
		private const val LAYER_PREFIX = "Layer "
	}
	*/
}
