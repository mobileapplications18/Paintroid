/**
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2015 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.command.implementation;

import android.graphics.*;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import org.catrobat.paintroid.model.LayerModel;

public class PathCommand extends BaseCommand {

	private static final String TAG = PathCommand.class.getSimpleName();

	@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
	public Path path;

	public PathCommand(Paint paint, Path path) {
		super(paint);
		if (path != null) {
			this.path = new Path(path);
		}
	}

	@Override
	public void run(@NonNull Canvas canvas, @NonNull LayerModel layerModel) {
		if (path == null) {
			Log.w(TAG, "Object must not be null in PathCommand.");
			return;
		}

		RectF bounds = new RectF();
		path.computeBounds(bounds, true);
		Rect boundsCanvas = canvas.getClipBounds();

		if (pathInCanvas(bounds, boundsCanvas)) {
			canvas.drawPath(path, paint);
		} else {
			notifyStatus(NotifyStates.COMMAND_FAILED);
		}
	}

	private boolean pathInCanvas(RectF rectangleBoundsPath, Rect rectangleBoundsCanvas) {
		RectF rectangleCanvas = new RectF(rectangleBoundsCanvas);

		float strokeWidth = paint.getStrokeWidth();

		rectangleBoundsPath.bottom = rectangleBoundsPath.bottom + (strokeWidth / 2);
		rectangleBoundsPath.left = rectangleBoundsPath.left - (strokeWidth / 2);
		rectangleBoundsPath.right = rectangleBoundsPath.right + (strokeWidth / 2);
		rectangleBoundsPath.top = rectangleBoundsPath.top - (strokeWidth / 2);

		return RectF.intersects(rectangleCanvas, rectangleBoundsPath);
	}
}
