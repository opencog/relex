package relex.output;

import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class RegexMatcher extends BaseMatcher<String> {
	private final Pattern regex;

	public RegexMatcher(String regex) {
		this.regex = Pattern.compile(regex);
	}

	public RegexMatcher(Pattern regex) {
		this.regex = regex;
	}

	public boolean matches(Object o) {
		return regex.matcher((String) o).matches();
	}

	public void describeTo(Description description) {
		description.appendText("matches " + regex);
	}

	public static RegexMatcher matches(String regex) {
		return new RegexMatcher(regex);
	}
	
	public static RegexMatcher matches(Pattern regex) {
		return new RegexMatcher(regex);
	}
	
}
