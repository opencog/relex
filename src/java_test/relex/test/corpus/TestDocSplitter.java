package relex.test.corpus;

import relex.corpus.DocSplitter;
import relex.corpus.DocSplitterFactory;

@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace"})
public class TestDocSplitter {
  public static void main(String[] args){
    try {
      DocSplitter ds=DocSplitterFactory.create();
      ds.process("");
      System.out.println("Class used: "+ds.getClass().getSimpleName());
      System.out.println("Test passed OK");
    } catch (Throwable e) {
      System.err.println("Test failed");
      e.printStackTrace();
    }
  }
}
