package org.catrobat.paintroid.test.command;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.implementation.AddLayerCommand;
import org.catrobat.paintroid.command.implementation.MergeLayerCommand;
import org.catrobat.paintroid.command.implementation.RemoveLayerCommand;
import org.catrobat.paintroid.model.BitmapFactory;
import org.catrobat.paintroid.model.LayerModel;
import org.catrobat.paintroid.model.Layer;
import org.catrobat.paintroid.ui.DrawingSurface;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LayerCommandTest {


    private LayerModel layerModel;
    private BitmapFactory bitmapFactory;
    private AddLayerCommand addLayerCommand;

    @Before
    public void setUp() {
        bitmapFactory = mock(BitmapFactory.class);
        when(bitmapFactory.createBitmap(anyInt(), anyInt(), any(Bitmap.Config.class))).then(new Answer<Bitmap>() {
            @Override
            public Bitmap answer(InvocationOnMock invocation) throws Throwable {
                Bitmap bitmap = mock(Bitmap.class);
                return bitmap;
            }
        });
        layerModel = new LayerModel(bitmapFactory.createBitmap(10, 10, Bitmap.Config.ARGB_8888));
        layerModel.setBitmapFactory(bitmapFactory);
        PaintroidApplication.drawingSurface = mock(DrawingSurface.class);
        when(PaintroidApplication.drawingSurface.getCanvas()).then(new Answer<Canvas>() {
            @Override
            public Canvas answer(InvocationOnMock invocation) throws Throwable {
                Canvas canvas = mock(Canvas.class);
                return canvas;
            }
        });
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddLayer() {
        AddLayerCommand addLayerCommand = new AddLayerCommand(bitmapFactory);

        assertEquals(layerModel.getLayerCount(), 1);

        addLayerCommand.run(PaintroidApplication.drawingSurface.getCanvas(), layerModel);
        assertEquals(layerModel.getLayerCount(), 2);
        assertTrue((layerModel.getLayer(0)) instanceof Layer);
    }

    @Test
    public void testRemoveLayer() {
        RemoveLayerCommand removeLayerCommand = new RemoveLayerCommand();
        removeLayerCommand.run(PaintroidApplication.drawingSurface.getCanvas(), layerModel);
        assertEquals(layerModel.getLayerCount(), 0);
    }

    @Test
    public void testMergeLayer() {
        AddLayerCommand addLayerCommand = new AddLayerCommand(bitmapFactory);

        int layer_count = layerModel.getLayerCount();

        addLayerCommand.run(PaintroidApplication.drawingSurface.getCanvas(), layerModel);
        addLayerCommand.run(PaintroidApplication.drawingSurface.getCanvas(), layerModel);

        assertEquals(layerModel.getLayerCount(), layer_count+2);

        MergeLayerCommand mergeLayerCommand = new MergeLayerCommand(0,1, bitmapFactory);

        assertEquals(layerModel.getLayerCount(), layer_count+2);
    }
} 
