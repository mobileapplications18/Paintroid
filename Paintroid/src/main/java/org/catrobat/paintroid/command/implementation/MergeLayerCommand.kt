package org.catrobat.paintroid.command.implementation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint

import org.catrobat.paintroid.model.BitmapFactory
import org.catrobat.paintroid.model.LayerModel
import org.catrobat.paintroid.model.Layer

class MergeLayerCommand(var firstLayerId : Int, var secondLayerId: Int, var bitmapFactory : BitmapFactory) : BaseCommand() {

    override fun run(canvas: Canvas, layerModel: LayerModel) {

        var firstLayer : Layer = layerModel.getLayer(firstLayerId)
        var secondLayer : Layer = layerModel.getLayer(secondLayerId)

        val mergedBitmap = when {
            layerModel.getPosition(firstLayer) > layerModel.getPosition(secondLayer)
            -> mergeBitmaps(firstLayer,secondLayer)
            else -> mergeBitmaps(secondLayer, firstLayer)
        }

        layerModel.removeLayer(firstLayer)
        layerModel.removeLayer(secondLayer)


        val layer = Layer(layerModel.getLayerCount()+1, mergedBitmap)
        layerModel.addLayer(layer)
    }

    private fun mergeBitmaps(firstLayer: Layer, secondLayer: Layer): Bitmap {
        val firstBitmap = firstLayer.image!!
        val secondBitmap = secondLayer.image!!

        val bmpOverlay = bitmapFactory.createBitmap(firstBitmap.width, firstBitmap.height, firstBitmap.config)
        val canvas = Canvas(bmpOverlay)

        val overlayPaint = Paint()
        overlayPaint.alpha = firstLayer.scaledOpacity

        canvas.drawBitmap(firstBitmap, Matrix(), overlayPaint)
        overlayPaint.alpha = secondLayer.scaledOpacity
        canvas.drawBitmap(secondBitmap, 0f, 0f, overlayPaint)

        return bmpOverlay
    }

}
