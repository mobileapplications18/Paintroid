package org.catrobat.paintroid.command.implementation;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import org.catrobat.paintroid.command.LayerBitmapCommand;
import org.catrobat.paintroid.listener.LayerListener;
import org.catrobat.paintroid.model.LayerModel;
import org.catrobat.paintroid.tools.Layer;

import java.util.ArrayList;

public class LayerCommand extends BaseCommand {
	private ArrayList<Integer> listOfMergedLayerIds;
	private ArrayList<LayerBitmapCommand> layersBitmapCommands;
	private CommandManagerImplementation.CommandType layerCommandType;

	private int oldLayerPosition;

	public LayerCommand() {
		oldLayerPosition = -1;
	}

	public LayerCommand(ArrayList<Integer> listOfMergedLayerIds) {
		this.listOfMergedLayerIds = listOfMergedLayerIds;
		this.layersBitmapCommands = new ArrayList<>(listOfMergedLayerIds.size());
		this.layerCommandType = CommandManagerImplementation.CommandType.NO_LAYER_COMMAND;
		this.oldLayerPosition = -1;
	}

	public Layer getLayer() {
		return getLayerModel().getCurrentLayer();
	}

	public ArrayList<Integer> getLayersToMerge() {
		return listOfMergedLayerIds;
	}

	public ArrayList<LayerBitmapCommand> getLayersBitmapCommands() {
		return layersBitmapCommands;
	}

	public void setLayersBitmapCommands(ArrayList<LayerBitmapCommand> layersBitmapCommandManagerList) {
		this.layersBitmapCommands = layersBitmapCommandManagerList;
	}

	public CommandManagerImplementation.CommandType getLayerCommandType() {
		return layerCommandType;
	}

	public void setLayerCommandType(CommandManagerImplementation.CommandType type) {
		layerCommandType = type;
	}

	public int getOldLayerPosition() {
		return oldLayerPosition;
	}

	public void setOldLayerPosition(int pos) {
		oldLayerPosition = pos;
	}

	@Override
	public void run(@NonNull Canvas canvas, @NonNull LayerModel layerModel) {
	}

	private LayerModel getLayerModel() {
		return LayerListener.getInstance().getLayerModel();
	}
}
