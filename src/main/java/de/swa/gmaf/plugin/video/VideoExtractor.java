package de.swa.gmaf.plugin.video;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.videointelligence.v1.AnnotateVideoProgress;
import com.google.cloud.videointelligence.v1.AnnotateVideoRequest;
import com.google.cloud.videointelligence.v1.AnnotateVideoResponse;
import com.google.cloud.videointelligence.v1.Entity;
import com.google.cloud.videointelligence.v1.Feature;
import com.google.cloud.videointelligence.v1.LabelAnnotation;
import com.google.cloud.videointelligence.v1.LabelSegment;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient;

import de.swa.gmaf.plugin.text.extension.FileExtensionPlugin;

public class VideoExtractor implements FileExtensionPlugin {
	private File file;

	public boolean canProcess(File f) {
		String extension = f.getName();
		String ext = extension.toUpperCase();
		file = f;
		if (ext.endsWith("WAV") || ext.endsWith("MP3") || ext.endsWith("AIFF") || ext.endsWith("M4A"))
			return true;
		return false;
	}

	public Vector<File> getAdditionalAssets() {
		return new Vector<File>();
	}

	public Vector<String> getExtensions() {
		Vector<String> ext = new Vector<String>();
		ext.add(".wav");
		ext.add(".mp3");
		ext.add(".aiff");
		ext.add(".m4a");
		return ext;
	}

	private Hashtable<String, String> attributes = new Hashtable<String, String>();
	private Vector<String> sentences = new Vector<String>();

	public Hashtable<String, String> getKeywords() {
		return attributes;
	}

	public Vector<String> getSentences() {
		return sentences;
	}

	public void process() {
		attributes.clear();
		sentences.clear();

		try {
			try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create()) {
				// The Google Cloud Storage path to the video to annotate.
				String gcsUri = "gs://cloud-samples-data/video/cat.mp4";

				// Create an operation that will contain the response when the operation
				// completes.
				AnnotateVideoRequest request = AnnotateVideoRequest.newBuilder().setInputUri(gcsUri)
						.addFeatures(Feature.LABEL_DETECTION).build();

				OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response = client
						.annotateVideoAsync(request);

				System.out.println("Waiting for operation to complete...");

				List<VideoAnnotationResults> results = response.get().getAnnotationResultsList();
				if (results.isEmpty()) {
					System.out.println("No labels detected in " + gcsUri);
					return;
				}
				for (VideoAnnotationResults result : results) {
					System.out.println("Labels:");
					// get video segment label annotations
					for (LabelAnnotation annotation : result.getSegmentLabelAnnotationsList()) {
						System.out.println("Video label description : " + annotation.getEntity().getDescription());
						// categories
						for (Entity categoryEntity : annotation.getCategoryEntitiesList()) {
							System.out.println("Label Category description : " + categoryEntity.getDescription());
						}
						// segments
						for (LabelSegment segment : annotation.getSegmentsList()) {
							double startTime = segment.getSegment().getStartTimeOffset().getSeconds()
									+ segment.getSegment().getStartTimeOffset().getNanos() / 1e9;
							double endTime = segment.getSegment().getEndTimeOffset().getSeconds()
									+ segment.getSegment().getEndTimeOffset().getNanos() / 1e9;
							System.out.printf("Segment location : %.3f:%.3f\n", startTime, endTime);
							System.out.println("Confidence : " + segment.getConfidence());
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
