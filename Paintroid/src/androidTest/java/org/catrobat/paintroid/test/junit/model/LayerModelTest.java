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

package org.catrobat.paintroid.test.junit.model;

import android.graphics.Bitmap;

import org.catrobat.paintroid.model.LayerModel;

import org.catrobat.paintroid.tools.Layer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LayerModelTest {

	private Bitmap bitmap;
	private LayerModel layerModel;

	@Before
	public void setUp() {
		bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
		layerModel = new LayerModel(bitmap);
	}

	@After
	public void tearDown() {
		bitmap.recycle();
		bitmap = null;
	}

	@Test
	public void testLayerModel() {
		try{
			assertNotNull(layerModel);
			assertEquals(layerModel.getLayerCount(), 1);
			assertTrue((layerModel.getLayer(layerModel.getLayerCount()-1)) instanceof Layer);
			assertEquals(layerModel.getPosition(0), layerModel.getLayerCount()-1);
		}catch (Exception e){
			fail("EXCEPTION: " + e.toString());
		}
	}


//Drawing Surface must not be null error
	/*
	@Test
	public void testAddLayer() {
		try{
			assertEquals(layerModel.getLayerCount(), 1);
			layerModel.addLayer();
			assertEquals(layerModel.getLayerCount(), 2);
			assertTrue((layerModel.getLayer(0)) instanceof Layer);
			layerModel.removeLayer(layerModel.getLayer(0));
			assertEquals(layerModel.getLayerCount(), 1);
		}catch (Exception e){
			fail("EXCEPTION: " + e.toString());
		}
	}
	*/

	@Test
	public void testAddLayerExisting() {
		try{
			assertEquals(layerModel.getLayerCount(), 1);
			Layer testLayer = new Layer(layerModel.getLayerCount(), bitmap);
			layerModel.addLayer(testLayer);
			assertEquals(layerModel.getLayerCount(), 2);
			assertTrue((layerModel.getLayer(0)) instanceof Layer);
			assertEquals(layerModel.getLayer(0),testLayer);
			layerModel.removeLayer(layerModel.getLayer(0));
			assertEquals(layerModel.getLayerCount(), 1);

		}catch (Exception e){
			fail("EXCEPTION: " + e.toString());
		}
	}


	@Test
	public void testRemoveLayer() {
		try{
			assertEquals(layerModel.getLayerCount(), 1);
			layerModel.removeLayer(layerModel.getLayer(0));
			assertEquals(layerModel.getLayerCount(), 0);

			Layer testLayer = new Layer(layerModel.getLayerCount(), bitmap);
			layerModel.addLayer(testLayer);
			assertEquals(layerModel.getLayerCount(), 1);
			Layer testLayer2 = new Layer(layerModel.getLayerCount(), bitmap);
			layerModel.addLayer(testLayer2);

			layerModel.removeLayer(layerModel.getLayer(0));
			assertEquals(layerModel.getLayer(0), testLayer);
		}catch (Exception e){
			fail("EXCEPTION: " + e.toString());
		}
	}

	@Test
	public void testMerge() {
		try {
			Layer testLayer = new Layer(layerModel.getLayerCount(), bitmap);
			layerModel.addLayer(testLayer);
			assertEquals(layerModel.getLayerCount(), 2);
			layerModel.mergeLayer(layerModel.getLayer(0), layerModel.getLayer(1));
			assertEquals(layerModel.getLayerCount(), 1);

			assertNotNull(layerModel.getLayer(0).getImage());
		}catch (Exception e){
			fail("EXCEPTION: " + e.toString());
		}
	}

//Drawing Surface must not be null error
/*	@Test
	public void testClearLayer() {
		try{
			layerModel.clearLayer();
			assertEquals(layerModel.getLayerCount(), 1);
		}catch (Exception e){
			fail("EXCEPTION: " + e.toString());
		}
	}*/

}