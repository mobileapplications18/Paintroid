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

package org.catrobat.paintroid.ui.dragndrop;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import org.catrobat.paintroid.model.Layer;

public class MyDragShadowBuilder extends View.DragShadowBuilder {

	private final Layer layer;

	private static Drawable shadow;
	private Bitmap greyBitmap;

	public MyDragShadowBuilder(View imageView, Layer layer) {
		super(imageView);
		this.layer = layer;

		Bitmap buffer = layer.getImage();
		greyBitmap = Bitmap.createBitmap(buffer.getWidth(), buffer.getHeight(), buffer.getConfig());
		greyBitmap.eraseColor(Color.LTGRAY);
	}

	@Override
	public void onProvideShadowMetrics(Point size, Point touch) {
		Bitmap shadowBitmap = mergeBitmaps(greyBitmap, layer.getImage());
		shadow = new BitmapDrawable(getView().getResources(), shadowBitmap);

		final View view = getView();
		final int width = view.getWidth();
		final int height = view.getHeight();

		shadow.setBounds(0, 0, width, height);

		size.set(width, height);
		touch.set(width / 2, height / 2);
	}

	@Override
	public void onDrawShadow(Canvas canvas) {
		shadow.draw(canvas);
	}

	private Bitmap mergeBitmaps(Bitmap first, Bitmap second) {
		Bitmap bmpOverlay = Bitmap.createBitmap(first.getWidth(), first.getHeight(), first.getConfig());
		Canvas canvas = new Canvas(bmpOverlay);

		Paint overlayPaint = new Paint();

		canvas.drawBitmap(first, new Matrix(), overlayPaint);
		canvas.drawBitmap(second, 0, 0, overlayPaint);

		return bmpOverlay;
	}
}
