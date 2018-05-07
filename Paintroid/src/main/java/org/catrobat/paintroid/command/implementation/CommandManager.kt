package org.catrobat.paintroid.command.implementation

import android.graphics.Color
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

	private val canvas get() = PaintroidApplication.drawingSurface.canvas

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

		clearCanvas()
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

		clearCanvas()
		layerModel.clearLayer()

		notifyCommandExecuted()
	}

	private fun notifyCommandExecuted() {
		commandListener.forEach {
			it.get()?.commandExecuted()
		}
	}

	private fun clearCanvas() {
		layerModel.getLayers().forEach { layer ->
			layer.image.eraseColor(Color.TRANSPARENT)
		}

		PaintroidApplication.drawingSurface.resetBitmap(layerModel.currentLayer.image)
	}

	interface CommandListener {
		fun commandExecuted()
	}
}
