package relex.test.corpus;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;

public class DocSplitterTest {

	private static final Logger log = LoggerFactory
			.getLogger(DocSplitterTest.class);

	@Test
	public void docSplitter() {
		DocSplitter ds = DocSplitterFactory.create();
		ds.process("");
		log.info("Class used: {}", ds.getClass().getSimpleName());
		log.info("Test passed OK");
	}

}
