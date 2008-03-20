package relex.corpus;

import java.util.Arrays;
import java.util.List;

public class DocSplitterFallbackImpl implements DocSplitter{
  public void setEnglishModelFilename(String emf) {
  }

  public String getEnglishModelFilename() {
    return null;
  }

  public boolean acceptableBreak(String s, int start, int end) {
    return false;
  }

  private StringBuilder sb=new StringBuilder();
  public void addText(String newText) {
    sb.append(newText);
  }

  public void clearBuffer() {
    sb.setLength(0);
  }

  public String getNextSentence() {
    String s = sb.toString();
    clearBuffer();
    return s;
  }

  public List<TextInterval> process(String docText) {
    return Arrays.asList(new TextInterval(0,docText.length()-1));
  }

  public List<String> split(String docText) {
    return Arrays.asList(docText);
  }
}
