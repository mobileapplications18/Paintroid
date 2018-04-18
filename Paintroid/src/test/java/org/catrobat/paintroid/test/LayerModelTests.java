/**
 *  Paintroid: An image manipulation application for Android.
 *  Copyright (C) 2010-2015 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.test;

import android.graphics.Bitmap;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.model.BitmapFactory;
import org.catrobat.paintroid.model.LayerModel;

import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.ui.DrawingSurface;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LayerModelTests {

	private LayerModel layerModel;
	private BitmapFactory bitmapFactory;

	@Before
	public void setUp() {
		bitmapFactory = mock(BitmapFactory.class);
		when(bitmapFactory.createBitmap(anyInt(), anyInt(), any(Bitmap.Config.class))).then(new Answer<Bitmap>() {
			@Override
			public Bitmap answer(InvocationOnMock invocation) throws Throwable {
				Bitmap bitmap = mock(Bitmap.class);
				when(bitmap.getWidth()).thenReturn((Integer) invocation.getArguments()[0]);
				when(bitmap.getHeight()).thenReturn((Integer) invocation.getArguments()[1]);
				when(bitmap.getConfig()).thenReturn((Bitmap.Config) invocation.getArguments()[2]);

				return bitmap;
			}
		});
		layerModel = new LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
		layerModel.setBitmapFactory(bitmapFactory);
		PaintroidApplication.drawingSurface = mock(DrawingSurface.class);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testLayerModel() {
		assertNotNull(layerModel);
		assertEquals(layerModel.getLayerCount(), 1);
		assertTrue((layerModel.getLayer(layerModel.getLayerCount()-1)) instanceof Layer);
		assertEquals(layerModel.getPosition(0), layerModel.getLayerCount()-1);
	}


	@Test
	public void testAddLayer() {
		assertEquals(layerModel.getLayerCount(), 1);
		layerModel.addLayer();
		assertEquals(layerModel.getLayerCount(), 2);
		assertTrue((layerModel.getLayer(0)) instanceof Layer);
		layerModel.removeLayer(layerModel.getLayer(0));
		assertEquals(layerModel.getLayerCount(), 1);
	}


	@Test
	public void testAddLayerExisting() {
		assertEquals(layerModel.getLayerCount(), 1);
		Layer testLayer = new Layer(layerModel.getLayerCount(), bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
		layerModel.addLayer(testLayer);
		assertEquals(layerModel.getLayerCount(), 2);
		assertTrue((layerModel.getLayer(0)) instanceof Layer);
		assertEquals((layerModel.getLayer(0)),testLayer);
		layerModel.removeLayer(layerModel.getLayer(0));
		assertEquals(layerModel.getLayerCount(), 1);
	}


	@Test
	public void testRemoveLayer() {
		assertEquals(layerModel.getLayerCount(), 1);
		layerModel.removeLayer(layerModel.getLayer(0));
		assertEquals(layerModel.getLayerCount(), 0);
		Layer testLayer = new Layer(layerModel.getLayerCount(), bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
		layerModel.addLayer(testLayer);
		assertEquals(layerModel.getLayerCount(), 1);
		Layer testLayer2 = new Layer(layerModel.getLayerCount(), bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
		layerModel.addLayer(testLayer2);
		layerModel.removeLayer(layerModel.getLayer(0));
		assertEquals(layerModel.getLayer(0), testLayer);
	}

	@Test
	public void testMerge() {
		Layer testLayer = new Layer(layerModel.getLayerCount(), bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
		layerModel.addLayer(testLayer);
		assertEquals(layerModel.getLayerCount(), 2);
		layerModel.mergeLayer(layerModel.getLayer(0), layerModel.getLayer(1));
		assertEquals(layerModel.getLayerCount(), 1);
	}

	@Test
	public void testClearLayer() {
		Layer testLayer = new Layer(layerModel.getLayerCount(), bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
		layerModel.addLayer(testLayer);
		layerModel.addLayer(testLayer);
		layerModel.addLayer(testLayer);
		layerModel.clearLayer();
		assertEquals(layerModel.getLayerCount(), 1);
	}
}