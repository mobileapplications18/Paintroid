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
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.catrobat.paintroid.PaintroidApplication
import org.catrobat.paintroid.command.implementation.AddLayerCommand
import org.catrobat.paintroid.command.implementation.CommandManager
import org.catrobat.paintroid.model.BitmapFactory
import org.catrobat.paintroid.model.LayerModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CommandManagerTest {

    private val bitmapFactory: BitmapFactory = mock {
        on { createBitmap(any(), any(), any()) } doReturn mock<Bitmap>()
    }

    private val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888)).also {
        it.bitmapFactory = bitmapFactory
    }

    @Before
    fun setup() {
        PaintroidApplication.drawingSurface = mock()
    }

    @Test
    fun testAddCommand() {
        val commandManager = CommandManager(layerModel)
        Assert.assertFalse(commandManager.isUndoAvailable())
        Assert.assertFalse(commandManager.isRedoAvailable())

        commandManager.addCommand(AddLayerCommand(bitmapFactory))
        Assert.assertTrue(commandManager.isUndoAvailable())
        Assert.assertFalse(commandManager.isRedoAvailable())
    }

    @Test
    fun testUndoRedoCommand() {
        val commandManager = CommandManager(layerModel).also {
            it.bitmapFactory = bitmapFactory
        }
        Assert.assertFalse(commandManager.isUndoAvailable())
        Assert.assertFalse(commandManager.isRedoAvailable())

        commandManager.addCommand(AddLayerCommand(bitmapFactory))
        Assert.assertTrue(commandManager.isUndoAvailable())
        Assert.assertFalse(commandManager.isRedoAvailable())

        commandManager.addCommand(AddLayerCommand(bitmapFactory))
        Assert.assertTrue(commandManager.isUndoAvailable())
        Assert.assertFalse(commandManager.isRedoAvailable())

        commandManager.undo()
        Assert.assertTrue(commandManager.isUndoAvailable())
        Assert.assertTrue(commandManager.isRedoAvailable())

        commandManager.undo()
        Assert.assertFalse(commandManager.isUndoAvailable())
        Assert.assertTrue(commandManager.isRedoAvailable())

        commandManager.redo()
        Assert.assertTrue(commandManager.isUndoAvailable())
        Assert.assertTrue(commandManager.isRedoAvailable())

        commandManager.redo()
        Assert.assertTrue(commandManager.isUndoAvailable())
        Assert.assertFalse(commandManager.isRedoAvailable())
    }

    @Test
    fun testClear() {
        val commandManager = CommandManager(layerModel).also {
            it.bitmapFactory = bitmapFactory
        }
        Assert.assertFalse(commandManager.isUndoAvailable())
        Assert.assertFalse(commandManager.isRedoAvailable())

        commandManager.addCommand(AddLayerCommand(bitmapFactory))
        Assert.assertTrue(commandManager.isUndoAvailable())
        Assert.assertFalse(commandManager.isRedoAvailable())

        commandManager.addCommand(AddLayerCommand(bitmapFactory))
        Assert.assertTrue(commandManager.isUndoAvailable())
        Assert.assertFalse(commandManager.isRedoAvailable())

        commandManager.undo()
        Assert.assertTrue(commandManager.isUndoAvailable())
        Assert.assertTrue(commandManager.isRedoAvailable())

        commandManager.resetAndClear()
        Assert.assertFalse(commandManager.isUndoAvailable())
        Assert.assertFalse(commandManager.isRedoAvailable())
    }
}
