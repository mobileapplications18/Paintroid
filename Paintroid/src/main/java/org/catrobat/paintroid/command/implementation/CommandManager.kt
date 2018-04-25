package org.catrobat.paintroid.command.implementation

import org.catrobat.paintroid.PaintroidApplication
import org.catrobat.paintroid.command.Command
import org.catrobat.paintroid.listener.LayerListener
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
		TODO()

		commandListener.forEach(CommandListener::commandExecuted)
	}

	fun redo() {
		TODO()

		commandListener.forEach(CommandListener::commandExecuted)
	}

	fun resetAndClear() {
		TODO()

		commandListener.forEach(CommandListener::commandExecuted)
	}

	interface CommandListener {
		fun commandExecuted()
	}
}
