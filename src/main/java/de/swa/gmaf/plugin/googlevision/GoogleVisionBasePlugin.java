package de.swa.gmaf.plugin.googlevision;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.NormalizedVertex;
import com.google.cloud.vision.v1.Vertex;
import com.google.protobuf.ByteString;

import de.swa.gmaf.plugin.GMAF_Plugin;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.Node;
import de.swa.mmfg.TechnicalAttribute;

/** this class acts as a basis for the GoogleVision API and contains standard 
 * methods for authentication, authorisation and API calls.
 * 
 * @author stefan_wagenpfeil
 */
public abstract class GoogleVisionBasePlugin implements GMAF_Plugin {
	// paramters to represent width and height of the processed image
	protected int width, height;

	// 20200801 (Stefan Wagenpfeil): initially created
	// 20201213 (Stefan Wagenpfeil): added parameters for URL-based calls
	// 20210105 (Stefan Wagenpfeil): switched to updated libraries
	// 20210402 (Stefan Wagenpfeil): update for semantics included
	
	/** the process method processes a Multimedia asset into a Multimedia Feature Graph
	 * @param url 	URL, where the asset to be processed is available
	 * @param f 	File-pointer, if the asset can be accessed via FIle-IO
	 * @param bytes byte-array containing an asset's content
	 * @param fv	MMFG, in which the detected features should be inserted
	 */	
	public final void process(URL url, File f, byte[] bytes, MMFG fv) {
		try {
			// Node: for this plugin, only file-based calls should be allowed
			BufferedImage img = ImageIO.read(f);
			width = img.getWidth(null);
			height = img.getHeight(null);
			
			// only images with a given minimum size can be processed
			if (width < 200) return;
			if (height < 200) return;
		}
		catch (Exception x) {
			x.printStackTrace();
		}
		try {
			// call GoogleVision-API
			ImageAnnotatorClient vision = ImageAnnotatorClient.create();
			try {
				ByteString imgBytes = ByteString.copyFrom(bytes);
				List<AnnotateImageRequest> requests = new ArrayList<AnnotateImageRequest>();
				Image img = Image.newBuilder().setContent(imgBytes).build();
	
				Feature feat = getSearchFeature(); // Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
	
				AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
				requests.add(request);
				BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
				List<AnnotateImageResponse> responses = response.getResponsesList();
	
				for (AnnotateImageResponse res : responses) {
					if (res.hasError()) {
						System.out.format("Error: %s%n", res.getError().getMessage());
						return;
					}
					try {
						processResult(res, fv);
					} catch (Exception x) {
						System.out.println("ERROR processing annotation " + res.toString() + " > " + x.getMessage());
					}
				}
			}
			catch (Exception x) {
				x.printStackTrace();
			}
			vision.close();
		} catch (Exception x) {
			x.printStackTrace();
			System.out.println("Google Error: " + x);
		}
	}

	public boolean canProcess(String extension) {
		if (extension.equalsIgnoreCase(".jpg")) return true;
		if (extension.equalsIgnoreCase(".jpeg")) return true;
		if (extension.equalsIgnoreCase(".png")) return true;
		if (extension.equalsIgnoreCase(".tiff")) return true;
		if (extension.equalsIgnoreCase(".gif")) return true;
		return false;
	}
	
	protected TechnicalAttribute getBoundingBox(BoundingPoly poly) {
		int x = Integer.MAX_VALUE;
		int y = Integer.MAX_VALUE;
		int max_x = 0;
		int max_y = 0;
		if (poly.getNormalizedVerticesCount() > 0) {
			for (NormalizedVertex v : poly.getNormalizedVerticesList()) {
				int vx = (int)(v.getX() * width);
				int vy = (int)(v.getY() * height);
				if (vx < x)
					x = vx;
				if (vx > max_x)
					max_x = vx;
				if (vy < y)
					y = vy;
				if (vy > max_y)
					max_y = vy;
			}
		} else {
			for (Vertex v : poly.getVerticesList()) {
				if (v.getX() < x)
					x = v.getX();
				if (v.getX() > max_x)
					max_x = v.getX();
				if (v.getY() < y)
					y = v.getY();
				if (v.getY() > max_y)
					max_y = v.getY();
			}
		}
		if (x == Integer.MAX_VALUE)
			x = 0;
		if (y == Integer.MAX_VALUE)
			y = 0;
		TechnicalAttribute ta = new TechnicalAttribute(x, y, (max_x - x), (max_y - y), 0, 0);
		return ta;
	}

	public Vector<Node> getDetectedNodes() {
		return null;
	}

	public boolean isGeneralPlugin() {
		return false;
	}
	
	protected abstract Feature getSearchFeature();

	protected abstract void processResult(AnnotateImageResponse res, MMFG fv);

	public abstract boolean providesRecoursiveData();
}
