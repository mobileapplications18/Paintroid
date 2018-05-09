package org.catrobat.paintroid.test.command

import android.graphics.Bitmap
import android.graphics.Canvas
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.catrobat.paintroid.command.implementation.AddLayerCommand
import org.catrobat.paintroid.command.implementation.SelectLayerCommand
import org.catrobat.paintroid.model.BitmapFactory
import org.catrobat.paintroid.model.LayerModel
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SelectLayerCommandTest {

	private val bitmapFactory = mock<BitmapFactory> {
		on { createBitmap(any(), any(), any()) } doReturn mock<Bitmap>()
	}

	@Mock
	lateinit var canvas: Canvas

	private val layerModel = LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888))

	@Test
	fun testSelect() {
		val firstLayer = layerModel.currentLayer

		AddLayerCommand(bitmapFactory).run(canvas, layerModel)
		Assert.assertEquals(2, layerModel.getLayerCount())

		val secondLayer = layerModel.currentLayer
		Assert.assertNotEquals(firstLayer, secondLayer)

		SelectLayerCommand(firstLayer.layerID).run(canvas, layerModel)
		Assert.assertEquals(firstLayer, layerModel.currentLayer)
	}

}
