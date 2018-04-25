package org.catrobat.paintroid.command.implementation;

import android.graphics.Canvas;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.model.LayerModel;

public class MergeLayerCommand implements Command {

	public MergeLayerCommand(int firstLayerId, int secondLayerId) {
	}

	@Override
	public void run(Canvas canvas, LayerModel layerModel) {
	}

	@Override
	public void freeResources() {
	}
}
