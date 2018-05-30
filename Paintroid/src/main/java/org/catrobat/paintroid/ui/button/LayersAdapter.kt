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

package org.catrobat.paintroid.ui.button

import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import org.catrobat.paintroid.R
import org.catrobat.paintroid.model.Layer
import org.catrobat.paintroid.model.LayerModel
import java.util.*

class LayersAdapter(layerModel: LayerModel) : BaseAdapter() {

	private var layers: List<Layer> = layerModel.getLayers()
	private var currentLayer: Layer = layerModel.currentLayer

	fun updateLayers(layerModel: LayerModel) {
		this.layers = ArrayList(layerModel.getLayers())
		this.currentLayer = layerModel.currentLayer
		notifyDataSetChanged()
	}

	override fun getCount(): Int {
		return layers.size
	}

	override fun getItem(position: Int): Any {
		return layers[layers.size - position - 1]
	}

	override fun getItemId(position: Int): Long {
		return -1
	}

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val context = parent.context

		val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.layer_button, parent, false)
		val layerButton = view.findViewById(R.id.layer_button) as LinearLayout
		val imageView = view.findViewById(R.id.layer_button_image) as ImageView

		val layer = getItem(position) as Layer
		if (layer == currentLayer) {
			layerButton.setBackgroundColor(ContextCompat.getColor(context, R.color.color_chooser_blue1))
		} else {
			layerButton.setBackgroundColor(ContextCompat.getColor(context, R.color.custom_background_color))
		}
		imageView.setImageBitmap(layer.image)
		view.visibility = View.VISIBLE

		return view
	}

	fun getPosition(viewPosition: Int): Int {
		return layers.size - viewPosition - 1
	}

	fun swapLayer(posMarkedLayer: Int, targetPosition: Int) {
		if (posMarkedLayer >= 0 && posMarkedLayer < layers.size
				&& targetPosition >= 0 && targetPosition < layers.size) {
			if (posMarkedLayer < targetPosition) {
				for (i in posMarkedLayer until targetPosition) {
					Collections.swap(layers, i, i + 1)
				}
			} else if (posMarkedLayer > targetPosition) {
				for (i in posMarkedLayer downTo targetPosition + 1) {
					Collections.swap(layers, i, i - 1)
				}
			}
		}
		notifyDataSetChanged()
	}
}
