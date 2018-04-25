package org.catrobat.paintroid.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import org.catrobat.paintroid.PaintroidApplication
import org.catrobat.paintroid.eventlistener.OnLayerEventListener
import org.catrobat.paintroid.tools.Layer
import java.util.*

class LayerModel(firstLayer: Bitmap) : OnLayerEventListener {

	companion object {
		const val MAX_LAYER = 4
	}

	var bitmapFactory: BitmapFactory = BitmapFactory() // todo DI

	var currentLayer: Layer
	private val layerList: MutableList<Layer> = mutableListOf()
	private var layerCounter = 1

	init {
		val layer = Layer(0, firstLayer)
		layerList.add(layer)
		currentLayer = layer
	}

	fun getLayerCount() = layerList.size

	fun getLayer(index: Int) = layerList[index]

	fun getPosition(layerID: Int) = layerList.indexOfFirst { it.layerID == layerID }

	@Deprecated("Do not access layerList directly")
	fun getLayers(): List<Layer> = layerList

	fun addLayer(): Boolean {
		if (layerList.size < MAX_LAYER) {
			val drawingSurface = PaintroidApplication.drawingSurface
			val image = bitmapFactory.createBitmap(drawingSurface.bitmapWidth,
					drawingSurface.bitmapHeight, Bitmap.Config.ARGB_8888)

			layerList.add(0, Layer(layerCounter, image))
			layerCounter++
			return true
		}

		return false
	}

	fun addLayer(existingLayer: Layer): Boolean {
		if (layerList.size < MAX_LAYER) {
			layerList.add(0, existingLayer)
			return true
		}

		return false
	}

	fun removeLayer(layer: Layer) {
		if (layerList.size > 0) {
			layerList.remove(layer)
		}
	}

	fun mergeLayer(firstLayer: Layer, secondLayer: Layer): Layer {
		val mergedBitmap = when {
			getPosition(firstLayer.layerID) > getPosition(secondLayer.layerID) -> mergeBitmaps(firstLayer, secondLayer)
			else -> mergeBitmaps(secondLayer, firstLayer)
		}

		removeLayer(firstLayer)
		removeLayer(secondLayer)

		val layer = Layer(layerCounter++, mergedBitmap)
		layer.opacity = 100
		addLayer(layer)

		return layer
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

	fun clearLayer(): Layer {
		if (layerList.size >= 1) {
			for (i in layerList.size - 1 downTo 0) {
				layerList.removeAt(i)
			}
		}
		layerCounter = 0
		addLayer()
		return layerList[0]
	}

	/* not used ??
	fun copy(currentLayer: Int) {
		if (layerList.size < MAX_LAYER) {
			val image = layerList[getPosition(currentLayer)].image?.copy(layerList[currentLayer].image?.config, true) // TODO WHAT? once the currentLayer ^= id, once ^= index
			layerList.add(0, Layer(layerCounter, image))
			layerCounter++
			notifyDataSetChanged()
		}
	}
	*/

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

	override fun onLayerAdded(layer: Layer) {
		addLayer(layer)
	}

	override fun onLayerRemoved(layer: Layer) {
		removeLayer(layer)
	}

	override fun onLayerMoved(startPos: Int, targetPos: Int) {
		swapLayer(startPos, targetPos)
	}

	fun getBitmapToSave(): Bitmap {
		val firstBitmap = layerList[layerList.size - 1].image
		val bitmap = bitmapFactory.createBitmap(firstBitmap.width, firstBitmap.height, firstBitmap.config)
		val canvas = Canvas(bitmap)
		val overlayPaint = Paint()
		overlayPaint.alpha = layerList[layerList.size - 1].scaledOpacity
		canvas.drawBitmap(firstBitmap, Matrix(), overlayPaint)

		if (layerList.size > 1) {
			for (i in layerList.size - 2 downTo 0) { // todo: what? -2 ?
				overlayPaint.alpha = layerList[i].scaledOpacity
				canvas.drawBitmap(layerList[i].image, 0f, 0f, overlayPaint)
			}
		}

		return bitmap
	}

	fun checkAllLayerVisible(): Boolean {
		for (layer in layerList) {
			if (layer.visible) {
				return false
			}
		}

		return true
	}

	@Deprecated("Use getLayerCount() instead")
	fun getLayerCounter(): Int {
		return layerCounter
	}
}
