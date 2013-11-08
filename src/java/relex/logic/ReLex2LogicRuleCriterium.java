package relex.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

// Description: Stores a criterium for a ReLex2LogicRule
public class ReLex2LogicRuleCriterium {
	
	// The label of the criterium, subj, preadj, etc.
	private String _criteriumLabel;
	// The variables contained in the criterium
	private List<String> _variables = new ArrayList<String>();
	// Set to keep track of the values that criteria variables have been matched to.
	private HashMap<String, String> _variableValues = new HashMap<String, String>();
	// The original criterium string
	private String _criteriumString;
	
	// Summary: Constructor for a criterium, used by ReLex2LogicRule.
	public ReLex2LogicRuleCriterium(String criterium)
	{
		_criteriumString = criterium;
				
		_criteriumLabel = criterium.substring(0, criterium.indexOf("("));
		
		String criteriumVariables[] = criterium.substring(criterium.indexOf("(") + 1).replace(" ", "").replace(")", "").split(",");
		
		_variables = Arrays.asList(criteriumVariables);
	}
	
	// Summary: Returns the label of a criterium, so for subj($y, $x) it would return subj.
	public String getCriteriumLabel()
	{
		return _criteriumLabel;
	}
	
	// Summary: Returns a boolean indicating whether all variables have been satisfied. A similar function exists in ReLex2LogicRule which calls the function below
	// on all its criteria.
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
	
	// Summary: Return the full string the criterium was constructed with, for example subj($y, $x)
	public String getCriteriumString()
	{
		return _criteriumString;
	}
	
	// Summary: Returns a list of the variables, so for subj($y, $x) it would return a list with $y and $x inside it.
	public List<String> getVariables()
	{
		return _variables;
	}
	
	// Summary: Returns the name of the first variable in the criteria, so for subj($y, $x) it would return $y.
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
	
	// Summary: Returns the name of the second variable in the criteria, so for subj($y, $x) it would return $y.	
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
	
	// Summary: Set's a value for a variable, once it is found in the dependency graph. So for  subj($y, $x) it may set $y to 'like'.
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
	
	// Summary: Returns the value of a variable, used for retrieving variable values when writing the output of a rule. 
	public String getVariableValue(String variableName)
	{
		String variableValue = "";
		
		if (_variableValues.containsKey(variableName))
			variableValue = _variableValues.get(variableName);
		
		return variableValue;
	}
}
