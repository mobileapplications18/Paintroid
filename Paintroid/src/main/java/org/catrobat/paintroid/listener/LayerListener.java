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

package org.catrobat.paintroid.listener;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.command.implementation.*;
import org.catrobat.paintroid.model.BitmapFactory;
import org.catrobat.paintroid.model.Layer;
import org.catrobat.paintroid.model.LayerModel;
import org.catrobat.paintroid.ui.ToastFactory;
import org.catrobat.paintroid.ui.button.LayersAdapter;
import org.catrobat.paintroid.ui.dragndrop.DragAndDropListener;
import org.catrobat.paintroid.ui.dragndrop.MyDragShadowBuilder;
import org.catrobat.paintroid.ui.dragndrop.OnDragListener;

public final class LayerListener implements AdapterView.OnItemClickListener, LayerActionListener, CommandManager.CommandListener {

	private static final int ANIMATION_TIME = 300;

	private final Activity activity;
	private final ImageButton addButton;
	private final ImageButton delButton;

	@NonNull
	private LayersAdapter layersAdapter;

	public LayerListener(Activity activity, NavigationView navigationView) {
		this.activity = activity;
		this.layersAdapter = new LayersAdapter(PaintroidApplication.layerModel);

		final ListView listView = (ListView) navigationView.findViewById(R.id.nav_layer_list);
		listView.setAdapter(layersAdapter);

		final DragAndDropListener dragAndDropListener = new DragAndDropListener(listView, this);
		final OnDragListener dragListener = new OnDragListener(dragAndDropListener);

		listView.setOnItemClickListener(this);
		listView.setOnDragListener(dragListener);
		listView.setLongClickable(true);
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView adapterView, View view, int position, long id) {
				listView.getChildAt(position).setVisibility(View.INVISIBLE);

				dragAndDropListener.setDragStartPosition(position);

				Layer layer = (Layer) layersAdapter.getItem(position);
				MyDragShadowBuilder shadowBuilder = new MyDragShadowBuilder(adapterView.getChildAt(position), layer);
				adapterView.startDrag(null, shadowBuilder, null, 0);

				return true;
			}
		});

		addButton = (ImageButton) navigationView.findViewById(R.id.layer_side_nav_button_add);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createLayer();
			}
		});
		delButton = (ImageButton) navigationView.findViewById(R.id.layer_side_nav_button_delete);
		delButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LayerModel layerModel = getLayerModel();

				View layerItem = listView.getChildAt(layersAdapter.getPosition(layerModel.getCurrentPosition()));
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
		refreshView();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectLayer(position);
	}

	@NonNull
	public LayerModel getLayerModel() {
		return PaintroidApplication.layerModel;
	}

	@Override
	public void selectLayer(int position) {
		int modelPosition = layersAdapter.getPosition(position);
		if (getLayerModel().getCurrentPosition() != modelPosition) {
			PaintroidApplication.commandManager.addCommand(new SelectLayerCommand(modelPosition));
		}
	}

	@Override
	public void createLayer() {
		PaintroidApplication.commandManager.addCommand(new AddLayerCommand(new BitmapFactory()));
	}

	@Override
	public void deleteLayer() {
		PaintroidApplication.commandManager.addCommand(new RemoveLayerCommand());
	}

	@Override
	public void moveLayer(int layerToMovePosition, int targetPosition) {
		int fromModelPosition = layersAdapter.getPosition(layerToMovePosition);
		int toModelPosition = layersAdapter.getPosition(targetPosition);

		PaintroidApplication.commandManager.addCommand(new MoveLayerCommand(fromModelPosition, toModelPosition));
	}

	@Override
	public void mergeLayer(int firstLayerPosition, int secondLayerPosition) {
		if (firstLayerPosition != secondLayerPosition) {
			int firstLayerModelPosition = layersAdapter.getPosition(firstLayerPosition);
			int secondLayerModelPosition = layersAdapter.getPosition(secondLayerPosition);

			PaintroidApplication.commandManager.addCommand(new MergeLayerCommand(firstLayerModelPosition, secondLayerModelPosition, new BitmapFactory()));
			ToastFactory.makeText(activity, R.string.layer_merged, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void moveLayerTemporarily(int layerToMovePosition, int targetPosition) {
		int fromModelPosition = layersAdapter.getPosition(layerToMovePosition);
		int toModelPosition = layersAdapter.getPosition(targetPosition);

		layersAdapter.swapLayer(fromModelPosition, toModelPosition);
	}

	@Override
	public void commandExecuted() {
		activity.runOnUiThread(
				new Runnable() {
					@Override
					public void run() {
						refreshView();
					}
				}
		);
	}

	private void refreshView() {
		LayerModel layerModel = getLayerModel();

		layersAdapter.updateLayers(layerModel);
		addButton.setEnabled(layerModel.getLayerCount() < LayerModel.MAX_LAYER);
		delButton.setEnabled(layerModel.getLayerCount() > 1);

		PaintroidApplication.drawingSurface.refreshDrawingSurface();
	}
}
