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

package org.catrobat.paintroid.ui.button;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.model.LayerModel;
import org.catrobat.paintroid.model.Layer;

public class LayersAdapter extends BaseAdapter {

	private LayerModel layerModel;

	public LayersAdapter(LayerModel layerModel) {
		this.layerModel = layerModel;
	}

	@Override
	public int getCount() {
		return layerModel.getLayerCount();
	}

	@Override
	public Object getItem(int position) {
		return layerModel.getLayer(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			Context context = parent.getContext();
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.layer_button, parent, false);
			LinearLayout layerButton = (LinearLayout) convertView.findViewById(R.id.layer_button);

			Layer layer = layerModel.getLayer(position);
			if (layer == layerModel.getCurrentLayer()) {
				layerButton.setBackgroundColor(ContextCompat.getColor(context, R.color.color_chooser_blue1));
			} else {
				layerButton.setBackgroundColor(ContextCompat.getColor(context, R.color.custom_background_color));
			}
			ImageView imageView = (ImageView) convertView.findViewById(R.id.layer_button_image);
			imageView.setImageBitmap(layer.getImage());
		}
		return convertView;
	}
}
