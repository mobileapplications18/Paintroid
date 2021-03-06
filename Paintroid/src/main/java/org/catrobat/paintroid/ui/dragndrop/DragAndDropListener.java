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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.listener.LayerActionListener;

public class DragAndDropListener {

	private static final int ANIMATION_TIME = 500;

	private final ListView view;
	private final LayerActionListener layerActionListener;

	private int listViewHeight;
	private int heightOneLayer;
	private int numLayer;
	private int currentDragLayerPos;
	private int startDragLayerPos;
	private int positionWentOutsideView;
	private boolean moveAlreadyAnimated;
	private boolean outsideView;
	private Bitmap blueBitmap;
	private boolean dragEnded;
	private boolean animationEnded;

	public DragAndDropListener(@NonNull ListView view, @NonNull LayerActionListener layerActionListener) {
		this.view = view;
		this.layerActionListener = layerActionListener;

		moveAlreadyAnimated = false;
		outsideView = false;
		positionWentOutsideView = -1;
		dragEnded = false;
		animationEnded = true;
	}

	public void setDragStartPosition(int startLayerPos) {
		startDragLayerPos = startLayerPos;
		currentDragLayerPos = startLayerPos;
		dragEnded = false;
		animationEnded = true;
	}

	public void goOutsideView(boolean isOutside, float x, float y) {
		outsideView = isOutside;
		View parent = (View) view.getParent();
		int heightDifference = parent.getHeight() - view.getHeight();
		y -= heightDifference;

		if (isOutside) {
			positionWentOutsideView = currentDragLayerPos;
		} else {
			moveAlreadyAnimated = true;

			if (positionWentOutsideView != -1) {

				int positionReenterView = 0;

				if (y >= 0 && y <= listViewHeight) {
					for (int i = 0; i <= listViewHeight; i += heightOneLayer) {
						if (y > i && y < (i + heightOneLayer)) {
							break;
						} else {
							positionReenterView++;
						}
					}
				}

				if (positionReenterView >= numLayer) {
					positionReenterView = numLayer - 1;
				}

				if (positionReenterView - positionWentOutsideView > 0) { //went down
					animateMoveLayerRecursive(positionWentOutsideView, positionReenterView, false);
				} else if (positionReenterView - positionWentOutsideView < 0) {
					animateMoveLayerRecursive(positionReenterView, positionWentOutsideView, true);
				}

				currentDragLayerPos = positionReenterView;
				positionWentOutsideView = -1;
			}
			moveAlreadyAnimated = false;
		}
	}

	public void setupProperties() {
		listViewHeight = view.getHeight();
		numLayer = view.getChildCount();
		if (numLayer > 0) {
			heightOneLayer = view.getChildAt(0).getHeight();
		}

		Bitmap buffer = PaintroidApplication.layerModel.getLayer(0).getImage();
		blueBitmap = Bitmap.createBitmap(buffer.getWidth(), buffer.getHeight(), buffer.getConfig());
		blueBitmap.eraseColor(view.getContext().getResources().getColor(R.color.custom_background_color));
	}

