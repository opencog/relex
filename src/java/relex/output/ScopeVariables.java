/*
 * Copyright 2014 OpenCog Foundation
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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Rodas Solomon <rodisolomon@gmail.com>
 */
public class ScopeVariables
{

	private final  String scopefile = "./data/varscope.txt";
	private ArrayList<String> scope = new ArrayList();

	public ArrayList<String>  loadVarScope()
	{
		File file = new File(scopefile);
		Scanner input = null;
		try {
			input = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (input.hasNext()) {

			String nextLine = input.nextLine();
			if (null == nextLine) break;
			if (!nextLine.equals(""))
				scope.add(nextLine);

		}


		input.close();

		return getScope();
	}

	public  ArrayList<String> getScope()
	{
		ArrayList<String> sVar = new ArrayList<String>();
		String temp[];
		for( String  var: scope) {
			temp = var.substring( (var.indexOf("[") + 1), (var.indexOf("]"))).split(",");
			for( String  y : temp) {
				sVar.add(y);
			}
		}
		return sVar;
	}

}

/* ============================ END OF FILE ====================== */
