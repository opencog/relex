/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package relex.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author opencogdeveloper
 */
public class ScopeVariables
 {
    
    private final  String scopefile="./data/varscope.txt";
    private ArrayList<String> scope =new ArrayList();
    
    public ArrayList<String>  loadVarScope()
        {
           File file = new File(scopefile);
		Scanner input = null;
		try 
                {
			input = new Scanner(file);
		} 
                catch (FileNotFoundException e) 
                {
			
			e.printStackTrace();
		}

		if (input != null)
		{
			

			while (input.hasNext())
			{
				
				String nextLine = input.nextLine();
                               	if (null == nextLine) break;
                                if (!nextLine.equals(""))
                                scope.add(nextLine);

			}
				

			input.close();
                        
    
                }
               
               return getScope();
        }
     
    public  ArrayList<String> getScope()
    { 
        ArrayList<String> sVar= new ArrayList<String>();
        String temp[];
        
        for( String  var:scope)
        {
          
          temp=var.substring( (var.indexOf("[")+1),(var.indexOf("]"))).split(",");
            for( String  y :temp)
            {
                          sVar.add(y);
            }
        }
         return sVar;
      }
   
    
   
}