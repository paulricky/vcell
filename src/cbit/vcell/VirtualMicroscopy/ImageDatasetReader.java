package cbit.vcell.VirtualMicroscopy;

import java.io.File;

import org.vcell.util.ISize;

import cbit.vcell.client.ClientRequestManager;
import cbit.vcell.client.task.ClientTaskStatusSupport;

public interface ImageDatasetReader {

	public abstract ClientRequestManager.ImageSizeInfo getImageSizeInfo(
			String fileName, Integer forceZSize) throws Exception;

	public abstract ImageDataset readImageDataset(String imageID,
			ClientTaskStatusSupport status) throws Exception;

	public abstract ImageDataset[] readImageDatasetChannels(String imageID,
			ClientTaskStatusSupport status, boolean bMergeChannels,
			Integer timeIndex, ISize resize) throws Exception;

	public abstract ImageDataset readImageDatasetFromMultiFiles(File[] files, ClientTaskStatusSupport status, boolean isTimeSeries, double timeInterval) throws Exception;
	
}