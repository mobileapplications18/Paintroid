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

import android.util.Log;
import android.view.DragEvent;
import android.view.View;

public class OnDragListener implements View.OnDragListener {

	private DragAndDropListener dragAndDropListener;

	public OnDragListener(DragAndDropListener dragAndDropListener) {
		this.dragAndDropListener = dragAndDropListener;
	}

	public boolean onDrag(View v, DragEvent event) {
		final int action = event.getAction();
		switch (action) {

			case DragEvent.ACTION_DRAG_STARTED:
				dragAndDropListener.resetMoveAlreadyAnimated();
				return true;
			case DragEvent.ACTION_DRAG_ENTERED:
				dragAndDropListener.setupProperties();
				dragAndDropListener.goOutsideView(false, event.getX(), event.getY());
				return true;
			case DragEvent.ACTION_DRAG_LOCATION:
				dragAndDropListener.showOptionFromCurrentPosition(event.getX(), event.getY());
				return true;
			case DragEvent.ACTION_DRAG_EXITED:
				dragAndDropListener.goOutsideView(true, event.getX(), event.getY());
				return true;
			case DragEvent.ACTION_DROP:
				dragAndDropListener.moveOrMerge(v, event.getX(), event.getY());
				return true;
			case DragEvent.ACTION_DRAG_ENDED:
				dragAndDropListener.dragEnded();
				return true;
			default:
				Log.e("Drag and Drop: ", "Unknown action type receiver by OnDragListener");
				break;
		}

		return false;
	}
}
