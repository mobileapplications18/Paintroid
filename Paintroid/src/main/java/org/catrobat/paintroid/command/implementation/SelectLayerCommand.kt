package org.catrobat.paintroid.command.implementation

import android.graphics.Canvas
import org.catrobat.paintroid.command.Command
import org.catrobat.paintroid.model.LayerModel

class SelectLayerCommand(private val layerId: Int) : Command {

	override fun run(canvas: Canvas, layerModel: LayerModel) {
		val position = layerModel.getPosition(layerId)
		val layer = layerModel.getLayer(position)
		layerModel.currentLayer = layer
	}

	override fun freeResources() {
	}

}
