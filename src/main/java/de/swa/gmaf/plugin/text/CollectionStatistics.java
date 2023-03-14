package de.swa.gmaf.plugin.text;

import java.io.File;
import java.util.Vector;

import de.swa.gmaf.extensions.defaults.Word;
import de.swa.ui.Configuration;

/** Statistical approach to Feature Relevance - WORK IN PROGRESS **/
public class CollectionStatistics {
	private CollectionStatistics() {
		calculateWordCount();
		calculateWordFrequency();
		calculateWordRelevance();
	}
	private static CollectionStatistics instance;
	public static CollectionStatistics getInstance() {
		if (instance == null) instance = new CollectionStatistics();
		return instance;
	}
	
	private void calculateWordCount() {
		Vector<String> paths = Configuration.getInstance().getCollectionPaths();
		for (String p : paths) {
			File f = new File(p);
			File[] fs = f.listFiles();
			for (File fi : fs) {
				
			}
		}
	}
	private void calculateWordFrequency() {
		
	}
	private void calculateWordRelevance() {
		
	}
	
	public float getWordFrequency(Word w) {
		return 0.0f;
	}
	
	public float getWordRelevance(Word w) {
		return 0.0f;
	}
}
