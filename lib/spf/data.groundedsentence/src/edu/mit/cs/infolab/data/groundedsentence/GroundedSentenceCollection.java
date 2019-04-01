package edu.mit.cs.infolab.data.groundedsentence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.cornell.cs.nlp.spf.base.exceptions.FileReadingException;
import edu.cornell.cs.nlp.spf.base.properties.Properties;
import edu.cornell.cs.nlp.spf.base.string.IStringFilter;
import edu.cornell.cs.nlp.spf.base.string.StubStringFilter;
import edu.cornell.cs.nlp.spf.data.collection.IDataCollection;
import edu.cornell.cs.nlp.spf.data.sentence.ITokenizer;
import edu.cornell.cs.nlp.spf.data.sentence.Sentence;
import edu.cornell.cs.nlp.spf.explat.IResourceRepository;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment.Parameters;
import edu.cornell.cs.nlp.spf.explat.resources.IResourceObjectCreator;
import edu.cornell.cs.nlp.spf.explat.resources.usage.ResourceUsage;

/**
 * Collection of {@link Sentence} objects.
 *
 * @author Yoav Artzi
 */
public class GroundedSentenceCollection implements IDataCollection<Sentence> {
	private static final long serialVersionUID = 8235137906395290242L;
	private final List<Sentence>	entries;

	public GroundedSentenceCollection(List<Sentence> data) {
		this.entries = Collections.unmodifiableList(data);
	}

	public static GroundedSentenceCollection read(File f) {
		return read(f, new StubStringFilter(), null);
	}

	public static GroundedSentenceCollection read(File f, IStringFilter textFilter,
			ITokenizer tokenizer) {
		int readLineCounter = 0;
		try {
			// Open the file
			final List<Sentence> data = new LinkedList<Sentence>();
			try (final BufferedReader in = new BufferedReader(
					new FileReader(f))) {
				String line;
				String currentSentence = null;
				Map<String, String> currentProperties = null;
				
				while ((line = in.readLine()) != null) {
					++readLineCounter;
					if (line.startsWith("//") || line.equals("")) {
						// Case comment or empty line, skip
						continue;
					}
					line = line.trim();
					if (currentSentence == null) {
						// Case we don't have a sentence, so we are supposed to
						// get a sentence.
						currentSentence = textFilter.filter(line);
					} else if (currentProperties == null
							&& Properties.isPropertiesLine(line)) {
						currentProperties = Properties.readProperties(line);
						
						
						final Sentence dataItem;
						dataItem = tokenizer == null
										? new Sentence(currentSentence, currentProperties)
										: new Sentence(currentSentence, currentProperties, tokenizer);
						data.add(dataItem);
								
						currentSentence = null;
						currentProperties = null;
						
					} else {
						throw new IOException("Invalid file format!");
					}
				}
			}
			return new GroundedSentenceCollection(data);
		} catch (final Exception e) {
			// Wrap with dataset exception and throw
			throw new FileReadingException(e, readLineCounter, f.getName());
		}
	}

	public static GroundedSentenceCollection read(File f, ITokenizer tokenizer) {
		return read(f, new StubStringFilter(), tokenizer);
	}

	@Override
	public Iterator<Sentence> iterator() {
		return entries.iterator();
	}

	@Override
	public int size() {
		return entries.size();
	}

	public static class Creator
			implements IResourceObjectCreator<GroundedSentenceCollection> {

		private final String type;

		public Creator() {
			this("data.grounded.sent");
		}

		public Creator(String type) {
			this.type = type;
		}

		@Override
		public GroundedSentenceCollection create(Parameters params,
				IResourceRepository repo) {
			return GroundedSentenceCollection.read(params.getAsFile("file"),
					(IStringFilter) (params.contains("filter")
							? repo.get(params.get("filter"))
							: new StubStringFilter()),
					(ITokenizer) (params.contains("tokenizer")
							? repo.get(params.get("tokenizer")) : null));
		}

		@Override
		public String type() {
			return type;
		}

		@Override
		public ResourceUsage usage() {
			return new ResourceUsage.Builder(type(), GroundedSentenceCollection.class)
					.setDescription("Collection of sentences")
					.addParam("tokenizer", ITokenizer.class,
							"Tokenizer to process the sentence string (default: default tokenizer)")
					.addParam("filter", IStringFilter.class,
							"Filter to process input strings (default: identify filter)")
					.addParam("file", "file",
							"File with sentences. Each line includes a sentence. Empty and comment lines are ignored.")
					.build();
		}

	}

}
