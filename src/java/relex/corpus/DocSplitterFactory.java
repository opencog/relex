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

@SuppressWarnings("unchecked")

public class DocSplitterFactory
{
	private static final Class<? extends DocSplitter> clazz;
	static {
		Class<? extends DocSplitter> clazz0;
		clazz0 = null;

		// First, try to load opennlp-1.5.0
		try
		{
			// Earlier versions of opennlp don't have this class,
			// and so we'll throw an error if its not found.
			Class.forName("opennlp.tools.sentdetect.SentenceModel");

			// clazz0 = DocSplitterOpenNLP15Impl.class;
			Class<?> c = Class.forName("relex.corpus.DocSplitterOpenNLP15Impl");

			// It seems to be impossible to perform this cast and not get
			// a type-safety warning,
			clazz0 = (Class<DocSplitter>) c;
		}
		catch(Throwable t) {}

		if (null == clazz0)
		{
			clazz0 = DocSplitterFallbackImpl.class;
			System.err.println(
				"\nWARNING:\n" +
				"\tIt appears that the OpenNLP tools are not installed or are not\n" +
				"\tcorrectly specified in the java classpath. The OpenNLP tools are\n" +
				"\tused to perform sentence detection. Without them, ReleEx will use\n" +
				"\tthe less accurate Java sentence detector. See the README file for info.\n");
		}

		clazz = clazz0;
	}

	public static DocSplitter create()
	{
		try {
			DocSplitter ds =  clazz.newInstance();
			if (false == ds.operational())
			{
				System.err.println(
					"\nWARNING:\n" +
					"\tIt appears that the DocSplitter class is not working for some reason.\n" +
					"\tMake sure that data/opennlp/models-1.5/en-sent.bin is installed\n" +
					"\tor that an alternate location is specified with -DEnglishModelFilename\n" +
					"\tin the script/run files.\n" +
					"\tWithout it, ReleEx will use the less-accurate Java native sentence detector.\n" +
					"\tPlease see the README file for info.\n");
				Class<? extends DocSplitter> clazzy;
				clazzy = DocSplitterFallbackImpl.class;
				ds =  clazzy.newInstance();
			}
			return ds;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
