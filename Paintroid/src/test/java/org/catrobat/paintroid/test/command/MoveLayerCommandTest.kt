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
import org.catrobat.paintroid.PaintroidApplication
import org.catrobat.paintroid.command.implementation.AddLayerCommand
import org.catrobat.paintroid.command.implementation.MoveLayerCommand
import org.catrobat.paintroid.model.BitmapFactory
import org.catrobat.paintroid.model.LayerModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MoveLayerCommandTest {

	private val bitmapFactory = mock<BitmapFactory> {
		on { createBitmap(any(), any(), any()) } doReturn mock<Bitmap>()
	}

	@Mock
	lateinit var canvas: Canvas

	private val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888))

	@Before
	fun setup() {
		PaintroidApplication.drawingSurface = mock()
	}

	@Test
	fun testTwoLayers() {
		val firstLayer = layerModel.currentLayer
		val firstLayerPosition = layerModel.getPosition(firstLayer)
		assertEquals(0, firstLayerPosition)
		assertEquals(firstLayerPosition, layerModel.getCurrentPosition())

		AddLayerCommand(bitmapFactory).run(canvas, layerModel)
		assertEquals(2, layerModel.getLayerCount())
		assertEquals(1, layerModel.getCurrentPosition())

		val secondLayer = layerModel.currentLayer
		assertNotEquals(firstLayer, secondLayer)

		MoveLayerCommand(1, 0).run(canvas, layerModel)
		assertEquals(0, layerModel.getCurrentPosition())
		assertEquals(secondLayer, layerModel.currentLayer)
	}

	@Test
	fun testThreeLayers() {
		val firstLayer = layerModel.currentLayer
		val firstLayerPosition = layerModel.getPosition(firstLayer)
		assertEquals(0, firstLayerPosition)
		assertEquals(firstLayerPosition, layerModel.getCurrentPosition())

		AddLayerCommand(bitmapFactory).run(canvas, layerModel)
		assertEquals(2, layerModel.getLayerCount())
		assertEquals(1, layerModel.getCurrentPosition())
		assertEquals(0, layerModel.getPosition(firstLayer))

		val secondLayer = layerModel.currentLayer
		assertNotEquals(firstLayer, secondLayer)

		AddLayerCommand(bitmapFactory).run(canvas, layerModel)
		assertEquals(3, layerModel.getLayerCount())
		assertEquals(2, layerModel.getCurrentPosition())
		assertEquals(1, layerModel.getPosition(secondLayer))
		assertEquals(0, layerModel.getPosition(firstLayer))

		val thirdLayer = layerModel.currentLayer
		assertNotEquals(firstLayer, thirdLayer)
		assertNotEquals(secondLayer, thirdLayer)

		MoveLayerCommand(0, 2).run(canvas, layerModel)
		assertEquals(2, layerModel.getCurrentPosition())
		assertEquals(2, layerModel.getPosition(firstLayer))
		assertEquals(1, layerModel.getPosition(thirdLayer))
		assertEquals(0, layerModel.getPosition(secondLayer))
		assertEquals(firstLayer, layerModel.currentLayer)
	}

}
