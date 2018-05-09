package org.catrobat.paintroid.command.implementation;

import android.graphics.Canvas;

import org.catrobat.paintroid.model.Layer;
import org.catrobat.paintroid.model.LayerModel;

import static java.lang.Math.max;

public class RemoveLayerCommand extends BaseCommand {

	@Override
	public void run(Canvas canvas, LayerModel layerModel) {

		int position = max(layerModel.getPosition(layerModel.getCurrentLayer()) - 1,0);
		layerModel.removeLayer(layerModel.getCurrentLayer());
		layerModel.setCurrentLayer(layerModel.getLayer(position));

	}

	@Override
	public void freeResources() {
	}
}