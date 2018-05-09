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

package org.catrobat.paintroid;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import org.catrobat.paintroid.command.implementation.CommandManager;
import org.catrobat.paintroid.model.BitmapFactory;
import org.catrobat.paintroid.model.LayerModel;
import org.catrobat.paintroid.tools.Tool;
import org.catrobat.paintroid.ui.DrawingSurface;
import org.catrobat.paintroid.ui.Perspective;

import java.util.Locale;

public class PaintroidApplication extends Application {

	public static Context applicationContext;
	public static String defaultSystemLanguage;

	public static LayerModel layerModel;
	public static CommandManager commandManager;

	public static DrawingSurface drawingSurface;
	public static Tool currentTool;
	public static Perspective perspective;

	@Override
	public void onCreate() {
		super.onCreate();

		applicationContext = getApplicationContext();
		defaultSystemLanguage = Locale.getDefault().getLanguage();

		layerModel = new LayerModel(blankBitmap());
		commandManager = new CommandManager(layerModel);
	}

	private Bitmap blankBitmap() {
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		Bitmap bitmap = new BitmapFactory().createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(Color.TRANSPARENT);

		return bitmap;
	}
}
