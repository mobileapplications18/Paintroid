package org.catrobat.paintroid.command.implementation

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import org.catrobat.paintroid.PaintroidApplication
import org.catrobat.paintroid.command.Command
import org.catrobat.paintroid.listener.LayerListener
import org.catrobat.paintroid.model.LayerModel
import org.catrobat.paintroid.tools.Layer
import org.catrobat.paintroid.tools.Tool
import java.util.*

class CommandManager {

	private val redoCommandList: LinkedList<Command> = LinkedList()
	private val undoCommandList: LinkedList<Command> = LinkedList()
	private val commandListener: MutableList<CommandListener> = mutableListOf()

	private val layerModel get() = LayerListener.getInstance().layerModel
	private val canvas get() = PaintroidApplication.drawingSurface.canvas

	fun addCommandListener(commandListener: CommandListener) {
		this.commandListener.add(commandListener)
	}

	fun removeCommandListener(commandListener: CommandListener) {
		this.commandListener.remove(commandListener)
	}

	fun isUndoAvailable() = undoCommandList.isNotEmpty()

	fun isRedoAvailable() = redoCommandList.isNotEmpty()

	fun addCommand(command: Command) {
		redoCommandList.clear()
		undoCommandList.add(command)
		command.run(canvas, layerModel)

		commandListener.forEach(CommandListener::commandExecuted)
	}

	fun undo() {
		redoCommandList.add(undoCommandList.last)
		undoCommandList.removeLast()

		clearCanvas()

		for(command in undoCommandList)
		{
			command.run(canvas, layerModel)
		}

		commandListener.forEach(CommandListener::commandExecuted)
	}

	fun redo() {
		val command = redoCommandList.last
		redoCommandList.removeLast()
		undoCommandList.add(command)
		command.run(canvas, layerModel)

		commandListener.forEach(CommandListener::commandExecuted)
	}

	fun resetAndClear() {
		TODO()

		commandListener.forEach(CommandListener::commandExecuted)
	}

	fun clearCanvas() {
		val resources = PaintroidApplication.applicationContext.resources
		val dm = resources.displayMetrics
		val bitmap: Bitmap
		val orientation = resources.configuration.orientation
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			bitmap = Bitmap.createBitmap(dm.heightPixels, dm.widthPixels, Bitmap.Config.ARGB_8888)
		} else {
			bitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888)
		}
		bitmap.eraseColor(Color.TRANSPARENT)
		for(layer in layerModel.getLayers())
			layer.image = bitmap
		PaintroidApplication.drawingSurface.resetBitmap(bitmap)
	}


	interface CommandListener {
		fun commandExecuted()
	}
}
