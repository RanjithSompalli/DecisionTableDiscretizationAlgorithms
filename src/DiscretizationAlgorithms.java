import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;


public class DiscretizationAlgorithms 
{
	static Map<String,ArrayList<String>> attributeConceptValuePairs = new LinkedHashMap<String,ArrayList<String>>();
	static Map<String,ArrayList<Double>> attributeValuePairsInDouble;
	static Map<String,ArrayList<Integer>> conceptValuePairs;
	static Map<String,ArrayList<String>> discretizedTable;

	public static void main(String[] args) 
	{
		String inputFileName=null;
		boolean t = true;
		
		if(t)
		{
			boolean file_exists=false;
			boolean validOption = false;
			Scanner scanner = new Scanner(System.in); 
			while(!file_exists)
			{	
				System.out.println("Please enter the name of your input file:: ");
				inputFileName = scanner.next();
				File file= new File("input//"+inputFileName);
				file_exists = file.exists();
				if(!file_exists)
					System.err.println("No such file exists. Please re enter the file name!! \n ");
			}
			while(!validOption)
			{
				//Select the discretization algorithm
				System.out.println("Please select the Discretization Algorithm:"
						+ "\n a.Equal Interval Width Algorithm"
						+ "\n b.Equal Frequency Per Interval Algorithm"
						+ "\n c.Conditional Entropy Algorithm");
				System.out.println("Please provide your option(enter a or b or c):");

				String algorithmSelected = scanner.next();
				if(algorithmSelected.equalsIgnoreCase("a"))
				{
					validOption = true;
					implementEqualIntervalWidthAlgorithm(inputFileName);
				}
				else if(algorithmSelected.equalsIgnoreCase("b"))
				{
					validOption = true;
					implementEqualFrequencyPerIntervalAlgorithm(inputFileName);
				}
				else if(algorithmSelected.equalsIgnoreCase("c"))
				{
					validOption = true;
					implementConditionalEntropyAlgorithm(inputFileName);
				}
				else
				{
					System.err.println("Invalid Option!!Please provide a Valid Option!!\n");
				}
			}
			
			LERSFileUtility.writeDiscretizedTableInLERSFormat(discretizedTable,conceptValuePairs);
			System.out.println("Table Discretized. Discretized table is written to output//test.data and intermediate steps are written to output//test.init");
			scanner.close();

		}
	}
	
