package org.catrobat.paintroid.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import java.util.*
import kotlin.math.max

class LayerModel(initialBitmap: Bitmap) {
  
    companion object {
        const val MAX_LAYER = 4
    }

    var bitmapFactory: BitmapFactory = BitmapFactory()
    var currentLayer: Layer

    private val layerList: MutableList<Layer> = mutableListOf()

    init {
        val layer = Layer(initialBitmap)
        addLayer(layer)
        currentLayer = layer
    }

    fun getLayerCount() = layerList.size

    fun getLayer(index: Int) = layerList[index]

    fun getPosition(layer: Layer) = layerList.indexOf(layer)

    fun getCurrentPosition() = layerList.indexOf(currentLayer)

    @Deprecated("Do not access layerList directly")
    fun getLayers(): List<Layer> = layerList

    fun addLayer(layer: Layer): Boolean {
        if (layerList.size < MAX_LAYER) {
            layerList.add(layer)
            currentLayer = layer
            return true
        }

        return false
    }

    fun removeLayer(layer: Layer) {
        if (layerList.size == 1) throw UnsupportedOperationException()

        val currentPosition = getCurrentPosition()
        val layerPosition = getPosition(layer)
        val position = when {
            layerPosition == currentPosition -> max(currentPosition - 1, 0)
            layerPosition < currentPosition -> currentPosition - 1
            else -> currentPosition
        }
        layerList.remove(layer)
        currentLayer = getLayer(position)
    }

    fun clearLayer() {
        val oldBitmap = layerList.first().image
        val image = bitmapFactory.createBitmap(oldBitmap.width, oldBitmap.height, Bitmap.Config.ARGB_8888)

        layerList.clear()
        val layer = Layer(image)
        addLayer(layer)
    }

    fun swapLayer(posMarkedLayer: Int, targetPosition: Int) {
        if (posMarkedLayer >= 0 && posMarkedLayer < layerList.size
                && targetPosition >= 0 && targetPosition < layerList.size) {
            if (posMarkedLayer < targetPosition) {
                for (i in posMarkedLayer until targetPosition) {
                    Collections.swap(layerList, i, i + 1)
                }
            } else if (posMarkedLayer > targetPosition) {
                for (i in posMarkedLayer downTo targetPosition + 1) {
                    Collections.swap(layerList, i, i - 1)
                }
            }
        }
    }

    fun getBitmapToSave(): Bitmap? {
        val overlayPaint = Paint()

        val result = layerList.foldRight<Layer, Pair<Bitmap, Canvas>?>(null) { layer, acc ->
            val bitmap = layer.image
            val pair = acc
                    ?: bitmapFactory.createBitmap(bitmap.width, bitmap.height, bitmap.config).let { it to Canvas(it) }

            pair.second.drawBitmap(bitmap, Matrix(), overlayPaint)

            return@foldRight pair
        }

        return result?.first
    }
}
