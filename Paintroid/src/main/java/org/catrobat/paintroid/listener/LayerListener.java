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
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.catrobat.paintroid.MainActivity;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.command.CommandManager;
import org.catrobat.paintroid.command.UndoRedoManager;
import org.catrobat.paintroid.command.implementation.LayerCommand;
import org.catrobat.paintroid.eventlistener.OnActiveLayerChangedListener;
import org.catrobat.paintroid.eventlistener.OnRefreshLayerDialogListener;
import org.catrobat.paintroid.model.LayerModel;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.tools.Tool;
import org.catrobat.paintroid.ui.ToastFactory;
import org.catrobat.paintroid.ui.button.LayersAdapter;
import org.catrobat.paintroid.ui.dragndrop.BrickDragAndDropLayerMenu;
import org.catrobat.paintroid.ui.dragndrop.MyDragShadowBuilder;
import org.catrobat.paintroid.ui.dragndrop.OnDragListener;

import java.util.ArrayList;

public final class LayerListener implements OnRefreshLayerDialogListener, OnActiveLayerChangedListener, AdapterView.OnItemClickListener {
	private static final String TAG = LayerListener.class.getSimpleName();
	private static final String NOT_INITIALIZED_ERROR_MESSAGE = "LayerListener has not been initialized. Call init() first!";
	private static final int ANIMATION_TIME = 300;
	private static final int LAYER_UNDO_LIMIT = 10;
	private static LayerListener instance;
	private Context context;
	private NavigationView navigationView;
	private BrickDragAndDropLayerMenu brickLayer;
	private ImageButton addButton;
	private ImageButton delButton;

	@Nullable
	private LayerModel layerModel;

	private LayerListener(Context context, NavigationView view, Bitmap firstLayer) {
		setupLayerListener(view, context, firstLayer, false);
	}

	public static LayerListener getInstance() {
		if (instance == null) {
			throw new IllegalStateException(NOT_INITIALIZED_ERROR_MESSAGE);
		}
		return instance;
	}

	public static void init(MainActivity mainActivity, NavigationView view, Bitmap firstLayer, boolean orientationChanged) {
		if (!orientationChanged) {
			instance = new LayerListener(mainActivity, view, firstLayer);
		} else {
			getInstance().setupLayerListener(view, mainActivity, null, true);
		}
	}

