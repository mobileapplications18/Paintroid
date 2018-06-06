/*
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2015 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