	private static void implementEqualIntervalWidthAlgorithm(String inputFileName)
	{
		System.out.println("Implementing Equal Interval Width Algorithm...");
		try
		{
			//Retrieve the Attribute Value pairs from the given input file.
			attributeConceptValuePairs = LERSFileUtility.readLERSFileData("input//"+inputFileName);
			
			File file = new File("output//test.init");
			if(file.exists())
				file.delete();
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			populateAttributeValuePairsInDouble(attributeConceptValuePairs);
			populateConceptValuePairs(attributeConceptValuePairs);
			
			//start with k=2 and determine the best cut point for each attribute
			int numberOfIntervals = 2; //start with k=2
			boolean isTableConsistent = true;
			
			Map<String,Integer> attributeNumberOfIntervalPairs = new LinkedHashMap<String,Integer>();
			for(Map.Entry<String, ArrayList<Double>> attribute : attributeValuePairsInDouble.entrySet())
			{
				attributeNumberOfIntervalPairs.put(attribute.getKey(),numberOfIntervals);
			}
			//get cut points for each attribute
			Map<String,Double> attributeSelectedCutPointPair = AlgorithmUtility.determineCutPointsForEqualIntervalWidthAlgorithm(attributeValuePairsInDouble);
			// Logic added for the purpose of merging the final dicretized table
			Map<String,ArrayList<Double>> atributeFinalCutPointsPair = new LinkedHashMap<String,ArrayList<Double>>();
			for(Map.Entry<String,Double> attributeSelectedCutPoint : attributeSelectedCutPointPair.entrySet())
			{
				ArrayList<Double> cutPoints = new ArrayList<Double>();
				cutPoints.add(attributeSelectedCutPoint.getValue());
				atributeFinalCutPointsPair.put(attributeSelectedCutPoint.getKey(),cutPoints);
			}
			
			//discretize the table based on the cut point
			discretizedTable = AlgorithmUtility.discretizeTable(attributeSelectedCutPointPair);
			//check if the discretized table is consistent
			isTableConsistent = checkIfTableIsConsistent(discretizedTable);
			
			String worstAttribute="";
			while(!isTableConsistent)
			{
				Map<String,Double> attributeEntropyPair = AlgorithmUtility.calculateEntropy(discretizedTable,conceptValuePairs);
				worstAttribute = AlgorithmUtility.selectWorstAttribute(attributeEntropyPair,attributeNumberOfIntervalPairs);
				ArrayList<Double> bestCutPointsforWorstAttribute = AlgorithmUtility.selectBestCutPointsForWorstAttributeForEqualIntervalWidthAlgorithm(worstAttribute,attributeValuePairsInDouble.get(worstAttribute),attributeNumberOfIntervalPairs.get(worstAttribute));
				atributeFinalCutPointsPair.put(worstAttribute, bestCutPointsforWorstAttribute);
				attributeNumberOfIntervalPairs.put(worstAttribute,attributeNumberOfIntervalPairs.get(worstAttribute)+1);
				discretizedTable = AlgorithmUtility.updateDiscretizedTableBasedOnWorstAttribute(discretizedTable,worstAttribute,bestCutPointsforWorstAttribute);
				isTableConsistent = checkIfTableIsConsistent(discretizedTable);
			}

			discretizedTable = mergeDicretizedTable(discretizedTable,atributeFinalCutPointsPair);
			bw.write("Table is now Consistent. Completed Merging of cut points. Final discretized table written to output//test.data");
			bw.newLine();
			bw.close();
			
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		
	}
	
	private static void implementEqualFrequencyPerIntervalAlgorithm(String inputFileName) 
	{
		System.out.println("Implementing Equal Frequency Per Interval Algorithm...");
		try
		{
			//Retrieve the Attribute Value pairs from the given input file.
			attributeConceptValuePairs = LERSFileUtility.readLERSFileData("input//"+inputFileName);
			
			File file = new File("output//test.init");
			if(file.exists())
				file.delete();
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			populateAttributeValuePairsInDouble(attributeConceptValuePairs);
			populateConceptValuePairs(attributeConceptValuePairs);
			
			//start with k=2 and determine the best cut point for each attribute
			int numberOfIntervals = 2; //start with k=2
			boolean isTableConsistent = true;
			Map<String,Integer> attributeNumberOfIntervalPairs = new LinkedHashMap<String,Integer>();
			for(Map.Entry<String, ArrayList<Double>> attribute : attributeValuePairsInDouble.entrySet())
			{
				attributeNumberOfIntervalPairs.put(attribute.getKey(),numberOfIntervals);
			}
			//get all the possible cut points for each attribute
			Map<String,ArrayList<Double>> attributeCutPointPairs = AlgorithmUtility.determineCutPoints(attributeValuePairsInDouble);
			//select the best cut point for each attribute
			Map<String,Double> attributeSelectedCutPointPair = AlgorithmUtility.selectBestCutPoints(attributeCutPointPairs);
			
			//Logic added for the purpose of merging the final dicretized table
			Map<String,ArrayList<Double>> atributeFinalCutPointsPair = new LinkedHashMap<String,ArrayList<Double>>();
			for(Map.Entry<String,Double> attributeSelectedCutPoint : attributeSelectedCutPointPair.entrySet())
			{
				ArrayList<Double> cutPoints = new ArrayList<Double>();
				cutPoints.add(attributeSelectedCutPoint.getValue());
				atributeFinalCutPointsPair.put(attributeSelectedCutPoint.getKey(),cutPoints);
			}
			
			
			//discretize the table based on the cut point
			discretizedTable = AlgorithmUtility.discretizeTable(attributeSelectedCutPointPair);
			//check if the discretized table is consistent
			isTableConsistent = checkIfTableIsConsistent(discretizedTable);
			
			String worstAttribute="";
			while(!isTableConsistent)
			{
				
				Map<String,Double> attributeEntropyPair = AlgorithmUtility.calculateEntropy(discretizedTable,conceptValuePairs);
				worstAttribute = AlgorithmUtility.selectWorstAttribute(attributeEntropyPair,attributeNumberOfIntervalPairs);
				ArrayList<Double> bestCutPointsforWorstAttribute = AlgorithmUtility.selectBestCutPointsForWorstAttribute(worstAttribute,attributeCutPointPairs.get(worstAttribute),attributeNumberOfIntervalPairs.get(worstAttribute));
				atributeFinalCutPointsPair.put(worstAttribute, bestCutPointsforWorstAttribute);
				attributeNumberOfIntervalPairs.put(worstAttribute,(attributeNumberOfIntervalPairs.get(worstAttribute)+1));
				discretizedTable = AlgorithmUtility.updateDiscretizedTableBasedOnWorstAttribute(discretizedTable,worstAttribute,bestCutPointsforWorstAttribute);
				isTableConsistent = checkIfTableIsConsistent(discretizedTable);
			}

			discretizedTable = mergeDicretizedTable(discretizedTable,atributeFinalCutPointsPair);
			bw.write("Table is now Consistent. Completed Merging of cut points. Final discretized table written to output//test.data");
			bw.newLine();
			bw.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		
	}
	
	
	private static void implementConditionalEntropyAlgorithm(String inputFileName) 
	{
		System.out.println("Implementing Conditional Entropy Algorithm....");
		try
		{
			//Retrieve the Attribute Value pairs from the given input file.
			attributeConceptValuePairs = LERSFileUtility.readLERSFileData("input//"+inputFileName);
			
			File file = new File("output//test.init");
			if(file.exists())
				file.delete();
			file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			populateAttributeValuePairsInDouble(attributeConceptValuePairs);
			populateConceptValuePairs(attributeConceptValuePairs);
			
			//start with k=2 and determine the best cut point for each attribute
			int numberOfIntervals = 2; //start with k=2
			boolean isTableConsistent = true;
			
			Map<String,Integer> attributeNumberOfIntervalPairs = new LinkedHashMap<String,Integer>();
			for(Map.Entry<String, ArrayList<Double>> attribute : attributeValuePairsInDouble.entrySet())
			{
				attributeNumberOfIntervalPairs.put(attribute.getKey(),numberOfIntervals);
			}
			//get all the possible cut points for each attribute
			Map<String,ArrayList<Double>> attributeCutPointPairs = AlgorithmUtility.determineCutPoints(attributeValuePairsInDouble);
			//select the best cut point for each attribute using single scanning
			Map<String,Double> attributeSelectedCutPointPair = AlgorithmUtility.selectBestCutPointsForConditionalEntropyAlgorithm(attributeCutPointPairs,conceptValuePairs);
			
			// Logic added for the purpose of merging the final dicretized table
			Map<String,ArrayList<Double>> atributeFinalCutPointsPair = new LinkedHashMap<String,ArrayList<Double>>();
			for(Map.Entry<String,Double> attributeSelectedCutPoint : attributeSelectedCutPointPair.entrySet())
			{
				ArrayList<Double> cutPoints = new ArrayList<Double>();
				cutPoints.add(attributeSelectedCutPoint.getValue());
				atributeFinalCutPointsPair.put(attributeSelectedCutPoint.getKey(),cutPoints);
			}
			//discretize the table based on the cut point
			discretizedTable = AlgorithmUtility.discretizeTable(attributeSelectedCutPointPair);
			//check if the discretized table is consistent
			isTableConsistent = checkIfTableIsConsistent(discretizedTable);
			// Using equal frequency method to find the final cut points -- Professor asked to use this method with only to perform initial scan with conditional entropy method
			
			String worstAttribute="";
			while(!isTableConsistent)
			{
				
				Map<String,Double> attributeEntropyPair = AlgorithmUtility.calculateEntropy(discretizedTable,conceptValuePairs);
				worstAttribute = AlgorithmUtility.selectWorstAttribute(attributeEntropyPair,attributeNumberOfIntervalPairs);
				ArrayList<Double> bestCutPointsforWorstAttribute = AlgorithmUtility.selectBestCutPointsForWorstAttribute(worstAttribute,attributeCutPointPairs.get(worstAttribute),attributeNumberOfIntervalPairs.get(worstAttribute));
				atributeFinalCutPointsPair.put(worstAttribute, bestCutPointsforWorstAttribute);
				attributeNumberOfIntervalPairs.put(worstAttribute,attributeNumberOfIntervalPairs.get(worstAttribute)+1);
				discretizedTable = AlgorithmUtility.updateDiscretizedTableBasedOnWorstAttribute(discretizedTable,worstAttribute,bestCutPointsforWorstAttribute);
				isTableConsistent = checkIfTableIsConsistent(discretizedTable);
			}

			discretizedTable = mergeDicretizedTable(discretizedTable,atributeFinalCutPointsPair);
			bw.write("Table is now Consistent. Completed Merging of cut points. Final discretized table written to output//test.data");
			bw.newLine();
			bw.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}
	
	private static boolean checkIfTableIsConsistent(Map<String, ArrayList<String>> discretizedTable) 
	{

		//Determine A* based on latest discretized table
		ArrayList<ArrayList<Integer>> attributeSetValues = new ArrayList<ArrayList<Integer>>();
		Map<String,ArrayList<Integer>> attributeSetValuePairs = new LinkedHashMap<String,ArrayList<Integer>>();
		ArrayList<String> attributeSetStrings = new ArrayList<String>();
		//Iterate it for number of cases to build all the combinations of all the cases.
		for(int i=0;i<discretizedTable.get(discretizedTable.keySet().iterator().next()).size();i++)
		{
			StringBuffer attributeSetString = new StringBuffer("");
			for(Map.Entry<String, ArrayList<String>> attributeValuePair : discretizedTable.entrySet())
			{
				attributeSetString.append(attributeValuePair.getValue().get(i));
			}
			attributeSetStrings.add(attributeSetString.toString());
		}

		int attributeSetPositionCounter =1;
		for(String attributeSet : attributeSetStrings)
		{
			if(!attributeSetValuePairs.containsKey(attributeSet))
			{
				attributeSetValuePairs.put(attributeSet,new ArrayList<Integer>() );
				attributeSetValuePairs.get(attributeSet).add(attributeSetPositionCounter);
			}
			else
				attributeSetValuePairs.get(attributeSet).add(attributeSetPositionCounter);

			attributeSetPositionCounter++;
		}
		for(Entry<String, ArrayList<Integer>> entry : attributeSetValuePairs.entrySet())
		{
			attributeSetValues.add(entry.getValue());
		}

		//Determine d* based on concept vales.
		ArrayList<ArrayList<Integer>> conceptSetValues = new ArrayList<ArrayList<Integer>>();
		if(conceptValuePairs!=null && conceptValuePairs.size() >0)
		{
			for(Entry<String,ArrayList<Integer>> entry : conceptValuePairs.entrySet())
			{
				conceptSetValues.add(entry.getValue());
			}
		}

		return AlgorithmUtility.checkIfTableIsConsistent(attributeSetValues,conceptSetValues);

	}
	
	private static Map<String, ArrayList<String>> mergeDicretizedTable(Map<String, ArrayList<String>> discretizedTable,Map<String, ArrayList<Double>> atributeFinalCutPointsPair)
	{
		for(Map.Entry<String, ArrayList<Double>> attributeCutPoints : atributeFinalCutPointsPair.entrySet())
		{
			ArrayList<Double> cutPoints = attributeCutPoints.getValue();
			ArrayList<Double> tempCutPoints = new ArrayList<Double>();
			tempCutPoints.addAll(cutPoints);
			
			for(Double cutPoint : cutPoints)
			{
				tempCutPoints.remove(cutPoint);
				if(tempCutPoints.size()==0)
				{
					tempCutPoints.add(cutPoint);
					break;
				}
				Map<String,ArrayList<String>> updatedDiscretizedTable = AlgorithmUtility.updateDiscretizedTableBasedOnWorstAttribute(discretizedTable, attributeCutPoints.getKey(), tempCutPoints);
				if(!checkIfTableIsConsistent(updatedDiscretizedTable))
				{
					tempCutPoints.add(cutPoint);
				}
			}
			atributeFinalCutPointsPair.put(attributeCutPoints.getKey(),tempCutPoints);
		}
		
		for(Map.Entry<String, ArrayList<Double>> attributeCutPoints : atributeFinalCutPointsPair.entrySet())
		{
			discretizedTable = AlgorithmUtility.updateDiscretizedTableBasedOnWorstAttribute(discretizedTable, attributeCutPoints.getKey(), attributeCutPoints.getValue());
		}
		return discretizedTable;
	}
	
	//method to populate the attribute value pairs in double values from the given attribute concept value pairs
	public static void populateAttributeValuePairsInDouble(Map<String,ArrayList<String>> attributeValuePairs)
	{
		attributeValuePairsInDouble = new LinkedHashMap<String, ArrayList<Double>>();
		int mapEntryCount = 1;
		for(Map.Entry<String, ArrayList<String>> attributeValuePair : attributeValuePairs.entrySet())
		{
			//This check is to make sure that we are discretizing only attributes and not concept. 
			if(mapEntryCount < attributeValuePairs.size())
			{
				String attributeName = attributeValuePair.getKey();
				attributeValuePairsInDouble.put(attributeName,new ArrayList<Double>());
				ArrayList<String> values = attributeValuePair.getValue();
				for(String value : values)
				{
					attributeValuePairsInDouble.get(attributeName).add(Double.parseDouble(value));
				}	
			}
			mapEntryCount++;
		}
	}
	
	//method to populate the concept value pairs from the given attribute concept value pairs
	public static void populateConceptValuePairs(Map<String,ArrayList<String>> attributeConceptValuePairs)
	{
		conceptValuePairs = new LinkedHashMap<String,ArrayList<Integer>>();

		Set<String> keySet = attributeConceptValuePairs.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		String conceptName = "";
		while(keyIterator.hasNext())
		{
			conceptName = keyIterator.next();
		}
		ArrayList<String> conceptValues = attributeConceptValuePairs.get(conceptName);

		int conceptPositionCounter =1;
		for(String concept : conceptValues)
		{
			if(!conceptValuePairs.containsKey(concept))
			{
				conceptValuePairs.put(concept,new ArrayList<Integer>());
				conceptValuePairs.get(concept).add(conceptPositionCounter);
			}
			else
				conceptValuePairs.get(concept).add(conceptPositionCounter);

			conceptPositionCounter++;
		}
		
	}

}
