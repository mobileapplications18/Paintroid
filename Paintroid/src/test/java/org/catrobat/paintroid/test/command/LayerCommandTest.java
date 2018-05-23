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

package org.catrobat.paintroid.test.command;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.catrobat.paintroid.command.implementation.AddLayerCommand;
import org.catrobat.paintroid.command.implementation.RemoveLayerCommand;
import org.catrobat.paintroid.model.BitmapFactory;
import org.catrobat.paintroid.model.LayerModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class LayerCommandTest {

    private LayerModel layerModel;
    private BitmapFactory bitmapFactory;

    @Mock
    private Canvas canvas;

    @Before
    public void setUp() {
        bitmapFactory = mock(BitmapFactory.class);
        when(bitmapFactory.createBitmap(anyInt(), anyInt(), any(Bitmap.Config.class))).then(new Answer<Bitmap>() {
            @Override
            public Bitmap answer(InvocationOnMock invocation) throws Throwable {
                return mock(Bitmap.class);
            }
        });
        layerModel = new LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
        layerModel.setBitmapFactory(bitmapFactory);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddLayer() {
        assertEquals(1, layerModel.getLayerCount());

        new AddLayerCommand(bitmapFactory).run(canvas, layerModel);
        assertEquals(2, layerModel.getLayerCount());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveLastLayer() {
        new RemoveLayerCommand().run(canvas, layerModel);
    }

    public void testRemoveLayer() {
        new AddLayerCommand(bitmapFactory).run(canvas, layerModel);
        assertEquals(2, layerModel.getLayerCount());
        new RemoveLayerCommand().run(canvas, layerModel);
        assertEquals(1, layerModel.getLayerCount());
    }
} 
