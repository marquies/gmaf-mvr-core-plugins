package de.swa.gmaf.plugin.googlevision;

import java.util.Vector;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;

import de.swa.mmfg.CompositionRelationship;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.Node;
import de.swa.ui.Logger;

/** Landmark Detection Plugin based on the GoogleVision API 
 * 
 * @author stefan_wagenpfeil
 */

public class LandmarkDetection extends GoogleVisionBasePlugin {
	private Vector<Node> objects = new Vector<Node>();

	protected Feature getSearchFeature() {
		Feature f = Feature.newBuilder().setType(Type.LANDMARK_DETECTION).build();
		return f;
	}

	protected void processResult(AnnotateImageResponse res, MMFG fv) {
		for (EntityAnnotation annotation : res.getLandmarkAnnotationsList()) {
			Node n = fv.getCurrentNode();
			String txt = annotation.getDescription();
			Logger.getInstance().log("----- LANDMARK DETECTED: " + txt);
			Node cn = new Node(txt, fv);
			cn.addTechnicalAttribute(getBoundingBox(annotation.getBoundingPoly()));
			n.addChildNode(cn);
			objects.add(cn);
			cn.setDetectedBy(this.getClass().getName());
			cn.addCompositionRelationship(new CompositionRelationship(CompositionRelationship.RELATION_PART_OF, n));
		}
	}

	public boolean providesRecoursiveData() {
		return false;
	}
	
	public Vector<Node> getDetectedNodes() {
		return objects;
	}

}
