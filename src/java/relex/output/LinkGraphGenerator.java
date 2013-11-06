/*
 * Copyright 2008 Novamente LLC
 *
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
package relex.output;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import relex.ParsedSentence;
import relex.feature.FeatureNode;
import relex.feature.FeatureNodeCallback;
import relex.feature.LinkForeach;

/**
 * Generates a graph (using graphviz) of the links produced by link-grammar
 *
 * @author muriloq
 */
public class LinkGraphGenerator {
	public static final boolean HIDE_LEFT_WALL = true;

	public static final String GRAPHVIZ_BIN = System.getProperty("graphviz.bin") != null ? System.getProperty("graphviz.bin") : "/usr/bin";

	public static File generateGraph(
			String title,
			ParsedSentence parse,
			File directory,
			boolean limitSize) {

		PrintWriter writer = null;
		File temp = null;
		try {
			if (directory == null) {
				temp = File.createTempFile("links-", ".dot");
			} else {
				temp = File.createTempFile("links-", ".dot", directory);
			}

			writer = new PrintWriter(temp);
			writer.println("digraph G {");

			String sentence = title.replace("\"", "\\\"");

			if (limitSize) {
				writer.println("\tgraph [ size=\"11,8\", label=\"" + sentence + "\", labelloc=t];");
			} else {
				writer.println("\tgraph [ label=\"" + sentence + "\", labelloc=t];");
			}

			// Avoids LEFT-WALL
			int begin = 0;
			int end = parse.getNumWords();

			if (HIDE_LEFT_WALL){
				begin++;
				end--;
			}
			for(int i=begin; i < end; i++){
				writer.println("\t\"node"+i+"\" [label=\""+parse.getOrigWord(i)+"\"]" );
			}
			LinkCB cb = new LinkCB();
			LinkForeach.foreach(parse.getLeft(), cb);
			writer.println(cb.sb.toString());

			writer.println("\n}");
		} catch (IOException ioe) {
			System.err.println("Error creating dot file.");
		} finally {
			if (writer != null)
				writer.close();
		}
		return temp;
	}

	/**
	 * Generate a graph in the PNG format using the DOT source code in the given
	 * file.
	 *
	 * @param temp The file containing the DOT source code
	 */
	public static File generateGraphImage(File temp) {
		try {
			String command = GRAPHVIZ_BIN + File.separator + "dot" + " -Tpng -o " + temp.getAbsolutePath().concat(".png") + " " + temp.getAbsolutePath();
			System.out.println("Executing " + command + " ...");
			Runtime.getRuntime().exec(command).waitFor();
			System.out.println("OK!");
		} catch (IOException e) {
			System.err.println("Error generating graph " + temp + ".png");
		} catch (InterruptedException e) {
		}
		return new File(temp.getAbsolutePath().concat(".png"));
	}
}


class LinkCB implements FeatureNodeCallback
{
	StringBuilder sb = new StringBuilder();
	public Boolean FNCallback(FeatureNode fn)
	{
		String label = fn.get("LAB").getValue();
		String lIndex = fn.get("F_L").get("index_in_sentence").getValue();
		String rIndex = fn.get("F_R").get("index_in_sentence").getValue();

		if (LinkGraphGenerator.HIDE_LEFT_WALL && lIndex.equals("0")) return false;

		sb.append("\t\"node"+lIndex+"\"");
		sb.append(" -> ");
		sb.append("\t\"node"+rIndex+"\"");
		sb.append(" [label=\"");
		sb.append(label);
		sb.append("\"]\n");

		return false;
	}
};

