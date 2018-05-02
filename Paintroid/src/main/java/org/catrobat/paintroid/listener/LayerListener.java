/**
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2015 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.listener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.command.implementation.AddLayerCommand;
import org.catrobat.paintroid.command.implementation.CommandManager;
import org.catrobat.paintroid.command.implementation.RemoveLayerCommand;
import org.catrobat.paintroid.command.implementation.SelectLayerCommand;
import org.catrobat.paintroid.model.LayerModel;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.ui.ToastFactory;
import org.catrobat.paintroid.ui.button.LayersAdapter;
import org.catrobat.paintroid.ui.dragndrop.BrickDragAndDropLayerMenu;
import org.catrobat.paintroid.ui.dragndrop.MyDragShadowBuilder;
import org.catrobat.paintroid.ui.dragndrop.OnDragListener;

public final class LayerListener implements AdapterView.OnItemClickListener, CommandManager.CommandListener {

	private static final String TAG = LayerListener.class.getSimpleName();
	private static final String NOT_INITIALIZED_ERROR_MESSAGE = "LayerListener has not been initialized. Call init() first!";
	private static final int ANIMATION_TIME = 300;
	private static final int LAYER_UNDO_LIMIT = 10;

	private static LayerListener instance;

	private Activity activity;
	private NavigationView navigationView;
	private BrickDragAndDropLayerMenu brickLayer;
	private ImageButton addButton;
	private ImageButton delButton;

	@NonNull
	private LayersAdapter layersAdapter;

	@NonNull
	private LayerModel layerModel;

	private LayerListener(Activity activity, NavigationView view, Bitmap firstLayer) {
		this.layerModel = new LayerModel(firstLayer);
		this.layersAdapter = new LayersAdapter(layerModel);

		setupLayerListener(activity, view);
		refreshView();
	}

	public static LayerListener getInstance() {
		if (instance == null) {
			throw new IllegalStateException(NOT_INITIALIZED_ERROR_MESSAGE);
		}
		return instance;
	}

	public static void init(Activity activity, NavigationView view, Bitmap firstLayer) {
		instance = new LayerListener(activity, view, firstLayer);
	}

	public static void initAfterOrientationChange(Activity activity, NavigationView view) {
		getInstance().setupLayerListener(activity, view);
	}

	private void setupLayerListener(Activity activity, NavigationView view) {
		this.activity = activity;
		this.navigationView = view;

		final ListView listView = (ListView) view.findViewById(R.id.nav_layer_list);
		listView.setAdapter(layersAdapter);

		brickLayer = new BrickDragAndDropLayerMenu(listView);
		OnDragListener dragListener = new OnDragListener(brickLayer);

		listView.setOnItemClickListener(this);
		listView.setOnDragListener(dragListener);
		listView.setLongClickable(true);

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView v, View arg1, int pos, long id) {
				listView.getChildAt(pos).setVisibility(View.INVISIBLE);
				/*
				Layer layer = layerModel.getLayer(pos);
				if (!layer.getSelected()) {
					setCurrentLayer(layer);
				}
				*/
				brickLayer.setDragStartPosition(pos);

				MyDragShadowBuilder myShadow = new MyDragShadowBuilder(listView.getChildAt(pos));
				myShadow.setDragPos(pos);

				v.startDrag(null,  // the data to be dragged (dragData)
						myShadow,  // the drag shadow builder
						null,      // no need to use local data
						0          // flags (not currently used, set to 0)
				);

				return true;
			}
		});

		addButton = (ImageButton) view.findViewById(R.id.layer_side_nav_button_add);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createLayer();
			}
		});
		delButton = (ImageButton) view.findViewById(R.id.layer_side_nav_button_delete);
		delButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				View layerItem = listView.getChildAt(layerModel.getPosition(getCurrentLayer().getLayerID()));
				Animation translateAnimation = new TranslateAnimation(0f, layerItem.getWidth(), 0f, 0f);
				translateAnimation.setDuration(ANIMATION_TIME);
				translateAnimation.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						deleteLayer();
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}
				});

				if (layerModel.getLayerCount() > 1) {
					layerItem.startAnimation(translateAnimation);
				}
			}
		});
		updateButtonResource();
		refreshView();
	}

	@NonNull
	public LayerModel getLayerModel() {
		return layerModel;
	}

	public Layer getCurrentLayer() {
		return layerModel.getCurrentLayer();
	}

	public void refreshView() {
		ListView listView = (ListView) navigationView.findViewById(R.id.nav_layer_list);
		if (listView != null) {
			layersAdapter.notifyDataSetChanged();
			listView.setAdapter(layersAdapter);
		} else {
			Log.d(TAG, "LAYERGRIDVIEW NOT INITIALIZED");
		}
		refreshDrawingSurface();
	}

	public void updateButtonResource() {
		addButton.setEnabled(layerModel.getLayerCount() < LayerModel.MAX_LAYER);
		delButton.setEnabled(layerModel.getLayerCount() > 1);
	}

	public void createLayer() {
		PaintroidApplication.commandManager.addCommand(new AddLayerCommand());
	}

	public void deleteLayer() {
		PaintroidApplication.commandManager.addCommand(new RemoveLayerCommand());
		ToastFactory.makeText(activity, R.string.layer_invisible, Toast.LENGTH_LONG).show();
	}

	public void moveLayer(int layerToMovePosition, int targetPosition) {
		// TODO: uncomment when BrickDragAndDropLayerMenu / ListView has a temporary list for modification
		// PaintroidApplication.commandManager.addCommand(new MoveLayerCommand(layerToMovePosition, targetPosition));
	}

	public void mergeLayer(int firstLayer, int secondLayer) {
		int firstLayerId = layerModel.getLayer(firstLayer).getLayerID();
		int secondLayerId = layerModel.getLayer(secondLayer).getLayerID();

		if (firstLayerId != secondLayerId) {
			// TODO: uncomment when BrickDragAndDropLayerMenu / ListView has a temporary list for modification
			/*
			PaintroidApplication.commandManager.addCommand(new MergeLayerCommand(firstLayerId, secondLayerId));
			ToastFactory.makeText(activity, R.string.layer_merged, Toast.LENGTH_LONG).show();
			*/
		}
	}

	public Bitmap getBitmapOfAllLayersToSave() {
		return layerModel.getBitmapToSave();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Layer layer = layerModel.getLayer(position);
		if (layerModel.getCurrentLayer().getLayerID() != layer.getLayerID()) {
			PaintroidApplication.commandManager.addCommand(new SelectLayerCommand(layer.getLayerID()));
		}
	}

	@Override
	public void commandExecuted() {
		refreshView();
	}

	private void refreshDrawingSurface() {
		PaintroidApplication.drawingSurface.refreshDrawingSurface();
	}
}
