package org.catrobat.paintroid.listener

interface LayerActionListener {

	fun selectLayer(position: Int)

	fun createLayer()

	fun deleteLayer()

	fun moveLayer(layerToMovePosition: Int, targetPosition: Int)

	fun mergeLayer(firstLayerPosition: Int, secondLayerPosition: Int)

	fun moveLayerTemporarily(layerToMovePosition: Int, targetPosition: Int)
}
