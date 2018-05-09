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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import org.catrobat.paintroid.command.implementation.LoadCommand;
import org.catrobat.paintroid.common.Constants;
import org.catrobat.paintroid.dialog.*;
import org.catrobat.paintroid.dialog.InfoDialog.DialogType;
import org.catrobat.paintroid.dialog.colorpicker.ColorPickerDialog;
import org.catrobat.paintroid.listener.LayerListener;
import org.catrobat.paintroid.model.LayerModel;
import org.catrobat.paintroid.tools.Tool;
import org.catrobat.paintroid.tools.ToolFactory;
import org.catrobat.paintroid.tools.ToolType;
import org.catrobat.paintroid.tools.implementation.ImportTool;
import org.catrobat.paintroid.ui.BottomBar;
import org.catrobat.paintroid.ui.DrawingSurface;
import org.catrobat.paintroid.ui.Perspective;
import org.catrobat.paintroid.ui.TopBar;

import java.io.File;

import static org.catrobat.paintroid.common.Constants.PAINTROID_PICTURE_PATH;
import static org.catrobat.paintroid.common.Constants.TEMP_PICTURE_NAME;

public class MainActivity extends NavigationDrawerMenuActivity implements NavigationView.OnNavigationItemSelectedListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	public static int colorPickerInitialColor = Color.BLACK;

	@VisibleForTesting
	public String catroidPicturePath;

	private BottomBar bottomBar;
	@VisibleForTesting
	public TopBar topBar;
	private LayerListener layerListener;

	private DrawerLayout drawerLayout;
	private DrawingSurface drawingSurface;
	private NavigationView layerSideNav;
	private NavigationView navigationView;

	private boolean isFullScreen;
	private boolean isKeyboardShown;
	private Bundle toolBundle = new Bundle();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		onCreateView();

		// Parse Intent
		String tempPicturePath = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			tempPicturePath = extras.getString(PAINTROID_PICTURE_PATH);
			Log.d(TAG, "catroidPicturePath: " + tempPicturePath);
		}
		if (tempPicturePath != null) {
			openedFromCatroid = true;
			if (!tempPicturePath.equals("")) {
				catroidPicturePath = tempPicturePath;
				scaleImage = false;
			}
			ActionBar supportActionBar = getSupportActionBar();
			if (supportActionBar != null) {
				supportActionBar.setDisplayHomeAsUpEnabled(true);
				supportActionBar.setDisplayShowHomeEnabled(true);
			}
		} else {
			openedFromCatroid = false;
		}

		initialiseNewBitmap(); // TODO

		if (openedFromCatroid) {
			PaintroidApplication.commandManager.resetAndClear();

			if (catroidPicturePath != null && catroidPicturePath.length() > 0) {
				loadBitmapFromUriAndRun(Uri.fromFile(new File(catroidPicturePath)),
						new RunnableWithBitmap() {
							@Override
							public void run(Bitmap bitmap) {
								if (!bitmap.hasAlpha()) {
									bitmap.setHasAlpha(true);
								}
								handleAndAssignImage(bitmap);
							}

							private void handleAndAssignImage(Bitmap bitmap) {
								PaintroidApplication.commandManager.addCommand(new LoadCommand(bitmap));
							}
						});
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "MainActivity onConfigurationChanged");
		super.onConfigurationChanged(newConfig);

		if (isFinishing()) {
			Log.d(TAG, "MainActivity onConfigurationChanged called, but is finishing.");
			return;
		}

		boolean isProgressDialogShowing = IndeterminateProgressDialog.getInstance().isShowing();

		onDestroyView();
		setContentView(R.layout.main);
		onCreateView();
		hideKeyboard();

		boolean isRTL = newConfig.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
		View mainView = findViewById(R.id.drawer_layout);
		mainView.setLayoutDirection(isRTL ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

		if (isProgressDialogShowing) {
			IndeterminateProgressDialog.getInstance().show();
		}

		PaintroidApplication.perspective.resetScaleAndTranslation();
		PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.NEW_IMAGE_LOADED);
	}

	private void onCreateView() {
		initLocaleConfiguration();

		IndeterminateProgressDialog.init(this);
		ColorPickerDialog.init(this);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawingSurface = (DrawingSurface) findViewById(R.id.drawingSurfaceView);
		layerSideNav = (NavigationView) findViewById(R.id.nav_view_layer);
		navigationView = (NavigationView) findViewById(R.id.nav_view);

		final Resources resources = getApplicationContext().getResources();
		final DisplayMetrics metrics = resources.getDisplayMetrics();

		PaintroidApplication.drawingSurface = drawingSurface; // !! set drawingSurface before creating BottomBar
		PaintroidApplication.perspective = new Perspective(drawingSurface.getHolder(), metrics.density);

		bottomBar = new BottomBar(this);
		topBar = new TopBar(this);
		layerListener = new LayerListener(this, layerSideNav);

		initActionBar();
		initNavigationDrawer();
		initKeyboardIsShownListener();
		setFullScreen(false);

		int colorPickerBackgroundColor = colorPickerInitialColor;
		ColorPickerDialog.getInstance().setInitialColor(colorPickerBackgroundColor);

		PaintroidApplication.commandManager.addCommandListener(layerListener);
		PaintroidApplication.commandManager.addCommandListener(topBar);
	}

	private void initActionBar() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBar supportActionBar = getSupportActionBar();
		if (supportActionBar != null) {
			getSupportActionBar().setDisplayShowTitleEnabled(false);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
				this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				drawerLayout.requestLayout();
			}
		};

		drawerLayout.addDrawerListener(actionBarDrawerToggle);
		actionBarDrawerToggle.syncState();
	}

	private void initNavigationDrawer() {
		NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
		mNavigationView.setNavigationItemSelectedListener(this);

		if (!openedFromCatroid) {
			mNavigationView.getMenu().removeItem(R.id.nav_back_to_pocket_code);
			mNavigationView.getMenu().removeItem(R.id.nav_export);
		} else {
			mNavigationView.getMenu().removeItem(R.id.nav_save_image);
			mNavigationView.getMenu().removeItem(R.id.nav_save_duplicate);
		}

		if (PaintroidApplication.perspective.getFullscreen()) {
			mNavigationView.getMenu().findItem(R.id.nav_fullscreen_mode).setVisible(false);
		} else {
			mNavigationView.getMenu().findItem(R.id.nav_exit_fullscreen_mode).setVisible(false);
		}
	}

	private void onDestroyView() {
		IndeterminateProgressDialog.getInstance().dismiss();
		ColorPickerDialog.getInstance().dismiss();

		PaintroidApplication.commandManager.removeCommandListener(layerListener);
		PaintroidApplication.commandManager.removeCommandListener(topBar);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkIfLoadBitmapFailed();
	}

	public void checkIfLoadBitmapFailed() {
		if (loadBitmapFailed) {
			loadBitmapFailed = false;
			InfoDialog.newInstance(DialogType.WARNING,
					R.string.dialog_loading_image_failed_title,
					R.string.dialog_loading_image_failed_text).show(
					getSupportFragmentManager(), "loadbitmapdialogerror");
		}
	}

	@Override
	public void onDetachedFromWindow() {
		Log.d(TAG, "MainActivity onDetachedFromWindow");

		IndeterminateProgressDialog.getInstance().dismiss();

		super.onDetachedFromWindow();
	}

	@Override
	protected void onDestroy() {
		// !! PaintStroke is set static in the tools !! don't remove these lines
		PaintroidApplication.currentTool.changePaintStrokeCap(Cap.ROUND);
		PaintroidApplication.currentTool.changePaintStrokeWidth(25);
		PaintroidApplication.currentTool = null;

		NavigationDrawerMenuActivity.savedPictureUri = null;

		onDestroyView();
		super.onDestroy();
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.nav_back_to_pocket_code:
				showSecurityQuestionBeforeExit();
				break;
			case R.id.nav_export:
				saveCopy = true;
				SaveTask saveExportTask = new SaveTask(this);
				saveExportTask.execute();
				break;
			case R.id.nav_save_image:
				SaveTask saveTask = new SaveTask(this);
				saveTask.execute();
				break;
			case R.id.nav_save_duplicate:
				saveCopy = true;
				SaveTask saveCopyTask = new SaveTask(this);
				saveCopyTask.execute();
				break;
			case R.id.nav_open_image:
				onLoadImage();
				break;
			case R.id.nav_new_image:
				newImage();
				break;
			case R.id.nav_fullscreen_mode:
				setFullScreen(true);
				break;
			case R.id.nav_exit_fullscreen_mode:
				setFullScreen(false);
				break;
			case R.id.nav_tos:
				DialogTermsOfUseAndService termsOfUseAndService = new DialogTermsOfUseAndService();
				termsOfUseAndService.show(getSupportFragmentManager(), "termsofuseandservicedialogfragment");
				break;
			case R.id.nav_help:
				Intent intent = new Intent(this, WelcomeActivity.class);
				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivity(intent);
				break;
			case R.id.nav_about:
				DialogAbout about = new DialogAbout();
				about.show(getSupportFragmentManager(), "aboutdialogfragment");
				break;
			case R.id.nav_lang:
				Intent language = new Intent(this, MultilingualActivity.class);
				startActivityForResult(language, REQUEST_CODE_LANGUAGE);
				break;
		}

		drawerLayout.closeDrawers();
		return true;
	}

	@Override
	public void onBackPressed() {
		if (isFullScreen) {
			setFullScreen(false);
		} else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
			drawerLayout.closeDrawer(Gravity.START);
		} else if (layerSideNav.isShown()) {
			drawerLayout.closeDrawer(Gravity.END);
		} else if (PaintroidApplication.currentTool.getToolOptionsAreShown()) {
			PaintroidApplication.currentTool.toggleShowToolOptions();
		} else if (PaintroidApplication.currentTool.getToolType() == ToolType.BRUSH) {
			showSecurityQuestionBeforeExit();
		} else {
			switchTool(ToolType.BRUSH);
		}
	}

	@Override
	public void onActivityResult(@RequestCode int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			Log.d(TAG, "onActivityResult: result not ok, most likely a dialog hast been canceled");
			return;
		}

		switch (requestCode) {
			case REQUEST_CODE_IMPORTPNG:
				Uri selectedGalleryImageUri = data.getData();
				Tool tool = ToolFactory.createTool(this, ToolType.IMPORTPNG);
				switchTool(tool);

				loadBitmapFromUriAndRun(selectedGalleryImageUri, new RunnableWithBitmap() {
					@Override
					public void run(Bitmap bitmap) {
						if (PaintroidApplication.currentTool instanceof ImportTool) {
							((ImportTool) PaintroidApplication.currentTool).setBitmapFromFile(bitmap);
						} else {
							Log.e(TAG, "importPngToFloatingBox: Current tool is no ImportTool as required");
						}
					}
				});
				break;

			case REQUEST_CODE_FINISH:
				finish();
				break;

			case REQUEST_CODE_LANGUAGE:
				onConfigurationChanged(getResources().getConfiguration());
				break;

			case REQUEST_CODE_LOAD_PICTURE:
			case REQUEST_CODE_TAKE_PICTURE:
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public boolean isKeyboardShown() {
		return isKeyboardShown;
	}

	public void hideKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
	}

	public synchronized void switchTool(ToolType changeToToolType) {
		switch (changeToToolType) {
			case IMPORTPNG:
				importPng();
				break;
			default:
				Tool tool = ToolFactory.createTool(this, changeToToolType);
				switchTool(tool);
				break;
		}
	}

	private void importPng() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivityForResult(intent, REQUEST_CODE_IMPORTPNG);
	}

	public synchronized void switchTool(Tool tool) {
		if (tool == null) {
			return;
		}

		Tool currentTool = PaintroidApplication.currentTool;
		Paint tempPaint = currentTool.getDrawPaint();

		currentTool.leaveTool();
		if (currentTool.getToolType() == tool.getToolType()) {
			currentTool.onSaveInstanceState(toolBundle);
			PaintroidApplication.currentTool = tool;
			bottomBar.setTool(tool);
			tool.onRestoreInstanceState(toolBundle);
		} else {
			toolBundle = new Bundle();
			bottomBar.setTool(tool);
			PaintroidApplication.currentTool = tool;
		}
		tool.startTool();
		tool.setDrawPaint(tempPaint);
	}

	private void showSecurityQuestionBeforeExit() {
		if (!imageHasBeenModified() || imageHasBeenSaved()) {
			finish();
		} else {
			AlertDialog.Builder builder = new CustomAlertDialogBuilder(this);
			if (openedFromCatroid) {
				builder.setTitle(R.string.closing_catroid_security_question_title);
				builder.setMessage(R.string.closing_security_question);
				builder.setPositiveButton(R.string.save_button_text, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						exitToCatroid();
					}
				});
				builder.setNegativeButton(R.string.discard_button_text, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				});
			} else {
				builder.setTitle(R.string.closing_security_question_title);
				builder.setMessage(R.string.closing_security_question);
				builder.setPositiveButton(R.string.save_button_text, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						saveFile();
						finish();
					}
				});
				builder.setNegativeButton(R.string.discard_button_text, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				});
			}
			builder.setCancelable(true);
			builder.show();
		}
	}

	private void exitToCatroid() {
		String pictureFileName = TEMP_PICTURE_NAME;

		if (catroidPicturePath != null) {
			pictureFileName = catroidPicturePath;
		} else {
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				String catroidPictureName = extras.getString(Constants.PAINTROID_PICTURE_NAME);
				if (catroidPictureName != null && catroidPictureName.length() > 0) {
					pictureFileName = catroidPictureName;
				}
			}
			pictureFileName = FileIO.createNewEmptyPictureFile(pictureFileName).getAbsolutePath();
		}

		Intent resultIntent = new Intent();

		LayerModel layerModel = PaintroidApplication.layerModel;
		if (FileIO.saveBitmap(this, layerModel.getBitmapToSave(), pictureFileName, saveCopy)) {
			Bundle bundle = new Bundle();
			bundle.putString(Constants.PAINTROID_PICTURE_PATH, pictureFileName);
			resultIntent.putExtras(bundle);
			setResult(RESULT_OK, resultIntent);
		} else {
			setResult(RESULT_CANCELED, resultIntent);
		}
		finish();
	}

	private void setFullScreen(boolean isFullScreen) {
		PaintroidApplication.perspective.setFullscreen(isFullScreen);

		if (isFullScreen) {
			PaintroidApplication.currentTool.hide();

			ActionBar supportActionBar = getSupportActionBar();
			if (supportActionBar != null) {
				supportActionBar.hide();
			}

			findViewById(R.id.layout_top_bar).setVisibility(View.GONE);
			findViewById(R.id.main_bottom_bar).setVisibility(View.GONE);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

			navigationView.getMenu().findItem(R.id.nav_exit_fullscreen_mode).setVisible(true);
			navigationView.getMenu().findItem(R.id.nav_fullscreen_mode).setVisible(false);
		} else {
			ActionBar supportActionBar = getSupportActionBar();
			if (supportActionBar != null) {
				supportActionBar.show();
			}

			findViewById(R.id.layout_top_bar).setVisibility(View.VISIBLE);
			findViewById(R.id.main_bottom_bar).setVisibility(View.VISIBLE);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

			navigationView.getMenu().findItem(R.id.nav_exit_fullscreen_mode).setVisible(false);
			navigationView.getMenu().findItem(R.id.nav_fullscreen_mode).setVisible(true);
		}

		this.isFullScreen = isFullScreen;
	}

	private void initKeyboardIsShownListener() {
		final View activityRootView = findViewById(R.id.main_layout);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
				isKeyboardShown = heightDiff > 300;
			}
		});
	}

	private void initLocaleConfiguration() {
		MultilingualActivity.setToChosenLanguage(this);
	}
}
