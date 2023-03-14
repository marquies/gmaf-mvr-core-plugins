package de.swa.gmaf.plugin.audio;

import java.io.File;
import java.net.URL;
import java.util.Vector;

import com.google.gson.Gson;

import de.swa.gmaf.plugin.GMAF_Plugin;
import de.swa.mmfg.MMFG;
import de.swa.mmfg.Node;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ShazamPlugin implements GMAF_Plugin {
	public boolean canProcess(String extension) {
		// TODO Auto-generated method stub
		return false;
	}

	Vector<Node> nodes = new Vector<Node>();
	public Vector<Node> getDetectedNodes() {
		return nodes;
	}

	public boolean isGeneralPlugin() {
		return false;
	}

	public void process(URL url, File f, byte[] bytes, MMFG mmfg) {
		try {
			OkHttpClient client = new OkHttpClient();

			RequestBody body = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("file", f.getName(),
					RequestBody.create(MediaType.parse("audio/mpeg"), f))
				.build();

			Request request = new Request.Builder()
				.url("https://shazam-core.p.rapidapi.com/v1/tracks/recognize")
				.post(body)
				.addHeader("X-RapidAPI-Key", "282bc55d1fmshe44adc5330a9cb0p1f2d91jsn482bdecd391a")
				.addHeader("X-RapidAPI-Host", "shazam-core.p.rapidapi.com")
				.build();

			Response response = client.newCall(request).execute();
			
			ResponseBody responseBody = response.body();
			String json = responseBody.string();

			String[] str = json.split(",\n");
			String title = "";
			String artist = "";
			String link = "";
			String lyrics = "";
			String writers = "";
			String image = "";
			String label = "";
			String released = "";
			String album = "";
			String isrc = "";
			String genre = "";
			
			String lastValue = "";
			for (String s : str) {
				s = s.trim();
				if (s.startsWith("\"coverart")) image = extractValue(s);
				if (s.startsWith("\"isrc")) isrc = extractValue(s);
				if (s.startsWith("\"footer")) writers = extractValue(s);
				if (s.startsWith("\"text")) lyrics = extractValue(s);
				if (s.indexOf("\"Label") > 0) label = extractValue(lastValue);
				if (s.indexOf("\"Album") > 0) album = extractValue(lastValue);
				if (s.indexOf("\"Released") > 0) released = extractValue(lastValue);
				if (s.startsWith("\"genre")) genre = extractValue(s);
				if (s.startsWith("\"html")) link = extractValue(s);
				if (s.startsWith("\"title")) title = extractValue(s);
				if (s.startsWith("\"subtitle")) artist = extractValue(s);
				lastValue = s;
			}
			
			System.out.println("Title: " + title);
			System.out.println("Artist: " + artist);
//			System.out.println("Link: " + link);
//			System.out.println("Writers: " + writers);
//			System.out.println("Image: " + image);
//			System.out.println("Label: " + label);
//			System.out.println("Released: " + released);
//			System.out.println("Album: " + album);
//			System.out.println("Isrc: " + isrc);
//			System.out.println("Genre: " + genre);
//			System.out.println("Lyrics: " + lyrics);
			
			Node n = new Node("Music", mmfg);
			n.addChildNode(new Node("Title", title, mmfg));
			n.addChildNode(new Node("Artist", artist, mmfg));
			n.addChildNode(new Node("Link", link, mmfg));
			n.addChildNode(new Node("Writers", writers, mmfg));
			n.addChildNode(new Node("Image", image, mmfg));
			n.addChildNode(new Node("Label", label, mmfg));
			n.addChildNode(new Node("Released", released, mmfg));
			n.addChildNode(new Node("Album", album, mmfg));
			n.addChildNode(new Node("Isrc", isrc, mmfg));
			n.addChildNode(new Node("Genre", genre, mmfg));
			n.addChildNode(new Node("Lyrics", lyrics, mmfg));
			n.addChildNode(new Node("Preview", image, mmfg));
			mmfg.addNode(n);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private String extractValue(String s) {
		if (s.indexOf("genre") > 0) {
			System.out.println("PRIM");
		}
		String[] str = s.split(": ");
		String val = str[1];
		if (val.indexOf("Writer(s)") > 0) val = str[2];
		else if (val.indexOf("metadata") > 0) val = str[3];
		else if (s.indexOf("genre") > 0) val = str[2];
		val = val.replace("\"", " ");
		val = val.replace(",", " ");
		val = val.replace("}", " ");
		val = val.replace("\n", " ");
		val = val.trim();
		return val;
	}

	public boolean providesRecoursiveData() {
		return false;
	}
	
	public static void main(String[] args) {
		ShazamPlugin sp = new ShazamPlugin();
		sp.process(null, new File("/Users/stefan_wagenpfeil/Desktop/output.mp3"), null, null);
	}
}
