package org.catrobat.paintroid.command.implementation

import android.graphics.Canvas
import org.catrobat.paintroid.command.Command
import org.catrobat.paintroid.model.LayerModel

class MoveLayerCommand(private val layerPosition: Int, private val targetPosition: Int) : Command {

	override fun run(canvas: Canvas, layerModel: LayerModel) {
		layerModel.swapLayer(layerPosition, targetPosition)
		layerModel.currentLayer = layerModel.getLayer(targetPosition)
	}

	override fun freeResources() {
	}

}
