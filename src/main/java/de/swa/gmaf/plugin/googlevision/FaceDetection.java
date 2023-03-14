package de.swa.gmaf.plugin.googlevision;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;

import de.swa.mmfg.CompositionRelationship;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.Node;
import de.swa.ui.Logger;

/** Face Detection Plugin based on the GoogleVision API 
 * 
 * @author stefan_wagenpfeil
 */

public class FaceDetection extends GoogleVisionBasePlugin {
	protected Feature getSearchFeature() {
		Feature f = Feature.newBuilder().setType(Type.FACE_DETECTION).build();
		return f;
	}

	protected void processResult(AnnotateImageResponse res, MMFG fv) {
		int counter = 1;
		
		for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
			Node n = fv.getCurrentNode();
			Logger.getInstance().log("----- FACE DETECTED ");
			String txt = "Face";
			for (Node ci : n.getChildNodes()) {
				if (ci.getName().equals(txt)) {
					txt = txt + "_" + counter;
					counter ++;
				}
			}
			Node cn = new Node(txt, fv);
			n.addChildNode(cn);
			cn.setDetectedBy(this.getClass().getName());
			cn.addCompositionRelationship(new CompositionRelationship(CompositionRelationship.RELATION_PART_OF, n));
		}
	}
	
	public boolean providesRecoursiveData() {
		return false;
	}
}
