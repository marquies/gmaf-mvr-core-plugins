package de.swa.gmaf.plugin.text;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import de.swa.gmaf.GMAF;
import de.swa.gmaf.plugin.GMAF_Plugin;
import de.swa.gmaf.plugin.text.extension.FileExtensionFactory;
import de.swa.gmaf.plugin.text.extension.FileExtensionPlugin;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.Node;
import de.swa.ui.Logger;

/** GMAF Plugin for the BagOfWords Detection algorithm for texts **/
public class BagOfWordsDetection implements GMAF_Plugin {
	Vector<Node> nodes = new Vector<Node>();
	Vector<String> blacklist = new Vector<String>();
	
	public BagOfWordsDetection() {
		blacklist.add("a");
		blacklist.add("the");
	}
	
	public Vector<Node> getDetectedNodes() {
		return nodes;
	}

	public boolean isGeneralPlugin() {
		return false;
	}
	
	private FileExtensionPlugin fep;
	public boolean canProcess(String extension) {
		return FileExtensionFactory.getInstance().canProcess(extension);
	}


	private void process(String source, String target, MMFG fv) {
		Node n = new Node(source, fv);
		n.setDetectedBy(getClass().toString());
		Node ntarget = new Node(target, fv);
		n.addChildNode(ntarget);
		
		nodes.add(n);
		nodes.add(ntarget);
	}
	
	public void process(URL url, File f, byte[] bytes, MMFG fv) {
		if (f.exists()) {
			FileExtensionPlugin fep = FileExtensionFactory.getInstance().getPluginFor(f);
			if (fep == null) return; 
			fep.process();
			
			Hashtable<String, String> attributes = fep.getKeywords();
			for (String key : attributes.keySet()) {
				String val = attributes.get(key);
				process(key, val, fv);
			}
			Vector<String> sentences = fep.getSentences();
			Hashtable<String, Integer> wordBag = new Hashtable<String, Integer>();

			for (String sentence : sentences) {
				Logger.getInstance().log("DETECTED: " + sentence);
				String[] words = sentence.split(" ");
				for (String word : words) {
					word = word.replace(",", " ").trim();
					word = word.toUpperCase();
					int count = 1;
					if (wordBag.containsKey(word)) {
						count = wordBag.get(word);
						count ++;
					}
					wordBag.put(word, count);
				}
			}
			Node contentNode = new Node("content", fv);
			for (String key : wordBag.keySet()) {
				int number = wordBag.get(key);
				if (number >= 2) {
					Node keyNode = new Node(key, fv);
					Node countNode = new Node("" + number, fv);
					keyNode.addChildNode(countNode);
					contentNode.addChildNode(keyNode);
					nodes.add(keyNode);
					nodes.add(countNode);
				}
			}
			
			// additional multimedia parts
			Vector<File> additionalParts = fep.getAdditionalAssets();
			GMAF gmaf = new GMAF();
			for (File fi : additionalParts) {
				try {
					MMFG partMMFG = gmaf.processAsset(fi);
					Vector<Node> partMMFGNodes = partMMFG.getNodes();
					for (Node ni : partMMFGNodes) 
						contentNode.addChildNode(ni);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			nodes.add(contentNode);
		}
	}

	public boolean providesRecoursiveData() {
		return false;
	}
}
