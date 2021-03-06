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

package org.catrobat.paintroid.command.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.model.BitmapFactory;
import org.catrobat.paintroid.model.Layer;
import org.catrobat.paintroid.model.LayerModel;
import org.catrobat.paintroid.ui.DrawingSurface;

public class AddLayerCommand extends BaseCommand {

    private BitmapFactory bitmapFactory;

    public AddLayerCommand(BitmapFactory bitmapFactory) {
        this.bitmapFactory = bitmapFactory;
    }

    @Override
    public void run(@NonNull Canvas canvas, @NonNull LayerModel layerModel) {
        DrawingSurface drawingSurface = PaintroidApplication.drawingSurface; // TODO: reduce drawingSurface access
        int width = drawingSurface.getBitmapWidth();
        int height = drawingSurface.getBitmapHeight();

        Layer layer = new Layer(bitmapFactory.createBitmap(width, height, Bitmap.Config.ARGB_8888));
        layerModel.addLayer(layer);
        PaintroidApplication.drawingSurface.setBitmap(layer.getImage());

        drawingSurface.refreshDrawingSurface();
    }

    @Override
    public void freeResources() {
    }
}
