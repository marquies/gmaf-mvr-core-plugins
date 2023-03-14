package de.swa.gmaf.plugin.text.extension;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/** implementation of Plain-Text format **/

public class TXT_ExtensionPlugin implements FileExtensionPlugin{
	private File f;
	public boolean canProcess(File f) {
		this.f = f;
		if (f.getName().endsWith("txt")) return true;
		return false;
	}

	public Vector<File> getAdditionalAssets() {
		return new Vector<File>();
	}

	public Vector<String> getExtensions() {
		Vector<String> extensions = new Vector<String>();
		extensions.add(".txt");
		return extensions;
	}
	
	private Hashtable<String, String> attributes = new Hashtable<String, String>();
	private Vector<String> sentences = new Vector<String>();
	
	public void process() {
		attributes.clear();
		sentences.clear();
		
		try {
			RandomAccessFile rf = new RandomAccessFile(f, "r");
			StringBuffer content = new StringBuffer();
			String line = "";
			while ((line = rf.readLine()) != null) {
				content.append(line);
			}
			StringTokenizer st = new StringTokenizer(content.toString(), ".");
			while (st.hasMoreTokens()) {
				String sentence = st.nextToken();
				sentences.add(sentence);
			}
			rf.close();
		}
		catch (Exception x) {
			x.printStackTrace();
		}
	}

	public Hashtable<String, String> getKeywords() {
		return attributes;
	}

	public Vector<String> getSentences() {
		return sentences;
	}
}
