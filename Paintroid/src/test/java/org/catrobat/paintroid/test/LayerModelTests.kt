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

package org.catrobat.paintroid.test

import android.graphics.Bitmap
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.catrobat.paintroid.PaintroidApplication
import org.catrobat.paintroid.model.BitmapFactory
import org.catrobat.paintroid.model.Layer
import org.catrobat.paintroid.model.LayerModel
import org.catrobat.paintroid.ui.DrawingSurface
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LayerModelTests {

    private val bitmapFactory = mock<BitmapFactory> {
        on { createBitmap(any(), any(), any()) } doAnswer { inv ->
            mock {
                on { width } doReturn inv.getArgument<Int>(0)
                on { height } doReturn inv.getArgument<Int>(1)
                // on { config } doReturn inv.getArgument<Bitmap.Config>(2) // reported unnecessary
            }
        }
    }

    @Before
    fun setUp() {
        PaintroidApplication.drawingSurface = mock(DrawingSurface::class.java)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testLayerModel() {
        val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888))

        assertNotNull(layerModel)
        assertEquals(1, layerModel.getLayerCount())

        val firstLayer = layerModel.currentLayer
        assertNotNull(firstLayer)
        assertEquals(0, layerModel.getPosition(firstLayer))
        assertEquals(0, layerModel.getCurrentPosition())
    }

    @Test
    fun testAddLayer() {
        val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888))

        assertEquals(1, layerModel.getLayerCount())
        assertNotNull(layerModel.currentLayer)
        val firstLayer = layerModel.currentLayer

        val secondLayer = createLayer()
        assertTrue(layerModel.addLayer(secondLayer))
        assertEquals(2, layerModel.getLayerCount())
        assertEquals(1, layerModel.getCurrentPosition())
        assertEquals(0, layerModel.getPosition(firstLayer))

        val thirdLayer = createLayer()
        assertTrue(layerModel.addLayer(thirdLayer))
        assertEquals(3, layerModel.getLayerCount())

        val fourthLayer = createLayer()
        assertTrue(layerModel.addLayer(fourthLayer))
        assertEquals(4, layerModel.getLayerCount())

        val fifthLayer = createLayer()
        assertFalse(layerModel.addLayer(fifthLayer))
        assertEquals(4, layerModel.getLayerCount())
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testRemoveLastLayer() {
        val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888))
        assertEquals(1, layerModel.getLayerCount())
        val layer = layerModel.currentLayer

        layerModel.removeLayer(layer)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testRemoveLastLaser() {
        val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888))

        assertEquals(1, layerModel.getLayerCount())
        assertNotNull(layerModel.currentLayer)
        val firstLayer = layerModel.currentLayer

        layerModel.removeLayer(firstLayer)
        assertEquals(0, layerModel.getLayerCount())
    }

    @Test
    fun testRemoveLayer() {
        val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888))

        assertEquals(1, layerModel.getLayerCount())
        assertNotNull(layerModel.currentLayer)
        val firstLayer = layerModel.currentLayer
        assertEquals(0, layerModel.getPosition(firstLayer))

        val secondLayer = createLayer()
        layerModel.addLayer(secondLayer)
        assertEquals(2, layerModel.getLayerCount())
        assertEquals(1, layerModel.getPosition(secondLayer))

        val thirdLayer = createLayer()
        layerModel.addLayer(thirdLayer)
        assertEquals(3, layerModel.getLayerCount())
        assertEquals(2, layerModel.getPosition(thirdLayer))

        val fourthLayer = createLayer()
        layerModel.addLayer(fourthLayer)
        assertEquals(4, layerModel.getLayerCount())
        assertEquals(3, layerModel.getPosition(fourthLayer))

        val fifthLayer = createLayer()
        layerModel.addLayer(fifthLayer)
        assertEquals(4, layerModel.getLayerCount())

        layerModel.removeLayer(thirdLayer)
        assertEquals(3, layerModel.getLayerCount())
        assertEquals(0, layerModel.getPosition(firstLayer))
        assertEquals(1, layerModel.getPosition(secondLayer))
        assertEquals(2, layerModel.getPosition(fourthLayer))

        layerModel.addLayer(fifthLayer)
        assertEquals(4, layerModel.getLayerCount())
        assertEquals(3, layerModel.getPosition(fifthLayer))

        layerModel.removeLayer(thirdLayer)
        assertEquals(4, layerModel.getLayerCount())

        layerModel.removeLayer(firstLayer)
        layerModel.removeLayer(secondLayer)
        layerModel.removeLayer(fourthLayer)
        assertEquals(1, layerModel.getLayerCount())
    }

    @Test
    fun testClearLayer() {
        val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888)).also {
            it.bitmapFactory = bitmapFactory
        }

        val firstLayer = layerModel.currentLayer

        layerModel.addLayer(createLayer())
        val secondLayer = layerModel.currentLayer
        assertNotEquals(firstLayer, secondLayer)

        layerModel.addLayer(createLayer())
        val thirdLayer = layerModel.currentLayer
        assertNotEquals(secondLayer, thirdLayer)

        layerModel.addLayer(createLayer())
        val fourthLayer = layerModel.currentLayer
        assertNotEquals(thirdLayer, fourthLayer)
        assertEquals(4, layerModel.getLayerCount())

        layerModel.clearLayer()
        val newLayer = layerModel.currentLayer
        assertEquals(1, layerModel.getLayerCount())
        assertNotEquals(firstLayer, newLayer)
        assertNotEquals(secondLayer, newLayer)
        assertNotEquals(thirdLayer, newLayer)
        assertNotEquals(fourthLayer, newLayer)
    }

    private fun createLayer(): Layer {
        return Layer(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888))
    }
}
