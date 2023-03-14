package de.swa.gmaf.plugin.text.extension;

import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import com.google.gson.Gson;

import de.swa.importers.RssItem;

/** implementation of RSS (Json + .rss) format **/

public class RSS_ExtensionPlugin implements FileExtensionPlugin {
	private File f;
	public boolean canProcess(File f) {
		this.f = f;
		if (f.getName().endsWith("json")) return true;
		if (f.getName().endsWith("rss")) return true;
		return false;
	}

	public Vector<File> getAdditionalAssets() {
		return new Vector<File>();
	}

	public Vector<String> getExtensions() {
		Vector<String> extensions = new Vector<String>();
		extensions.add(".rss");
		extensions.add(".json");
		return extensions;
	}
	
	private Hashtable<String, String> attributes = new Hashtable<String, String>();
	private Vector<String> sentences = new Vector<String>();
	
	public void process() {
		attributes.clear();
		sentences.clear();
		
		Gson gson = new Gson();
		try {
			RssItem ri = gson.fromJson(new FileReader(f), RssItem.class);
			attributes.put("title", ri.getTitle());
			attributes.put("title", ri.getTitle());
			attributes.put("author", ri.getAuthor());
			attributes.put("date", ri.getDate());
			attributes.put("link", ri.getLink());
			
			String content = ri.getContent();
			StringTokenizer st = new StringTokenizer(content, ".");
			while (st.hasMoreTokens()) {
				String sentence = st.nextToken();
				sentences.add(sentence);
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
