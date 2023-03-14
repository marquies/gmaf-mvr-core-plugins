package de.swa.gmaf.plugin.googlevision;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;

import de.swa.mmfg.MMFG;
import de.swa.mmfg.Node;
import de.swa.mmfg.SemanticRelationship;
import de.swa.mmfg.TechnicalAttribute;
import de.swa.ui.Logger;

/** Object Detection Plugin based on the GoogleVision API 
 * 
 * @author stefan_wagenpfeil
 */

public class ObjectDetection extends GoogleVisionBasePlugin {
	private Vector<Node> objects = new Vector<Node>();
	
	protected Feature getSearchFeature() {
		Feature f = Feature.newBuilder().setType(Type.OBJECT_LOCALIZATION).build();
		return f;
	}

	protected void processResult(AnnotateImageResponse res, MMFG fv) {
		objects = new Vector<Node>();
		int counter = 1;
		for (LocalizedObjectAnnotation annotation : res.getLocalizedObjectAnnotationsList()) {
			Node n = fv.getCurrentNode();
			String txt = annotation.getName();
			String mid = annotation.getMid();
			
			Logger.getInstance().log("----- OBJECT DETECTED: " + txt);
			
			for (Node ci : n.getChildNodes()) {
				if (ci.getName().equals(txt)) {
					txt = txt + "_" + counter;
					counter ++;
				}
			}
			
			Node cn = new Node(txt, fv);
			TechnicalAttribute ta = getBoundingBox(annotation.getBoundingPoly());
			
			// check, if bounding box is similar to whole image
			if (ta.getRelative_x() < 30 && ta.getRelative_y() < 30 && (width - ta.getWidth()) < 60 && (height - ta.getHeight()) < 60) {
				System.out.println("simmilar bounding box");
			}
			else {
				cn.addTechnicalAttribute(ta);
			}
			
			n.addChildNode(cn);
			objects.add(cn); 
			cn.setDetectedBy(this.getClass().getName());
			if (!mid.equals("")) {
				URL url = null;
				try {
					url = new URL("https://kgsearch.googleapis.com/v1/id/" + mid);
				} catch (MalformedURLException e) {
				}
				cn.addSemanticRelationship(new SemanticRelationship(url, "Google Knowledge Graph ID: " + mid));
			}
		}
	}
		
	public Vector<Node> getDetectedNodes() {
		return objects;
	}
	
	public boolean providesRecoursiveData() {
		return true;
	}
}
