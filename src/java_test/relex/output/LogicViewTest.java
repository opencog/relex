package relex.output;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import junitparams.JUnitParamsRunner;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import relex.ParsedSentence;
import relex.RelationExtractor;
import relex.Sentence;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

/**
 * @author ceefour
 *
 */
@RunWith(JUnitParamsRunner.class)
public class LogicViewTest {

	private static final Logger log = LoggerFactory
			.getLogger(LogicViewTest.class);
	

	private static final int max_parses = 1;
	private static final String lang = "en";

	private static RelationExtractor re;
	private static LogicView lv;
	private static OpenCogScheme oc;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpClass() throws Exception {
		re = new RelationExtractor(false);
		re.setLanguage(lang);
		re.setMaxParses(max_parses);
		if (1000 < max_parses) {
			re.setMaxLinkages(max_parses + 100);
		}
		
		lv = new LogicView();
		lv.loadRules();
		
		oc = new OpenCogScheme();
		oc.setShowLinkage(true);
		oc.setShowRelex(true);
		oc.setShowAnaphora(true);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	@junitparams.Parameters(source=LogicCases.class)
	public void printRelationsNew(String sentence,
			String expectedLinkGrammar,
			Set<String> expectedBinaries, Set<String> expectedUnaries,
			Set<Pattern> expectedLogics) {
		System.err.println("Info: sentence: \"" + sentence + "\"");
		Sentence sntc = re.processSentence(sentence);
		assertThat("No parses!", sntc.getParses(), not(empty()));
		int np = Math.min(max_parses, sntc.getParses().size());
		assertThat(np, greaterThan(0));
		
		int pn;
		for (pn = 0; pn < np; pn++) {
			ParsedSentence parse = sntc.getParses().get(pn);
			// Print the phrase string ... handy for debugging.
			log.info("; #{}: {}", pn + 1, parse.getPhraseString());
			
			Set<String> binaries = ImmutableSet.copyOf(Splitter.on("\n").omitEmptyStrings().split(SimpleView.printBinaryRelations(parse)));
			Set<String> unaries = ImmutableSet.copyOf(Splitter.on("\n").omitEmptyStrings().split(SimpleView.printUnaryRelations(parse)));
			log.debug("Binary relations: {}", binaries);
			log.debug("Unary relations: {}", unaries);
			
			oc.setParse(parse);
			String openCogSchemeStr = oc.toString();
			System.out.println("(ListLink (stv 1 1)");
			System.out.println("   (AnchorNode \"# New Parsed Sentence\")");
			System.out.println("   (SentenceNode \"" + sntc.getID() + "\")");
			System.out.println(")\n");
			
			final List<String> logics = Splitter.on("\n").omitEmptyStrings().splitToList(lv.printRelationsNew(parse));
			log.info("Logic relations: {}", logics);
			
			assertThat(binaries, equalTo(expectedBinaries));
			assertThat(unaries, equalTo(expectedUnaries));
			Set<Matcher<? super String>> logicMatchers = FluentIterable.from(expectedLogics)
					.transform(new Function<Pattern, Matcher<? super String>>() {
				public Matcher<? super String> apply(Pattern input) {
					return RegexMatcher.matches(input);
				}
			}).toSet();
			assertThat("Logic relations", logics, Matchers.containsInAnyOrder(logicMatchers));
			assertThat("OpenCog Scheme relations is empty", openCogSchemeStr.trim(), not(Matchers.isEmptyString()));
		}
	}

}
