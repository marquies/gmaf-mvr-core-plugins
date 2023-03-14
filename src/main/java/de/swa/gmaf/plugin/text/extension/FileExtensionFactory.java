package de.swa.gmaf.plugin.text.extension;

import java.io.File;
import java.util.Vector;

/** factory for File Extensions to support additional document formats 
* 
* @author stefan_wagenpfeil
*/


public class FileExtensionFactory {
	private static FileExtensionFactory instance = null;
	public static FileExtensionFactory getInstance() {
		if (instance == null) instance = new FileExtensionFactory();
		return instance;
	}

	private Vector<FileExtensionPlugin> plugins = new Vector<FileExtensionPlugin>();
	private FileExtensionFactory() {
		plugins.add(new DOCX_ExtensionPlugin());
		plugins.add(new RSS_ExtensionPlugin());
		plugins.add(new TXT_ExtensionPlugin());
		plugins.add(new WAPO_ExtensionPlugin());
	}
	
	public boolean canProcess(String ext) {
		if (ext.startsWith(".")) ext = ext.substring(1, ext.length());
		for (FileExtensionPlugin fep : plugins) {
			for (String ex : fep.getExtensions()) {
				if (ex.equalsIgnoreCase(ext)) return true;
			}
		}
		return false;
	}
	
	public FileExtensionPlugin getPluginFor(File f) {
		for (FileExtensionPlugin fep : plugins) {
			if (fep.canProcess(f)) {
				return fep;
			}
		}
		return null;
	}
}
