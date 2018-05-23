package org.catrobat.paintroid.command.implementation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import org.catrobat.paintroid.model.BitmapFactory
import org.catrobat.paintroid.model.Layer
import org.catrobat.paintroid.model.LayerModel

class MergeLayerCommand(private val firstLayerPosition: Int, private val secondLayerPosition: Int, private val bitmapFactory: BitmapFactory) : BaseCommand() {

    override fun run(canvas: Canvas, layerModel: LayerModel) {
        val firstLayer = layerModel.getLayer(firstLayerPosition)
        val secondLayer = layerModel.getLayer(secondLayerPosition)

        val mergedBitmap = when {
            firstLayerPosition > secondLayerPosition -> mergeBitmaps(firstLayer, secondLayer)
            else -> mergeBitmaps(secondLayer, firstLayer)
        }

        val layer = Layer(mergedBitmap)
        layerModel.removeLayer(firstLayer)
        layerModel.addLayer(layer)
        layerModel.removeLayer(secondLayer)
    }

    private fun mergeBitmaps(firstLayer: Layer, secondLayer: Layer): Bitmap {
        val firstBitmap = firstLayer.image
        val secondBitmap = secondLayer.image

        val bmpOverlay = bitmapFactory.createBitmap(firstBitmap.width, firstBitmap.height, firstBitmap.config)
        val canvas = Canvas(bmpOverlay)

    		val overlayPaint = Paint()
	    	overlayPaint.alpha = 255

		    canvas.drawBitmap(firstBitmap, Matrix(), overlayPaint)
		    canvas.drawBitmap(secondBitmap, 0f, 0f, overlayPaint)

		    return bmpOverlay
	}

}
