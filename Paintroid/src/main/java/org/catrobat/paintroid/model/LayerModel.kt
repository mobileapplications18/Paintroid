package org.catrobat.paintroid.model

import android.graphics.Bitmap
import org.catrobat.paintroid.tools.Layer
import org.catrobat.paintroid.ui.button.LayersAdapter

class LayerModel(firstLayer: Bitmap?) {

	val layersAdapter: LayersAdapter = LayersAdapter(firstLayer) // todo: reduce access to this getter where possible
	var currentLayer: Layer? = null

}