	public void showOptionFromCurrentPosition(float x, float y) {

		int numLayerDropPosition = 0;

		if (y >= 0 && y <= listViewHeight) {
			for (int i = 0; i <= listViewHeight; i += heightOneLayer) {
				if (y > i && y < (i + heightOneLayer)) {
					break;
				} else {
					numLayerDropPosition++;
				}
			}

			if (numLayerDropPosition >= numLayer) {
				numLayerDropPosition = numLayer - 1;
			}

			if (numLayerDropPosition != currentDragLayerPos) {

				//lower third of the Layer
				if (y > (((numLayerDropPosition + 1) * heightOneLayer) - (heightOneLayer / 3))) {
					if (view.getChildAt(numLayerDropPosition) != null) {
						view.getChildAt(numLayerDropPosition).setBackgroundColor(Color.TRANSPARENT);
					}

					if ((currentDragLayerPos - numLayerDropPosition) != 1) {
						if (currentDragLayerPos < numLayerDropPosition && !moveAlreadyAnimated) {
							animateMoveLayerRecursive(currentDragLayerPos, numLayerDropPosition, false);
							currentDragLayerPos = numLayerDropPosition;
						}
						if (currentDragLayerPos > numLayerDropPosition && !moveAlreadyAnimated) {
							animateMoveLayerRecursive(currentDragLayerPos, numLayerDropPosition, true);
							currentDragLayerPos = numLayerDropPosition + 1;
						}
					}
				}

				//upper third of the Layer
				if (y < ((numLayerDropPosition * heightOneLayer) + (heightOneLayer / 3))) {
					if (view.getChildAt(numLayerDropPosition) != null) {
						view.getChildAt(numLayerDropPosition).setBackgroundColor(Color.TRANSPARENT);
					}

					if ((numLayerDropPosition - currentDragLayerPos) != 1) {

						if (currentDragLayerPos > numLayerDropPosition && !moveAlreadyAnimated) {
							animateMoveLayerRecursive(numLayerDropPosition, currentDragLayerPos, true);
							currentDragLayerPos = numLayerDropPosition;
						}
						if (currentDragLayerPos < numLayerDropPosition && !moveAlreadyAnimated) {
							animateMoveLayerRecursive(currentDragLayerPos, numLayerDropPosition, false);
							currentDragLayerPos = numLayerDropPosition - 1;
						}
					}
				}

				if (y < (((numLayerDropPosition + 1) * heightOneLayer) - (heightOneLayer / 3))
						&& y > ((numLayerDropPosition * heightOneLayer) + (heightOneLayer / 3))) {
					if (view.getChildAt(numLayerDropPosition) != null
							&& view.getChildAt(numLayerDropPosition).getDrawingCacheBackgroundColor() != Color.YELLOW) {

						view.getChildAt(numLayerDropPosition).setBackgroundColor(Color.YELLOW);
					}
					moveAlreadyAnimated = false;
				}
			}
		}

		if (view.getChildAt(currentDragLayerPos) != null) {
			view.getChildAt(currentDragLayerPos).setVisibility(View.INVISIBLE);
		}
	}

	public void moveOrMerge(View v, float x, float y) {
		int numLayerDropPosition = 0;

		if (y >= 0 && y <= listViewHeight) {
			for (int i = 0; i <= listViewHeight; i += heightOneLayer) {
				if (y > i && y < (i + heightOneLayer)) {
					break;
				} else {
					numLayerDropPosition++;
				}
			}

			if (numLayerDropPosition != currentDragLayerPos
					&& y < (((numLayerDropPosition + 1) * heightOneLayer) - (heightOneLayer / 3))
					&& y > ((numLayerDropPosition * heightOneLayer) + (heightOneLayer / 3))) {

				layerActionListener.mergeLayer(startDragLayerPos, numLayerDropPosition);
			} else {
				layerActionListener.moveLayer(startDragLayerPos, currentDragLayerPos);
			}
		}
	}

	public void dragEnded() {
		if (outsideView && animationEnded) {
			layerActionListener.moveLayer(startDragLayerPos, currentDragLayerPos);
		}

		positionWentOutsideView = -1;
		dragEnded = true;
	}

	public void resetMoveAlreadyAnimated() {
		moveAlreadyAnimated = false;
	}

	private void animateMoveLayerRecursive(final int currentLayerPos, final int movedLayerPos, final boolean up) {

		if (view.getChildAt(movedLayerPos) == null) {
			return;
		}
		moveAlreadyAnimated = true;

		Animation translateAnimation;
		if (up) {
			translateAnimation = new TranslateAnimation(0f, 0f, 0f, heightOneLayer);
		} else {
			translateAnimation = new TranslateAnimation(0f, 0f, 0f, heightOneLayer * (-1));
		}
		translateAnimation.setDuration(ANIMATION_TIME);

		translateAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				animationEnded = false;

				if ((movedLayerPos - currentLayerPos) > 1) {
					if (up) {
						animateMoveLayerRecursive(currentLayerPos, movedLayerPos - 1, up);
					} else {
						animateMoveLayerRecursive(currentLayerPos + 1, movedLayerPos, up);
					}
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (up) {
					layerActionListener.moveLayerTemporarily(movedLayerPos - 1, movedLayerPos);
				} else {
					layerActionListener.moveLayerTemporarily(currentLayerPos, currentLayerPos + 1);
				}

				animationEnded = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		if (up) {
			if (view.getChildAt(movedLayerPos - 1) != null) {
				view.getChildAt(movedLayerPos - 1).startAnimation(translateAnimation);
			}
		} else {
			if (view.getChildAt(currentLayerPos + 1) != null) {
				view.getChildAt(currentLayerPos + 1).startAnimation(translateAnimation);
			}
		}
	}
}
