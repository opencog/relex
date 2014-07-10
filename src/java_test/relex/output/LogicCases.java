package relex.output;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;


public class LogicCases {

	private static final Logger log = LoggerFactory
			.getLogger(LogicCases.class);
	private static final String RELEX2LOGIC_TEST_PATH = "/relex2logic.ods";

	protected Iterator<Row> inputIterator(Object... args) {
		URL logicOds = Preconditions.checkNotNull(LogicCases.class.getResource(RELEX2LOGIC_TEST_PATH),
				"Cannot load '%s' from classpath", RELEX2LOGIC_TEST_PATH);
		log.info("Loading '{}'...", logicOds);
		// TODO: Java 7 try-with-resources please
		try {
			SpreadsheetDocument doc = SpreadsheetDocument.loadDocument(logicOds.openStream());
			try {
				Table table = doc.getTableList().get(0);
				boolean firstRow = true;
				final List<Row> realRows = new ArrayList<Row>();
				for (Iterator<Row> iter = table.getRowIterator(); iter.hasNext(); ) {
					Row row = iter.next();
					if (firstRow) {
						firstRow = false;
						continue;
					}
					if (row.getCellByIndex(0).getStringValue().trim().isEmpty()) {
						break;
					}
					realRows.add(row);
				}
				log.info("Got {} out of {} rows in table {}", realRows.size(), table.getRowCount(), table.getTableName());
				return realRows.iterator();
			} finally {
				doc.close();
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot read " + logicOds, e);
		}
	}

	protected Object[] parse(Row row, Object... args) {
		String sentence = row.getCellByIndex(0).getStringValue();
		String expectedLinkGrammar = row.getCellByIndex(1).getStringValue();
		Set<String> expectedBinaries = ImmutableSet.copyOf( Splitter.on("\n").split(row.getCellByIndex(2).getStringValue()) );
		Set<String> expectedUnaries = ImmutableSet.copyOf( Splitter.on("\n").split(row.getCellByIndex(3).getStringValue()) );
		Iterable<String> logicStrs = Splitter.on(Pattern.compile("\n(?=[(])")).split(row.getCellByIndex(4).getStringValue());
		ImmutableSet<Pattern> expectedLogics = FluentIterable.from(logicStrs).transform(new Function<String, Pattern>() {
			public Pattern apply(String input) {
				String regex = input.trim();
				regex = Pattern.compile("( |\n)+", Pattern.MULTILINE).matcher(regex).replaceAll(" ");
				regex = regex.replace("(", "[(]")
					.replace(")", "[)]")
					.replace("+", ".+");
				regex = "^" + regex + "$";
				return Pattern.compile(regex);
			}
		}).toSet();
		log.debug("Test case: \"{}\" grammar={} primary={} attributes={} logics={}",
				sentence, expectedLinkGrammar, expectedBinaries, expectedUnaries, expectedLogics);
		return new Object[] { sentence, expectedLinkGrammar, expectedBinaries, expectedUnaries, expectedLogics };
	}

	public static Object[] provideLogic() {
		final LogicCases cases = new LogicCases();
		return FluentIterable.from(ImmutableList.copyOf(cases.inputIterator()))
				.transform(new Function<Row, Object[]>() {
			public Object[] apply(Row input) {
				return cases.parse(input);
			}
		}).toArray(Object[].class);
	}

}