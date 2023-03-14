package de.swa.gmaf.plugin.text.extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/** implementation of Microsoft Word .DOC format based on Apache POI library **/
public class DOCX_ExtensionPlugin implements FileExtensionPlugin {
	private File f;

	public boolean canProcess(File f) {
		this.f = f;
		if (f.getName().endsWith("docx")) return true;
		if (f.getName().endsWith("doc")) return true;
		return false;
	}

	private Vector<File> additionalFiles;
	public Vector<File> getAdditionalAssets() {
		return new Vector<File>();
	}

	public Vector<String> getExtensions() {
		Vector<String> extensions = new Vector<String>();
		extensions.add(".doc");
		extensions.add(".docx");
		return extensions;
	}

	private Hashtable<String, String> attributes = new Hashtable<String, String>();
	private Vector<String> sentences = new Vector<String>();

	public void process() {
		attributes.clear();
		sentences.clear();
		additionalFiles = new Vector<File>();

		try {
			FileInputStream fis = new FileInputStream(f);
			XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(fis));
			XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
			// Text part
			String content = extractor.getText();

			StringTokenizer st = new StringTokenizer(content.toString(), ".");
			while (st.hasMoreTokens()) {
				String sentence = st.nextToken();
				sentences.add(sentence);
			}
			// Multimedia part
			ArrayList<PackagePart> parts = extractor.getPackage().getParts();
			for (PackagePart pp : parts) {
				try {
					File fPart = new File("GMAF_Part_" + f.getName() + "_" + pp.getPartName());
					FileOutputStream fout = new FileOutputStream(fPart);
					fout.write(pp.getInputStream().readAllBytes());
					fout.close();
					additionalFiles.add(f);
				}
				catch (Exception x) {
					x.printStackTrace();
				}
			}
			
			extractor.close();
		} catch (Exception x) {
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
