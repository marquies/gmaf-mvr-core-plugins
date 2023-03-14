package de.swa.gmaf.plugin.text.extension;

import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import com.google.gson.Gson;

import de.swa.importers.wapo.Content;
import de.swa.importers.wapo.WashingtonPostItem;

/** implementation of the Washington Post archive-format **/
public class WAPO_ExtensionPlugin implements FileExtensionPlugin {
	private File f;
	public boolean canProcess(File f) {
		this.f = f;
		if (f.getName().endsWith("wapo")) return true;
		return false;
	}

	public Vector<File> getAdditionalAssets() {
		return new Vector<File>();
	}

	public Vector<String> getExtensions() {
		Vector<String> extensions = new Vector<String>();
		extensions.add("wapo");
		return extensions;
	}
	
	private Hashtable<String, String> attributes = new Hashtable<String, String>();
	private Vector<String> sentences = new Vector<String>();
	
	public void process() {
		attributes.clear();
		sentences.clear();
		
		Gson gson = new Gson();
		try {
			WashingtonPostItem ri = gson.fromJson(new FileReader(f), WashingtonPostItem.class);
			attributes.put("title", ri.getTitle());
			attributes.put("author", ri.getAuthor());
			attributes.put("date", "" + ri.getPublishedDate());
			if (ri.getArticleUrl() != null) attributes.put("link", ri.getArticleUrl());
			attributes.put("type", ri.getType());
			attributes.put("source", ri.getSource());

			List<Content> contents = ri.getContents();
			for (Content c : contents) {
				try {
					String content = c.getContent();
					StringTokenizer st = new StringTokenizer(content, ".");
					while (st.hasMoreTokens()) {
						String sentence = st.nextToken();
						sentences.add(sentence);
					}
				}
				catch (Exception x) {
//					x.printStackTrace();
				}
			}
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
