package org.catrobat.paintroid.command.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.model.BitmapFactory;
import org.catrobat.paintroid.model.LayerModel;
import org.catrobat.paintroid.model.Layer;
import org.catrobat.paintroid.ui.DrawingSurface;

public class AddLayerCommand extends BaseCommand {

   public AddLayerCommand(BitmapFactory bitmapFactory){
   this.bitmapFactory = bitmapFactory;
   }

   private BitmapFactory bitmapFactory;

   @Override
   public void run(Canvas canvas, LayerModel layerModel) {
      DrawingSurface drawingSurface = PaintroidApplication.drawingSurface;
      Bitmap image = bitmapFactory.createBitmap(drawingSurface.getBitmapWidth(),
                       drawingSurface.getBitmapHeight(), Bitmap.Config.ARGB_8888);

      layerModel.addLayer(new Layer(layerModel.getLayerCount(), image));

      Layer layer = layerModel.getLayer(0);

      layerModel.getCurrentLayer().setSelected(false);
      layerModel.getCurrentLayer().setImage(image);

      layerModel.setCurrentLayer(layer);
      layerModel.getCurrentLayer().setSelected(true);

      PaintroidApplication.drawingSurface.setBitmap(layerModel.getCurrentLayer().getImage());
      }

	@Override
	public void freeResources() {
	}
}
