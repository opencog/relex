/*
 * Copyright 2013 OpenCog Foundation
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
 * 
 * Alex van der Peet <alex.van.der.peet@gmail.com>
 */

package relex.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/** Contains the Criterium details of a Rule.
 * @author      Alex van der Peet <alex.van.der.peet@gmail.com>
 * @version     1.0                 (current version number of program)
 * @since       2013-11-08          (the version of the package this class was first added to)
 */
public class Criterium {
	
	/**
	 * The label of the criterium, subj, preadj, etc.
	 */
	private String _criteriumLabel;
	/**
	 * The variables contained in the criterium
	 */
	private List<String> _variables = new ArrayList<String>();
	/**
	 * Set to keep track of the values that criteria variables have been matched to.
	 */
	private HashMap<String, String> _variableValues = new HashMap<String, String>();
	/**
	 * The original criterium string
	 */
	private String _criteriumString;
	
	/**
	 * Constructor for a criterium, used by ReLex2LogicRule.
	 * @param criterium A criterium string of the form subj($x, $y)
	 */
	public Criterium(String criterium)
	{
		_criteriumString = criterium;
				
		_criteriumLabel = criterium.substring(0, criterium.indexOf("("));
		
		String criteriumVariables[] = criterium.substring(criterium.indexOf("(") + 1).replace(" ", "").replace(")", "").split(",");
		
		_variables = Arrays.asList(criteriumVariables);
	}
	
	/**
	 * Returns the label of a criterium, so for subj($y, $x) it would return subj.
	 * @return The subject of the label.
	 */
	public String getCriteriumLabel()
	{
		return _criteriumLabel;
	}
	
	/**
	 * Checks whether all variables have been set / satisfied. A similar function exists in ReLex2LogicRule which calls the function below on all its criteria.
	 * @return A boolean indicating whether all variables have been satisfied. 
	 */
	public Boolean getAllVariablesSatisfied()
	{
		Boolean allSatisfied = true;
		
		if (_variables.size() == 0)
		{
			allSatisfied = false;
		}
		else
		{
			for (String variable : _variables)
			{
				String variableValue = _variableValues.get(variable);
				
				if (variableValue == null)
					allSatisfied= false;
				
				if (variableValue == "")
					allSatisfied = false;
			}
		}
		
		return allSatisfied;
	}
	
	/**
	 * Returns the full string the criterium was constructed with, for example subj($y, $x)
	 * @return The full string used to construct this Criterium object.
	 */
	public String getCriteriumString()
	{
		return _criteriumString;
	}
	
	/**
	 * Returns a list of the variables, so for subj($y, $x) it would return a list with $y and $x inside it.
	 * @return A list of the variables in this criterium.
	 */
	public List<String> getVariables()
	{
		return _variables;
	}
	
	/**
	 * Returns the name of the first variable in the criterium, so for subj($y, $x) it would return $y.
	 * @return The name of the first variable in the criterium.
	 */
	public String getFirstVariableName()
	{
		if (_variables.size() > 0)
		{
			return _variables.get(0);
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * Returns the name of the second variable in the criterium, so for subj($y, $x) it would return $y.
	 * @return The name of the second variable in the criterium.
	 */
	public String getSecondVariableName()
	{
		if (_variables.size() > 1)
		{
			return _variables.get(1);
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * Sets a value for a variable, once it is found in the dependency graph. So for  subj($y, $x) it may set $y to 'like'.
	 * @param variableName The name of the variable that will have its value set.
	 * @param variableValue The value the variable will be set to.
	 */
	public void setVariableValue(String variableName, String variableValue)
	{
		if (_variableValues.get(variableName) == null)
		{
			_variableValues.put(variableName, variableValue);
		}
		else
		{
			_variableValues.put(variableName, variableValue);
		}
	}
	
	/**
	 * Returns the value of a variable, used for retrieving variable values when writing the output of a rule.
	 * @param variableName The name of the variable for which the value is to be retrieved.
	 * @return
	 */
	public String getVariableValue(String variableName)
	{
		String variableValue = "";
		
		if (_variableValues.containsKey(variableName))
			variableValue = _variableValues.get(variableName);
		
		return variableValue;
	}
}
