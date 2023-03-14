package de.swa.gmaf.plugin.audio;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Vector;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;

import de.swa.gmaf.plugin.text.extension.FileExtensionPlugin;

public class AudioTranscriber implements FileExtensionPlugin {
	private File file;

	public boolean canProcess(File f) {
		String extension = f.getName();
		String ext = extension.toUpperCase();
		file = f;
		if (ext.endsWith("WAV") || ext.endsWith("MP3") || ext.endsWith("AIFF") || ext.endsWith("M4A"))
			return true;
		return false;
	}

	public Vector<File> getAdditionalAssets() {
		return new Vector<File>();
	}

	public Vector<String> getExtensions() {
		Vector<String> ext = new Vector<String>();
		ext.add(".wav");
		ext.add(".mp3");
		ext.add(".aiff");
		ext.add(".m4a");
		return ext;
	}

	private Hashtable<String, String> attributes = new Hashtable<String, String>();
	private Vector<String> sentences = new Vector<String>();

	public Hashtable<String, String> getKeywords() {
		return attributes;
	}

	public Vector<String> getSentences() {
		return sentences;
	}

	public void process() {
		attributes.clear();
		sentences.clear();

		try (SpeechClient speech = SpeechClient.create()) {
			Path path = file.toPath();
			byte[] content = Files.readAllBytes(path);

			// Configure request with video media type
			RecognitionConfig recConfig = RecognitionConfig.newBuilder()
					// encoding may either be omitted or must match the value in the file header
					.setEncoding(AudioEncoding.LINEAR16).setLanguageCode("en-US")
					// sample rate hertz may be either be omitted or must match the value in the
					// file
					// header
					.setSampleRateHertz(16000).setModel("video").build();

			RecognitionAudio recognitionAudio = RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content))
					.build();

			RecognizeResponse recognizeResponse = speech.recognize(recConfig, recognitionAudio);
			// Just print the first result here.
			SpeechRecognitionResult result = recognizeResponse.getResultsList().get(0);
			String[] st = result.toString().split(".");
			for (String s : st) sentences.add(s);
			// There can be several alternative transcripts for a given chunk of speech.
			// Just use the
			// first (most likely) one here.
//			SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
//			System.out.printf("Transcript : %s\n", alternative.getTranscript());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
