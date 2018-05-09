/*
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2018 The Catrobat Team
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

package org.catrobat.paintroid.test.command

import android.graphics.Bitmap
import android.graphics.Canvas
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.catrobat.paintroid.command.implementation.AddLayerCommand
import org.catrobat.paintroid.command.implementation.SelectLayerCommand
import org.catrobat.paintroid.model.BitmapFactory
import org.catrobat.paintroid.model.LayerModel
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SelectLayerCommandTest {

	private val bitmapFactory = mock<BitmapFactory> {
		on { createBitmap(any(), any(), any()) } doReturn mock<Bitmap>()
	}

	@Mock
	lateinit var canvas: Canvas

	private val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888))

	@Test
	fun testSelect() {
		val firstLayer = layerModel.currentLayer

		AddLayerCommand(bitmapFactory).run(canvas, layerModel)
		Assert.assertEquals(2, layerModel.getLayerCount())

		val secondLayer = layerModel.currentLayer
		Assert.assertNotEquals(firstLayer, secondLayer)

		SelectLayerCommand(firstLayer.layerID).run(canvas, layerModel)
		Assert.assertEquals(firstLayer, layerModel.currentLayer)
	}

}
