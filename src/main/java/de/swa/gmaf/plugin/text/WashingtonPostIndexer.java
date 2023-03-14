package de.swa.gmaf.plugin.text;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import de.swa.gmaf.extensions.defaults.GeneralDictionary;
import de.swa.gmaf.extensions.defaults.Word;
import de.swa.gmaf.plugin.GMAF_Plugin;
import de.swa.gmaf.plugin.text.extension.FileExtensionFactory;
import de.swa.gmaf.plugin.text.extension.FileExtensionPlugin;
import de.swa.mmfg.CompositionRelationship;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.Node;

/** GMAF Plugin for the Washington Post archive algorithm for texts **/

public class WashingtonPostIndexer implements GMAF_Plugin {
	Vector<Node> nodes = new Vector<Node>();

	public WashingtonPostIndexer() {
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
		if (source != null && target != null) {
			Node n = new Node(source, fv);
			n.setDetectedBy(getClass().toString());
			Node ntarget = new Node(target, fv);
			n.addChildNode(ntarget);

			nodes.add(n);
			nodes.add(ntarget);
			fv.getCurrentNode().addChildNode(n);
		}
	}
	
	public static Vector<String> additionalSentences = new Vector<String>();

	public void process(URL url, File f, byte[] bytes, MMFG fv) {
		if (f.exists()) {
			FileExtensionPlugin fep = FileExtensionFactory.getInstance().getPluginFor(f);
			if (fep == null) {
				System.out.println("no plugin for " + f.getAbsolutePath());
				return;
			}
			fep.process();

			Hashtable<String, String> attributes = fep.getKeywords();
			for (String key : attributes.keySet()) {
				String val = attributes.get(key);
				if (key.equals("title")) {
					Node n = new Node("Title", fv);
					addSentenceToMMFG(val, fv, n);
					fv.getCurrentNode().addChildNode(n);
				}
				else {
					process(key, val, fv);
				}
			}
			Vector<String> sentences = fep.getSentences();
			int count = 0;
			for (String sentence : sentences) {
				count++;
				Node n = new Node("Sentence_" + count, fv);
				addSentenceToMMFG(sentence, fv, n);
				fv.getCurrentNode().addChildNode(n);
			}
			for (String sentence : additionalSentences) {
				count++;
				Node n = new Node("Sentence_" + count, fv);
				addSentenceToMMFG(sentence, fv, n);
				fv.getCurrentNode().addChildNode(n);
			}
		}
	}

	private void addSentenceToMMFG(String sentence, MMFG fv, Node current) {
		TFIDFCalculator tfidf = TFIDFCalculator.getInstance("wapo", 0.9f, 0.75f, true);
//		System.out.println("loaded relevance data with " + tfidf.getRelevantWords().size() + " words.");
//		LogPanel.getCurrentInstance().addToLog("SENTENCE: " + sentence);
		String[] words = sentence.split(" ");

		Vector<String> nouns = new Vector<String>();
		Vector<String> verbs = new Vector<String>();
		Vector<String> adverbs = new Vector<String>();
		Vector<String> adjectives = new Vector<String>();
		Vector<String> otherWords = new Vector<String>();

		for (String word : words) {
			word = word.replace(',', ' ').trim();
			word = word.replace('\'', ' ').trim();
			word = word.replace(':', ' ').trim();
			word = word.replace('"', ' ').trim();
			word = word.replace('(', ' ').trim();
			word = word.replace(')', ' ').trim();
			if (word.startsWith("<"))
				continue;
			if (word.endsWith(">"))
				continue;
			if (word.indexOf("=") > 0)
				continue;

			String wordStem = word;
			try {
				wordStem = GeneralDictionary.getInstance().getWord(word).get(0).getWord();
			} catch (Exception x) {
			}

			if (tfidf.getRelevantWords().contains(wordStem)) {
				Vector<Word> ws = GeneralDictionary.getInstance().getWord(wordStem);
				if (ws.size() != 0) {
					Word w = ws.get(0);
					if (w.getType() == Word.TYPE_NOUN)
						nouns.add(wordStem);
					else if (w.getType() == Word.TYPE_ADVERB)
						adverbs.add(wordStem);
					else if (w.getType() == Word.TYPE_ADJECTIVE)
						adjectives.add(wordStem);
					else if (w.getType() == Word.TYPE_VERB)
						verbs.add(wordStem);
					else
						otherWords.add(wordStem);
				}
			}
		}

		for (int ix = 0; ix < nouns.size(); ix++) {
			String noun = nouns.get(ix);
			Node n = new Node(noun, fv);
			for (int j = ix; j < nouns.size(); j++) {
				String otherNoun = nouns.get(j);
				Node otherNounNode = new Node(otherNoun, fv);
				nodes.add(otherNounNode);
				n.addCompositionRelationship(
						new CompositionRelationship(CompositionRelationship.RELATION_RELATED_TO, otherNounNode));
			}
			for (String v : verbs) {
				Node verbNode = new Node(v, fv);
				nodes.add(verbNode);
				n.addCompositionRelationship(
						new CompositionRelationship(CompositionRelationship.RELATION_DOING, verbNode));
			}
			for (String a : adverbs) {
				Node adverbNode = new Node(a, fv);
				nodes.add(adverbNode);
				n.addCompositionRelationship(
						new CompositionRelationship(CompositionRelationship.RELATION_PROPERTY, adverbNode));
			}
			for (String a : adjectives) {
				Node adjectiveNode = new Node(a, fv);
				nodes.add(adjectiveNode);
				n.addCompositionRelationship(
						new CompositionRelationship(CompositionRelationship.RELATION_DESCRIPTION, adjectiveNode));
			}
			for (String s : otherWords) {
				Node otherWord = new Node(s, fv);
				nodes.add(otherWord);
				n.addChildNode(otherWord);
			}
			current.addChildNode(n);
			nodes.add(n);
		}
	}

	public boolean providesRecoursiveData() {
		return false;
	}

}
