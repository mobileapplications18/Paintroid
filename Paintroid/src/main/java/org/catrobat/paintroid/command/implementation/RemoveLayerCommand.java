package org.catrobat.paintroid.command.implementation;

import android.graphics.Canvas;
import android.support.annotation.NonNull;

import org.catrobat.paintroid.model.LayerModel;

public class RemoveLayerCommand extends BaseCommand {

    @Override
    public void run(@NonNull Canvas canvas, @NonNull LayerModel layerModel) {
        layerModel.removeLayer(layerModel.getCurrentLayer());
    }

    @Override
    public void freeResources() {
    }
}
