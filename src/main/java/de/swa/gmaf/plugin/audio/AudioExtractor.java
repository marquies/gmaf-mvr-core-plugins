package de.swa.gmaf.plugin.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import de.swa.gmaf.GMAF;
import de.swa.gmaf.plugin.GMAF_Plugin;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.Node;

/** automated scene detection based on FFMPEG **/
public class AudioExtractor implements GMAF_Plugin {
	public boolean canProcess(String extension) {
		String[] extensions = { "mpg", "mpeg", "mov", "flv", "mp4", "mxf", "qt", "m4v" };
		for (String ext : extensions) {
			if (ext.equalsIgnoreCase(extension))
				return true;
		}
		return false;
	}

	public void process(URL url, File f, byte[] bytes, MMFG fv) {
		if (!f.exists()) {
			// Download the video
			try {
				URLConnection uc = url.openConnection();
				String extension = url.toString();
				extension = extension.substring(extension.lastIndexOf("."), extension.length());
				f = new File("temp/" + System.currentTimeMillis() + extension);
				FileOutputStream fout = new FileOutputStream(f);
				fout.write(uc.getInputStream().readAllBytes());
				fout.close();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		if (!f.exists()) {
			// create the video out of the bytes
			try {
				f = new File("temp/" + System.currentTimeMillis() + ".mp4");
				FileOutputStream fout = new FileOutputStream(f);
				fout.write(bytes);
				fout.close();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		if (f.exists()) {
			// create folder for the scene screenshots
			String[] cmd = { "ffmpeg", "-i", f.getAbsolutePath(), "temp/" + f.getName() + "_Audio.mp3" };
			try {
				Runtime.getRuntime().exec(cmd);

				GMAF gmaf = new GMAF();

				File fi = new File("temp/" + f.getName() + "_Audio.mp3");
				MMFG sceneMMFG = gmaf.processAsset(fi);
				String name = fi.getName();
				Node tcNode = new Node("Audio", fv);
				for (Node n : sceneMMFG.getNodes()) {
					tcNode.addChildNode(n);
					nodes.add(n);
				}
				fv.addNode(tcNode);
				nodes.add(tcNode);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	Vector<Node> nodes = new Vector<Node>();

	public Vector<Node> getDetectedNodes() {
		return nodes;
	}

	public boolean isGeneralPlugin() {
		return false;
	}

	public boolean providesRecoursiveData() {
		return false;
	}
}
