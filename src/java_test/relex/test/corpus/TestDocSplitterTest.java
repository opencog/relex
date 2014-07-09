package relex.test.corpus;

import org.junit.Test;

import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace"})
public class TestDocSplitterTest {
	
	@Test
	public void docSplitter() {
		DocSplitter ds=DocSplitterFactory.create();
		ds.process("");
		System.out.println("Class used: "+ds.getClass().getSimpleName());
		System.out.println("Test passed OK");
	}
	
}
