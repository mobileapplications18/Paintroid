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
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.catrobat.paintroid.command.implementation.AddLayerCommand
import org.catrobat.paintroid.command.implementation.MergeLayerCommand
import org.catrobat.paintroid.model.BitmapFactory
import org.catrobat.paintroid.model.LayerModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MergeLayerCommandTest {

    private val bitmapFactory = mock<BitmapFactory> {
        on { createBitmap(any(), any(), any()) } doAnswer { inv ->
            mock {
                on { width } doReturn inv.getArgument<Int>(0)
                on { height } doReturn inv.getArgument<Int>(1)
                on { config } doReturn inv.getArgument<Bitmap.Config>(2)
            }
        }
    }

    private val canvas = mock<Canvas>()

    @Test
    fun testMergeLayer() {
        val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888)).also {
            it.bitmapFactory = bitmapFactory
        }
        assertEquals(1, layerModel.getLayerCount())
        val firstLayer = layerModel.currentLayer

        val addLayerCommand = AddLayerCommand(bitmapFactory)
        addLayerCommand.run(canvas, layerModel)
        val secondLayer = layerModel.currentLayer
        assertNotEquals(firstLayer, secondLayer)
        addLayerCommand.run(canvas, layerModel)
        val thirdLayer = layerModel.currentLayer
        assertNotEquals(firstLayer, thirdLayer)
        assertEquals(3, layerModel.getLayerCount())

        MergeLayerCommand(0, 1, bitmapFactory).run(canvas, layerModel)
        assertEquals(2, layerModel.getLayerCount())

        val mergedLayer = layerModel.currentLayer
        assertNotEquals(firstLayer, mergedLayer)
        assertNotEquals(secondLayer, mergedLayer)
        assertNotEquals(thirdLayer, mergedLayer)
    }
}
