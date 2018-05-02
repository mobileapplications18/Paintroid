package org.catrobat.paintroid.command.implementation;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.model.LayerModel;

public class AddLayerCommand implements Command {

	@Override
	public void run(@NonNull Canvas canvas, @NonNull LayerModel layerModel) {
	}

	@Override
	public void freeResources() {
	}
}
