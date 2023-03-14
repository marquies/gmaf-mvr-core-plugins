package de.swa.gmaf.plugin.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Vector;

import de.swa.gmaf.extensions.defaults.GeneralDictionary;
import de.swa.gmaf.plugin.text.extension.FileExtensionFactory;
import de.swa.gmaf.plugin.text.extension.FileExtensionPlugin;

public class TFIDFCalculator {
	private Vector<String> allWords = new Vector<String>();
	private Hashtable<String, Integer> documentCount = new Hashtable<String, Integer>();
	private Hashtable<File, Hashtable<String, Integer>> fileWordCount = new Hashtable<File, Hashtable<String, Integer>>();
	private float threshold = 0;
	
	private static TFIDFCalculator instance = null;
	public static TFIDFCalculator getInstance(String textFolder, float threshold, float collectionThreshold, boolean cache) {
		if (instance == null) instance = new TFIDFCalculator(textFolder, threshold, collectionThreshold, cache, true);
		return instance;
	}
	
	private TFIDFCalculator(String textFolder, float threshold, float collectionThreshold, boolean cache, boolean recursive) {
		if (cache) {
			try {
				ObjectInputStream oin = new ObjectInputStream(new FileInputStream("temp/tfidf.ser"));
				relevantWords = (Vector<String>)oin.readObject();
				irrelevantWords = (Vector<String>)oin.readObject();
				return;
			}
			catch (Exception x) {
				x.printStackTrace();
			}
		}
		
		this.threshold = threshold;
		File f = new File(textFolder);
		Vector<File> allFiles = new Vector<File>();
		getRecursiveFileList(f, allFiles);
		int fcount = 1;
		int numOfFiles = allFiles.size();
//		File[] fs = f.listFiles();
		for (File fi : allFiles) {
			int globalWC = allWords.size();
			FileExtensionPlugin fep = FileExtensionFactory.getInstance().getPluginFor(fi);
			if (fep == null) continue;
			fep.process();
			Vector<String> sentences = fep.getSentences();
			Vector<String> documentWords = new Vector<String>();
			Hashtable<String, Integer> documentWordCount = new Hashtable<String, Integer>();
			for (String sentence : sentences) {
				String[] words = sentence.split(" ");
				for (String word : words) {
					word = word.replace(',', ' ').trim();
					word = word.replace('\'', ' ').trim();
					word = word.replace(':', ' ').trim();
					word = word.replace('"', ' ').trim();
					word = word.replace('(', ' ').trim();
					word = word.replace(')', ' ').trim();
					if (word.startsWith("<")) continue;
					if (word.endsWith(">")) continue;
					if (word.indexOf("=") > 0) continue;
					
					String wordStem = null;
					try { wordStem = GeneralDictionary.getInstance().getWord(word).get(0).getWord(); } catch (Exception x) {}
					if (wordStem == null) continue;
					
					if (!documentWords.contains(wordStem)) {
						documentWords.add(wordStem);
						documentWordCount.put(wordStem, 1);
					}
					else {
						int c = documentWordCount.get(wordStem);
						documentWordCount.remove(wordStem);
						c++;
						documentWordCount.put(wordStem, c);
					}
				}
			}

			// get relevant Words of document and add them to the global list
			int maxCount = 0;
			for (String s : documentWords) {
				int c = documentWordCount.get(s);
				if (c > maxCount) maxCount = c;
			}
			int relevanceLevel = (int)(maxCount * collectionThreshold);
			
			for (String s : documentWords ) {
				int count = documentWordCount.get(s);
				if (count <= relevanceLevel) {
					if (!allWords.contains(s)) allWords.add(s);
				}
				else {
					if (!irrelevantWords.contains(s)) irrelevantWords.add(s);
				}
			}
			
			fileWordCount.put(fi, documentWordCount);
			fcount ++;
			System.out.println("File " + fcount + "/" + numOfFiles + " " + fi.getName() + " -> " + documentWordCount.size() + " different words in " + sentences.size() + " sentences. Global word count " + allWords.size() + " (+" + (allWords.size() - globalWC) + ")");
		}
		
		int numberOfFiles = allFiles.size();
		int documentRelevance = (int)(numberOfFiles * collectionThreshold);
		int maxRelevance = 10000;
		System.out.println("Relevance Level: " + maxRelevance + " for " + numberOfFiles + " files.");
		int idx = 0;
		for (String word : allWords) {
			int counter = 0;
			idx ++;
			for (File fi : allFiles) {
				Hashtable<String, Integer> fwc = fileWordCount.get(fi); 
				if (fwc != null && fwc.containsKey(word)) counter ++;
			}
			
			// check if the word is relevant or irrelevant
			if (counter >= documentRelevance || counter >= maxRelevance) {
				irrelevantWords.add(word);
			}
			else relevantWords.add(word);
			System.out.println("WORD " + idx + "/" + allWords.size() + " -> " + word);
		}
		
		try {
			ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream("temp/tfidf.ser"));
			oout.writeObject(relevantWords);
			oout.writeObject(irrelevantWords);
			oout.flush();
			oout.close();
			System.out.println("written to cache temp/tfidf.ser");
		}
		catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	private void getRecursiveFileList(File baseFolder, Vector<File> files) {
		File[] fs = baseFolder.listFiles();
		for (File fi : fs) {
			if (fi.isDirectory()) getRecursiveFileList(fi, files);
			else if (fi.getName().endsWith(".wapo")) files.add(fi);
		}
	}
	
	private Vector<String> relevantWords = new Vector<String>();
	private Vector<String> irrelevantWords = new Vector<String>();
	
	public Vector<String> getRelevantWords() {
		return relevantWords;
	}
	
	public Vector<String> getIrrelevantWords() {
		return irrelevantWords;
	}
	
	public static void main(String[] args) {
		TFIDFCalculator tc = new TFIDFCalculator("/Users/stefan_wagenpfeil/Downloads/WashingtonPost.v4/split/", 0.9f, 0.75f, false, true);
		System.out.println("Relevant: " + tc.getRelevantWords().size() + ", Irrelevant: " + tc.getIrrelevantWords().size());
//		for (String s : tc.getIrrelevantWords()) System.out.println(" - " + s);
	}
	
	// Files: 728627
	// Global word count 41920 (unique)
	// Relevant: 34113, Irrelevant: 18618 -> 1000 threshold
	// Relevant: 40111, Irrelevant: 12620 -> 10000 threshold
}
