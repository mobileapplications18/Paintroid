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

package org.catrobat.paintroid.ui;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.listener.DrawingSurfaceListener;
import org.catrobat.paintroid.model.Layer;

import java.util.List;

public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = DrawingSurface.class.getSimpleName();

	protected static final String BUNDLE_INSTANCE_STATE = "BUNDLE_INSTANCE_STATE";
	protected static final String BUNDLE_PERSPECTIVE = "BUNDLE_PERSPECTIVE";
	protected static final String BUNDLE_WORKING_BITMAP = "BUNDLE_WORKING_BITMAP";
	protected static final int BACKGROUND_COLOR = Color.LTGRAY;

	private final Object drawingLock = new Object();
	protected boolean surfaceCanBeUsed;
	private DrawingSurfaceThread drawingThread;
	@VisibleForTesting
	public Bitmap workingBitmap;
	private Rect workingBitmapRect;
	private Canvas workingBitmapCanvas;
	private Paint framePaint;
	private Paint clearPaint;
	private Paint opacityPaint;
	private Paint checkeredPattern;
	private boolean lock;
	private boolean visible;
	private boolean drawingSurfaceDirtyFlag = false;
	private DrawingSurfaceListener drawingSurfaceListener;

	public DrawingSurface(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		init();
	}

	public DrawingSurface(Context context) {
		super(context);
		init();
	}

	public boolean getLock() {
		return lock;
	}

	public void setLock(boolean locked) {
		lock = locked;
	}

	public boolean getVisible() {
		return visible;
	}

	public void setVisible(boolean visibilityToSet) {
		visible = visibilityToSet;
	}

	public Canvas getCanvas() {
		return workingBitmapCanvas;
	}

	private synchronized void doDraw(Canvas surfaceViewCanvas) {
		try {
			if (workingBitmapRect == null || surfaceViewCanvas == null
					|| workingBitmapCanvas == null || isWorkingBitmapRecycled()) {
				return;
			}

			PaintroidApplication.perspective.applyToCanvas(surfaceViewCanvas);
			surfaceViewCanvas.save();
			surfaceViewCanvas.clipRect(workingBitmapRect, Region.Op.DIFFERENCE);
			surfaceViewCanvas.drawColor(BACKGROUND_COLOR);
			surfaceViewCanvas.restore();
			surfaceViewCanvas.drawRect(workingBitmapRect, checkeredPattern);
			surfaceViewCanvas.drawRect(workingBitmapRect, framePaint);

			if (workingBitmap != null && !workingBitmap.isRecycled() && surfaceCanBeUsed) {
				List<Layer> layers = PaintroidApplication.layerModel.getLayers();
				for (Layer layer : layers) {
					surfaceViewCanvas.drawBitmap(layer.getImage(), 0, 0, opacityPaint);
				}
				PaintroidApplication.currentTool.draw(surfaceViewCanvas);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		getHolder().addCallback(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		getHolder().removeCallback(this);
	}

	private void init() {

		workingBitmapRect = new Rect();
		workingBitmapCanvas = new Canvas();

		framePaint = new Paint();
		framePaint.setColor(Color.BLACK);
		framePaint.setStyle(Paint.Style.STROKE);

		clearPaint = new Paint();
		clearPaint.setColor(Color.TRANSPARENT);
		clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		opacityPaint = new Paint();

		Bitmap checkerboard = BitmapFactory.decodeResource(getResources(), R.drawable.checkeredbg);
		BitmapShader shader = new BitmapShader(checkerboard, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		checkeredPattern = new Paint();
		checkeredPattern.setShader(shader);
		setLock(false);
		setVisible(true);
		drawingSurfaceListener = new DrawingSurfaceListener();
		setOnTouchListener(drawingSurfaceListener);
	}

	public void refreshDrawingSurface() {
		synchronized (drawingLock) {
			drawingSurfaceDirtyFlag = true;
			drawingLock.notify();
		}
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(BUNDLE_INSTANCE_STATE, super.onSaveInstanceState());
		bundle.putParcelable(BUNDLE_WORKING_BITMAP, workingBitmap);
		bundle.putSerializable(BUNDLE_PERSPECTIVE, PaintroidApplication.perspective);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			PaintroidApplication.perspective = (Perspective) bundle.getSerializable(BUNDLE_PERSPECTIVE);
			resetBitmap((Bitmap) bundle.getParcelable(BUNDLE_WORKING_BITMAP));

			super.onRestoreInstanceState(bundle.getParcelable(BUNDLE_INSTANCE_STATE));
		} else {
			super.onRestoreInstanceState(state);
		}
	}

	public synchronized void resetBitmap(Bitmap bitmap) {
		setBitmap(bitmap);
		PaintroidApplication.perspective.resetScaleAndTranslation();

		if (surfaceCanBeUsed) {
			drawingThread.start();
		}
	}

	public synchronized void setBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			workingBitmap = bitmap;
			workingBitmapCanvas.setBitmap(workingBitmap);
			workingBitmapRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
		}
	}

	public synchronized Bitmap getBitmapCopy() {
		return !isWorkingBitmapRecycled() ? Bitmap.createBitmap(workingBitmap) : null;
	}

	private boolean isWorkingBitmapRecycled() {
		return workingBitmap == null || workingBitmap.isRecycled();
	}

	public synchronized boolean isDrawingSurfaceBitmapValid() {
		return !isWorkingBitmapRecycled() && !surfaceCanBeUsed;
	}

	public synchronized boolean isPointOnCanvas(PointF point) {
		if (isWorkingBitmapRecycled()) {
			return false;
		}

		Rect boundsCanvas = workingBitmapCanvas.getClipBounds();
		return boundsCanvas.contains((int) point.x, (int) point.y);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		surfaceCanBeUsed = true;
		PaintroidApplication.perspective.setSurfaceHolder(holder);

		if (workingBitmap != null && drawingThread != null) {
			drawingThread.start();
		}

		PaintroidApplication.drawingSurface.refreshDrawingSurface();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		drawingThread = new DrawingSurfaceThread(new DrawLoop());
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceCanBeUsed = false;
		if (drawingThread != null) {
			drawingThread.stop();
		}
	}

	public int getPixel(PointF coordinate) {
		try {
			if (!isWorkingBitmapRecycled()) {
				return workingBitmap.getPixel((int) coordinate.x, (int) coordinate.y);
			}
		} catch (IllegalArgumentException e) {
			Log.w(TAG, "getBitmapColor coordinate out of bounds");
		}
		return Color.TRANSPARENT;
	}

	public void getPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
		if (!isWorkingBitmapRecycled()) {
			workingBitmap.getPixels(pixels, offset, stride, x, y, width, height);
		}
	}

	public int getBitmapWidth() {
		if (workingBitmap == null) {
			return -1;
		}
		return workingBitmap.getWidth();
	}

	public int getBitmapHeight() {
		if (workingBitmap == null) {
			return -1;
		}
		return workingBitmap.getHeight();
	}

	public boolean isBitmapNull() {
		return workingBitmap == null;
	}

	private class DrawLoop implements Runnable {
		final SurfaceHolder holder = getHolder();

		@Override
		public void run() {

			synchronized (drawingLock) {
				if (!drawingSurfaceDirtyFlag && surfaceCanBeUsed) {
					try {
						drawingLock.wait();
					} catch (InterruptedException e) {
						Log.e(TAG, e.getMessage());
					}
				} else {
					drawingSurfaceDirtyFlag = false;
				}

				if (!surfaceCanBeUsed) {
					return;
				}
			}

			Canvas canvas = null;

			synchronized (holder) {
				try {
					canvas = holder.lockCanvas();
					if (canvas != null) {
						doDraw(canvas);
					}
				} finally {
					if (canvas != null) {
						holder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	}
}
