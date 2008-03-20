/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package relex.corpus;

// @SuppressWarnings({"CallToPrintStackTrace", "UseOfSystemOutOrSystemErr"})
public class DocSplitterFactory
{
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
	public static DocSplitter create()
	{
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
