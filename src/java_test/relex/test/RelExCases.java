package relex.test;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * @author ceefour
 *
 */
public class RelExCases {

	private static final Logger log = LoggerFactory
			.getLogger(RelExCases.class);
	
	static class RelExCase {
		String sentence;
		Set<String> relations;
		Optional<String> description;

		RelExCase(String sentence, Set<String> relations, Optional<String> description) {
			super();
			this.description = description;
			this.sentence = sentence;
			this.relations = relations;
		}
	}
	
	static class RelExCaseToObjectArray implements Function<RelExCase, Object[]> {
		public Object[] apply(RelExCase input) {
			return new Object[] { input.sentence, input.relations, input.description };
		}
	}

	protected static ImmutableList<RelExCase> parseTsv(String casesPath) {
		URL casesTsv = Preconditions.checkNotNull(RelExCases.class.getResource(casesPath),
				"Cannot load '%s' from classpath", casesPath);
		log.info("Loading '{}'...", casesTsv);
		try {
			CSVReader reader = new CSVReader(new InputStreamReader(casesTsv.openStream()),
					'\t', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
			try {
				Optional<String> curDescription = Optional.absent();
				Optional<String> curSentence = Optional.absent();
				ImmutableSet.Builder<String> curRelations = ImmutableSet.builder();
				ImmutableList.Builder<RelExCase> cases = ImmutableList.builder();
				reader.readNext(); // skip header line
				while (true) {
					String[] row = reader.readNext();
					if (row == null) {
						break;
					}
					if (row.length == 0) {
						continue;
					}
					if (row[0].startsWith("//")) {
						// add previous sentence
						if (curSentence.isPresent()) {
							cases.add(new RelExCase(curSentence.get(), curRelations.build(), curDescription));
							curSentence = Optional.absent();
							curRelations = ImmutableSet.builder();
						}
						curDescription = Optional.of(row[0].substring(2).trim());
						continue;
					}
					if (!row[0].trim().isEmpty()) {
						// add previous sentence
						if (curSentence.isPresent()) {
							cases.add(new RelExCase(curSentence.get(), curRelations.build(), curDescription));
							curSentence = Optional.absent();
							curRelations = ImmutableSet.builder();
						}
						// sentence row
						curSentence = Optional.of(row[0].trim());
					} else if (row.length >= 2 && !row[1].trim().isEmpty()) {
						// relation row
						curRelations.add(row[1].trim());
					}
				}
				// add previous sentence
				if (curSentence.isPresent()) {
					cases.add(new RelExCase(curSentence.get(), curRelations.build(), curDescription));
					curSentence = Optional.absent();
					curRelations = ImmutableSet.builder();
				}
				final ImmutableList<RelExCase> caseList = cases.build();
				log.info("Got {} cases from '{}'", caseList.size(), casesPath);
				return caseList;
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot read " + casesTsv, e);
		}
	}

	public static Object[] provideComparatives() {
		return FluentIterable.from(parseTsv("/relex-comparatives.tsv"))
				.transform(new RelExCaseToObjectArray()).toArray(Object[].class);
	}
	
	public static Object[] provideConjunction() {
		return FluentIterable.from(parseTsv("/relex-conjunction.tsv"))
				.transform(new RelExCaseToObjectArray()).toArray(Object[].class);
	}

	public static Object[] provideExtraposition() {
		return FluentIterable.from(parseTsv("/relex-extraposition.tsv"))
				.transform(new RelExCaseToObjectArray()).toArray(Object[].class);
	}

	public static Object[] provideStanfordUntagged() {
		return FluentIterable.from(parseTsv("/stanford-untagged.tsv"))
				.transform(new RelExCaseToObjectArray()).toArray(Object[].class);
	}
	
	public static Object[] provideStanfordTagged() {
		return FluentIterable.from(parseTsv("/stanford-tagged.tsv"))
				.transform(new RelExCaseToObjectArray()).toArray(Object[].class);
	}

}
