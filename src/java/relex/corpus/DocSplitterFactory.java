package relex.corpus;

@SuppressWarnings({"CallToPrintStackTrace", "UseOfSystemOutOrSystemErr"})
public class DocSplitterFactory{
  private static final Class<? extends DocSplitter> clazz;
  static{
    Class<? extends DocSplitter> clazz0;
    try
    {
      Class.forName("opennlp.tools.sentdetect.EnglishSentenceDetectorME");
      clazz0=DocSplitterOpenNLPImpl.class;
    }
    catch(Throwable t)
    {
      System.err.println(
        "\nWARNING:\n" +
        "\tIt appears the the OpenNLP tools are not installed\n" +
        "\tor are not correctly specified in the java classpath.\n" +
        "\tThe OpenNLP tools are used to perform sentence detection,\n" +
        "\tand RelEx will have trouble handling multiple sentences.\n" +
        "\tPlease see the README file for install info.\n");
      clazz0=DocSplitterFallbackImpl.class;
    }
    clazz=clazz0;

  }
  public static DocSplitter create() {
    try {
      return clazz.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}