	public void setupLayerListener(NavigationView view, Context context, Bitmap firstLayer, boolean orientationChanged) {
		navigationView = view;
		this.context = context;

		if (!orientationChanged) {
			layerModel = new LayerModel(firstLayer);
			initCurrentLayer();
		}

		final ListView listView = (ListView) view.findViewById(R.id.nav_layer_list);

		brickLayer = new BrickDragAndDropLayerMenu(listView);
		OnDragListener dragListener = new OnDragListener(brickLayer);

		if (!orientationChanged) {
			listView.setAdapter(layerModel.getLayersAdapter());
		}

		listView.setOnItemClickListener(this);
		listView.setOnDragListener(dragListener);
		listView.setLongClickable(true);

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView v, View arg1, int pos, long id) {

				listView.getChildAt(pos).setVisibility(View.INVISIBLE);
				Layer layer = layerModel.getLayersAdapter().getLayer(pos);
				if (!layer.getSelected()) {
					setCurrentLayer(layer);
				}
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
				View layerItem = listView.getChildAt(layerModel.getLayersAdapter().getPosition(getCurrentLayer().getLayerID()));
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

				if (layerModel.getLayersAdapter().getCount() > 1) {
					layerItem.startAnimation(translateAnimation);
				}
			}
		});
		updateButtonResource();
		refreshView();
	}

	private void initCurrentLayer() {
		if (layerModel == null) {
			Log.d(TAG, "ERROR, initCurrentLayer -> layerAdapter == null");
			layerModel = new LayerModel(PaintroidApplication.drawingSurface.getBitmapCopy());
		}
		layerModel.setCurrentLayer(layerModel.getLayersAdapter().getLayer(0)); // todo
		if (layerModel.getCurrentLayer() != null) {
			selectLayer(layerModel.getCurrentLayer());
			return;
		}
		Log.d(TAG, "CURRENT LAYER NOT INITIALIZED");
	}

	// todo remove this method -> LayerModel.getLayersAdapter()
	public LayersAdapter getAdapter() {
		return layerModel.getLayersAdapter();
	}

	public void selectLayer(Layer toSelect) {
		if (layerModel.getCurrentLayer() != null) {
			layerModel.getCurrentLayer().setSelected(false);
			layerModel.getCurrentLayer().setImage(PaintroidApplication.drawingSurface.getBitmapCopy());
		}
		layerModel.setCurrentLayer(toSelect);
		layerModel.getCurrentLayer().setSelected(true); // todo 🙄

		PaintroidApplication.drawingSurface.setLock(layerModel.getCurrentLayer().getLocked());
		PaintroidApplication.drawingSurface.setVisible(layerModel.getCurrentLayer().getVisible());
		PaintroidApplication.drawingSurface.setBitmap(layerModel.getCurrentLayer().getImage());
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				refreshView();
			}
		});
	}

	public Layer getCurrentLayer() {
		if (layerModel.getCurrentLayer() == null) {
			initCurrentLayer();
		}
		return layerModel.getCurrentLayer();
	}

	public void setCurrentLayer(Layer toSelect) {
		if (layerModel.getCurrentLayer() != null) {
			layerModel.getCurrentLayer().setSelected(false);
			layerModel.getCurrentLayer().setImage(PaintroidApplication.drawingSurface.getBitmapCopy());
		}
		layerModel.setCurrentLayer(toSelect);
		layerModel.getCurrentLayer().setSelected(true); // todo 🙄

		PaintroidApplication.drawingSurface.setLock(layerModel.getCurrentLayer().getLocked());
		PaintroidApplication.drawingSurface.setVisible(layerModel.getCurrentLayer().getVisible());
		PaintroidApplication.drawingSurface.setBitmap(layerModel.getCurrentLayer().getImage());
	}

	public void refreshView() {
		if (layerModel != null) {
			ListView listView = (ListView) navigationView.findViewById(R.id.nav_layer_list);
			if (listView != null) {
				layerModel.getLayersAdapter().notifyDataSetChanged();
				listView.setAdapter(layerModel.getLayersAdapter());
			} else {
				Log.d(TAG, "LAYERGRIDVIEW NOT INITIALIZED");
			}
		} else {
			Log.d(TAG, "LAYERBUTTONADAPTER NOT INITIALIZED");
		}
		refreshDrawingSurface();
	}

	public void updateButtonResource() {
		addButton.setEnabled(layerModel.getLayersAdapter().getCount() < layerModel.getLayersAdapter().MAX_LAYER);
		delButton.setEnabled(layerModel.getLayersAdapter().getCount() > 1);
	}

	public void createLayer() {
		final CommandManager commandManager = PaintroidApplication.commandManager;
		if (layerModel.getLayersAdapter().getLayerCounter() > LAYER_UNDO_LIMIT) {
			commandManager.deleteCommandFirstDeletedLayer();
		}

		boolean success = layerModel.getLayersAdapter().addLayer();
		if (success) {
			Layer layer = layerModel.getLayersAdapter().getLayer(0);
			selectLayer(layer);
			commandManager.commitAddLayerCommand(new LayerCommand(layer));
			UndoRedoManager.getInstance().update();
		} else {
			ToastFactory.makeText(context, R.string.layer_too_many_layers,
					Toast.LENGTH_LONG).show();
		}
		updateButtonResource();
		PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
		refreshDrawingSurface();
	}

	public void deleteLayer() {

		int layerCount = layerModel.getLayersAdapter().getCount();
		if (layerCount == 1 || layerModel.getCurrentLayer() == null) {
			return;
		}

		int currentPosition = layerModel.getLayersAdapter().getPosition(layerModel.getCurrentLayer().getLayerID());
		int newPosition = currentPosition;
		if (currentPosition == layerCount - 1 && layerCount > 1) {
			newPosition = currentPosition - 1;
		}

		PaintroidApplication.commandManager.commitRemoveLayerCommand(new LayerCommand(layerModel.getCurrentLayer()));
		layerModel.getLayersAdapter().removeLayer(layerModel.getCurrentLayer());
		selectLayer(layerModel.getLayersAdapter().getLayer(newPosition));

		if (layerModel.getLayersAdapter().checkAllLayerVisible()) {
			ToastFactory.makeText(context, R.string.layer_invisible,
					Toast.LENGTH_LONG).show();
		}

		updateButtonResource();
		refreshView();
		PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
		refreshDrawingSurface();
	}

	public void moveLayer(int layerToMove, int targetPosition) {
		layerModel.getLayersAdapter().swapLayer(layerToMove, targetPosition);
		refreshDrawingSurface();
	}

	public void mergeLayer(int firstLayer, int secondLayer) {
		if (layerModel.getLayersAdapter().getLayer(firstLayer).getLayerID() != layerModel.getLayersAdapter().getLayer(secondLayer).getLayerID()) {
			ArrayList<Integer> layerToMergeIds = new ArrayList<>();
			layerToMergeIds.add(layerModel.getLayersAdapter().getLayer(firstLayer).getLayerID());
			layerToMergeIds.add(layerModel.getLayersAdapter().getLayer(secondLayer).getLayerID());

			Layer layer = layerModel.getLayersAdapter().mergeLayer(layerModel.getLayersAdapter().getLayer(firstLayer), layerModel.getLayersAdapter().getLayer(secondLayer));

			selectLayer(layer);
			updateButtonResource();
			refreshView();

			PaintroidApplication.commandManager.commitMergeLayerCommand(new LayerCommand(getCurrentLayer(), layerToMergeIds));
			ToastFactory.makeText(context, R.string.layer_merged,
					Toast.LENGTH_LONG).show();

			PaintroidApplication.currentTool.resetInternalState(Tool.StateChange.RESET_INTERNAL_STATE);
			refreshDrawingSurface();
		}
	}

	public void resetLayer() {
		Layer layer = layerModel.getLayersAdapter().clearLayer();
		selectLayer(layer);
		PaintroidApplication.commandManager.commitAddLayerCommand(new LayerCommand(layer));
		updateButtonResource();
		refreshView();
	}

	public Bitmap getBitmapOfAllLayersToSave() {
		return layerModel.getLayersAdapter().getBitmapToSave();
	}

	@Override
	public void onActiveLayerChanged(Layer layer) {
		Log.e(TAG, "onActiveLayerChanged");
		if (layerModel.getCurrentLayer().getLayerID() != layer.getLayerID()) {
			selectLayer(layer);
		}
	}

	@Override
	public void onLayerDialogRefreshView() {
		Log.d(TAG, "onLayerDialogRefreshView");

		refreshView();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectLayer(layerModel.getLayersAdapter().getLayer(position));
		UndoRedoManager.getInstance().update();
	}

	private void refreshDrawingSurface() {
		PaintroidApplication.drawingSurface.refreshDrawingSurface();
	}
}
