package org.catrobat.paintroid.command.implementation;

import android.graphics.Canvas;

import org.catrobat.paintroid.model.LayerModel;

public class RemoveLayerCommand extends BaseCommand {

	@Override
	public void run(Canvas canvas, LayerModel layerModel) {

		layerModel.removeLayer(layerModel.getCurrentLayer());
	}

	@Override
	public void freeResources() {
	}
}