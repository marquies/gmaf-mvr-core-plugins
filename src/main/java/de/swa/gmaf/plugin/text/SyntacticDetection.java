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
import de.swa.ui.Logger;

/** GMAF Plugin for the Syntactic Detection algorithm for texts **/

public class SyntacticDetection implements GMAF_Plugin {
	Vector<Node> nodes = new Vector<Node>();
	Vector<String> blacklist = new Vector<String>();

	public SyntacticDetection() {
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
			if (fep == null) {
				System.out.println("no extension for " + f);
				return;
			}
			fep.process();

			Hashtable<String, String> attributes = fep.getKeywords();
			for (String key : attributes.keySet()) {
				String val = attributes.get(key);
				process(key, val, fv);
			}
			Vector<String> sentences = fep.getSentences();

			int i = 0;
			Node contentNode = new Node("content", fv);
			nodes.add(contentNode);
			for (String sentence : sentences) {
				i++;
				Logger.getInstance().log("DETECTED: " + sentence);
				String[] words = sentence.split(" ");

				Vector<String> nouns = new Vector<String>();
				Vector<String> verbs = new Vector<String>();
				Vector<String> adverbs = new Vector<String>();
				Vector<String> adjectives = new Vector<String>();
				Vector<String> otherWords = new Vector<String>();

				for (String word : words) {
					Vector<Word> ws = GeneralDictionary.getInstance().getWord(word);
					if (ws.size() != 0) {
						Word w = ws.get(0);
						if (w.getType() == Word.TYPE_NOUN)
							nouns.add(word);
						else if (w.getType() == Word.TYPE_ADVERB)
							adverbs.add(word);
						else if (w.getType() == Word.TYPE_ADJECTIVE)
							adjectives.add(word);
						else if (w.getType() == Word.TYPE_VERB)
							verbs.add(word);
						else
							otherWords.add(word);
					}
				}

				for (int ix = 0; ix < nouns.size(); ix++) {
					String noun = nouns.get(ix);
					Node n = new Node(noun, fv);
					for (int j = ix; j < nouns.size(); j++) {
						String otherNoun = nouns.get(j);
						Node otherNounNode = new Node(otherNoun, fv);
						nodes.add(otherNounNode);
						n.addCompositionRelationship(new CompositionRelationship(
								CompositionRelationship.RELATION_RELATED_TO, otherNounNode));
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
						n.addCompositionRelationship(new CompositionRelationship(
								CompositionRelationship.RELATION_DESCRIPTION, adjectiveNode));
					}
					for (String s : otherWords) {
						Node otherWord = new Node(s, fv);
						nodes.add(otherWord);
						n.addChildNode(otherWord);
					}
					contentNode.addChildNode(n);
				}
			}
		}
	}

	public boolean providesRecoursiveData() {
		return false;
	}
}
