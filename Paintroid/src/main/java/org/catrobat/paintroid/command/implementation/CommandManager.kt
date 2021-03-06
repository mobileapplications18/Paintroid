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

package org.catrobat.paintroid.command.implementation

import android.graphics.Canvas
import android.support.annotation.VisibleForTesting
import org.catrobat.paintroid.PaintroidApplication
import org.catrobat.paintroid.command.Command
import org.catrobat.paintroid.model.BitmapFactory
import org.catrobat.paintroid.model.LayerModel
import java.lang.ref.WeakReference
import java.util.*

class CommandManager(private val layerModel: LayerModel) {

	@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
	var bitmapFactory = BitmapFactory()

	private val redoCommandList = Stack<Command>()
	private val undoCommandList = Stack<Command>()
	private val commandListener = mutableListOf<WeakReference<CommandListener>>()

	private val canvas get() = Canvas(layerModel.currentLayer.image)

	fun addCommandListener(commandListener: CommandListener) {
		this.commandListener.add(WeakReference(commandListener))
	}

	fun removeCommandListener(commandListener: CommandListener) {
		this.commandListener.removeAll { it.get() == commandListener }
	}

	fun isUndoAvailable() = undoCommandList.isNotEmpty()

	fun isRedoAvailable() = redoCommandList.isNotEmpty()

	fun addCommand(command: Command) {
		redoCommandList.clear()
		undoCommandList.add(command)

		command.run(canvas, layerModel)

		notifyCommandExecuted()
	}

	fun undo() {
		val command = undoCommandList.pop()
		redoCommandList.add(command)

		layerModel.clearLayer()
		PaintroidApplication.drawingSurface.resetBitmap(layerModel.currentLayer.image)

		undoCommandList.forEach {
			it.run(canvas, layerModel)
		}

		notifyCommandExecuted()
	}

	fun redo() {
		val command = redoCommandList.pop()
		undoCommandList.add(command)

		command.run(canvas, layerModel)

		notifyCommandExecuted()
	}

	fun resetAndClear() {
		undoCommandList.clear()
		redoCommandList.clear()

		layerModel.clearLayer()
		PaintroidApplication.drawingSurface.resetBitmap(layerModel.currentLayer.image)

		notifyCommandExecuted()
	}

	private fun notifyCommandExecuted() {
		commandListener.forEach {
			it.get()?.commandExecuted()
		}
	}

	interface CommandListener {
		fun commandExecuted()
	}
}
