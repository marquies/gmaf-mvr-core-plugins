package de.swa.gmaf.plugin.text.extension;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

/** interface to define file extensions **/
public interface FileExtensionPlugin {
	/** returns, if a plugin can process a given file **/
	public boolean canProcess(File f);
	/** returns a vector of sentences as splitted text **/
	public Vector<String> getSentences();
	/** returns the hashtable of keywords of a given text **/
	public Hashtable<String, String> getKeywords();
	/** returns a list of additional assets, e.g. embedded images for further processing **/
	public Vector<File> getAdditionalAssets();
	/** returns the list of supported extensions **/
	public Vector<String> getExtensions();
	/** processes a the file **/
	public void process();
}
