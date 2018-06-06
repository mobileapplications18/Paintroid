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

package org.catrobat.paintroid.command.implementation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import org.catrobat.paintroid.PaintroidApplication
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

		PaintroidApplication.drawingSurface.setBitmap(layer.image)
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
