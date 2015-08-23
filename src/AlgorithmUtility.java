import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

public class AlgorithmUtility
{
	//stores the attribute values pairs with attribute values in double and will be accessible across the class.
	static Map<String, ArrayList<Double>> attributeValuePairsInDouble_Global = new LinkedHashMap<String, ArrayList<Double>>();

	//Determines all the possible cut points for the given attribute value pairs
	public static Map<String,ArrayList<Double>> determineCutPoints(Map<String,ArrayList<Double>> attributeValuePairsInDouble)
	{
		try 
		{
			File file = new File("output/test.init");
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Possible Cut Points for Each Attribute with k=2:::");
			bw.newLine();

			attributeValuePairsInDouble_Global.putAll(attributeValuePairsInDouble);
			Map<String,ArrayList<Double>> attributeCutPointPairs = new LinkedHashMap<String,ArrayList<Double>>();

			for(Map.Entry<String, ArrayList<Double>> attributeValuePair : attributeValuePairsInDouble.entrySet())
			{
				String attributeName = attributeValuePair.getKey();
				attributeCutPointPairs.put(attributeName, new ArrayList<Double>());
				ArrayList<Double> values = attributeValuePair.getValue();
				for(Double value : values)
				{
					if(!attributeCutPointPairs.get(attributeName).contains(value))
						attributeCutPointPairs.get(attributeName).add(value);
				}	
				Collections.sort(attributeCutPointPairs.get(attributeName));
			}

			for(Map.Entry<String, ArrayList<Double>> attributeCutPointPair : attributeCutPointPairs.entrySet())
			{
				String attributeName = attributeCutPointPair.getKey();
				ArrayList<Double> nonDuplicateAttributeValues = attributeCutPointPair.getValue();
				ArrayList<Double> cutPoints = new ArrayList<Double> ();
				for(int i=0;i<nonDuplicateAttributeValues.size()-1;i++)
				{
					double cutpoint = (nonDuplicateAttributeValues.get(i)+nonDuplicateAttributeValues.get(i+1))/2;
					cutPoints.add(cutpoint);
				}

				attributeCutPointPairs.get(attributeName).addAll(cutPoints);
				attributeCutPointPairs.get(attributeName).retainAll(cutPoints);
			}

			//Logic to write data file
			for(Map.Entry<String, ArrayList<Double>> attributeCutPointPair : attributeCutPointPairs.entrySet())
			{
				StringBuilder dataLine = new StringBuilder("Possible CutPoints for attribute "+attributeCutPointPair.getKey()+": ");
				for(Double cutPoint : attributeCutPointPair.getValue())
				{
					dataLine.append(cutPoint+" ");
				}
				bw.write(dataLine.toString());
				bw.newLine();
			}
			bw.close();

			return attributeCutPointPairs;
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	//Determines the best possible cut point for each attribute. This is initially when k=2.
	public static  Map<String,Double> selectBestCutPoints(Map<String,ArrayList<Double>> attributeCutPointPairs)
	{
		try 
		{
			File file = new File("output/test.init");
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Best Cut Point for Each Attribute:::");
			bw.newLine();


			Map<String,Double> attributeSelectedCutPointPair = new LinkedHashMap<String,Double>();
			for(Map.Entry<String, ArrayList<Double>> attributeCutPointPair : attributeCutPointPairs.entrySet())
			{
				String attributeName = attributeCutPointPair.getKey();
				ArrayList<Double> cutPoints = attributeCutPointPair.getValue();
				ArrayList<Double> actualValues = attributeValuePairsInDouble_Global.get(attributeName);
				int minDiff = actualValues.size();
				Double selectedCutPoint = cutPoints.get(0);
				//Determine cut point ranges based on number of intervals
				for(Double cutPoint : cutPoints)
				{
					int bin1 =0;
					int bin2 =0;
					for(int i=0;i<actualValues.size();i++)
					{
						if(actualValues.get(i)<cutPoint)
							bin1++;
						else
							bin2++;
					}
					int diff = bin2-bin1;
					if(diff<0)
						diff = -(diff);
					if(diff<minDiff)
					{
						minDiff = diff;
						selectedCutPoint = cutPoint;
					}
				}
				attributeSelectedCutPointPair.put(attributeName,selectedCutPoint);
			}

			//Logic to write data file
			for(Map.Entry<String, Double> attributeCutPointPair : attributeSelectedCutPointPair.entrySet())
			{
				StringBuilder dataLine = new StringBuilder("Selected CutPoint for attribute "+attributeCutPointPair.getKey()+": ");
				dataLine.append(attributeCutPointPair.getValue()+" ");
				bw.write(dataLine.toString());
				bw.newLine();
			}
			bw.close();

			return attributeSelectedCutPointPair;
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	//Determines the best possible cut point for the worst attribute selected based on average block entropy. numberOfIntervals gives the value of K.
	public static ArrayList<Double> selectBestCutPointsForWorstAttribute(String worstAttribute,ArrayList<Double> cutPointsOfWorstAttribute,int numberofIntervals)
	{

		FileWriter fw = null;
		BufferedWriter bw = null;
		try 
		{
			File file = new File("output/test.init");
			if(!file.exists())
				file.createNewFile();
			fw = new FileWriter(file.getAbsoluteFile(),true);
			bw = new BufferedWriter(fw);
			bw.write("All Possible Cut Point Pairs for Selected Worst Attribute with k="+(numberofIntervals+1)+": ");
			bw.newLine();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		/*Logic added for defect fix for attributes with single cut point*/
		if(cutPointsOfWorstAttribute.size()==1)
		{
			return cutPointsOfWorstAttribute;
		}
		/*Logic for defect fix ends */

		/* Logic to select all combinations of cut points*/
		List<Double> dataList = new ArrayList<Double>();
		ArrayList<ArrayList<Double>> returnValues = new ArrayList<ArrayList<Double>>();
		MathematicsUtility.combinationUtil(cutPointsOfWorstAttribute,returnValues,dataList,0,cutPointsOfWorstAttribute.size()-1,0,numberofIntervals);

		//Logic to write data file
		for(ArrayList<Double> returnValue : returnValues)
		{
			StringBuilder dataLine = new StringBuilder("");
			for(Double value : returnValue)
				dataLine.append(value+" ");
			try{
				bw.write(dataLine.toString());
				bw.newLine();}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		for(ArrayList<Double> returnValue : returnValues)
		{
			int bin[] = new int[returnValue.size()+1];
			ArrayList<Double> actualValues = attributeValuePairsInDouble_Global.get(worstAttribute);
			for(Double value:actualValues)
			{
				if(value<returnValue.get(0))
					bin[0]++;
				else if(value>returnValue.get(returnValue.size()-1))
					bin[returnValue.size()]++;
				else
				{
					for(int i=0;i<returnValue.size();i++)
					{
						if(value<returnValue.get(i) && value>returnValue.get(i-1))
							bin[i]++;
					}
				}
			}
			int meanValue = actualValues.size()/bin.length;
			boolean isBestCutPoint = false;
			for(int i=0;i<bin.length;i++)
			{
				if(bin[i]<=meanValue)
					isBestCutPoint = true;
				else
				{
					isBestCutPoint = false;
					break;
				}
			}
			if(isBestCutPoint)
			{
				try
				{
					bw.write("Best Cut Point Pair for Selected Worst Attribute with k="+(numberofIntervals+1)+": ");
					for(Double value:returnValue)
						bw.append(value+" ");
					bw.newLine();
					bw.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				return returnValue;
			}
		}
		try
		{
			bw.write("Best Cut Point Pair for Selected Worst Attribute with k="+(numberofIntervals+1)+": ");
			for(Double value:returnValues.get(0))
				bw.append(value+" ");
			bw.newLine();
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}


		return returnValues.get(0);
	}

	//Returns the discretized table based on the given cut point
	public static  Map<String,ArrayList<String>> discretizeTable(Map<String,Double> attributeSelectedCutPointPair)
	{
		Map<String,ArrayList<String>> dicretizedTable = new LinkedHashMap<String,ArrayList<String>>();
		for(Map.Entry<String,Double> attributeSelectedCutPoint : attributeSelectedCutPointPair.entrySet())
		{
			String attributeName = attributeSelectedCutPoint.getKey();
			Double cutPoint = attributeSelectedCutPoint.getValue();
			dicretizedTable.put(attributeName,new ArrayList<String>());
			ArrayList<Double> sortedAttributeValuePairs = new ArrayList<Double>();
			sortedAttributeValuePairs.addAll(attributeValuePairsInDouble_Global.get(attributeName));
			Collections.sort(sortedAttributeValuePairs);

			for(Double value : attributeValuePairsInDouble_Global.get(attributeName))
			{
				String discretizedValue = "";
				if(value<cutPoint)
				{
					discretizedValue = sortedAttributeValuePairs.get(0)+".."+cutPoint;
					dicretizedTable.get(attributeName).add(discretizedValue);
				}
				else
				{
					discretizedValue = cutPoint+".."+sortedAttributeValuePairs.get(sortedAttributeValuePairs.size()-1);
					dicretizedTable.get(attributeName).add(discretizedValue);
				}
			}
		}
		return dicretizedTable;
	}

	public static Map<String,ArrayList<String>> updateDiscretizedTableBasedOnWorstAttribute(Map<String,ArrayList<String>> dicretizedTable,String worstAttribute,ArrayList<Double> selectedCutPoints)
	{
		ArrayList<Double> sortedAttributeValuePairs = new ArrayList<Double>();
		sortedAttributeValuePairs.addAll(attributeValuePairsInDouble_Global.get(worstAttribute));
		Collections.sort(sortedAttributeValuePairs);
		dicretizedTable.put(worstAttribute, new ArrayList<String>());
		for(Double value : attributeValuePairsInDouble_Global.get(worstAttribute))
		{
			String discretizedValue = "";
			if(value <= selectedCutPoints.get(0))
			{
				discretizedValue = sortedAttributeValuePairs.get(0)+".."+selectedCutPoints.get(0);
				dicretizedTable.get(worstAttribute).add(discretizedValue);
			}
			else if(value >= selectedCutPoints.get(selectedCutPoints.size()-1))
			{
				discretizedValue = selectedCutPoints.get(selectedCutPoints.size()-1)+".."+sortedAttributeValuePairs.get(sortedAttributeValuePairs.size()-1);
				dicretizedTable.get(worstAttribute).add(discretizedValue);
			}
			else
			{
				for(int i=0;i<selectedCutPoints.size();i++)
				{
					if(value<selectedCutPoints.get(i) && value>=selectedCutPoints.get(i-1))
					{
						discretizedValue = selectedCutPoints.get(i-1)+".."+selectedCutPoints.get(i);
						dicretizedTable.get(worstAttribute).add(discretizedValue);
					}

				}
			}
		}
		return dicretizedTable;
	}
	//Checks if table is consistent based on the attribute and concept set values provided.
	public static boolean checkIfTableIsConsistent(ArrayList<ArrayList<Integer>> attributeSetValues,ArrayList<ArrayList<Integer>> conceptSetValues) 
	{
		boolean isConsistent = true;
		for(ArrayList<Integer> attributeSet : attributeSetValues)
		{
			for(ArrayList<Integer> conceptSet : conceptSetValues)
			{
				if(conceptSet.containsAll(attributeSet))
				{
					isConsistent =  true;
					break;
				}
				else
				{
					isConsistent =  false;
				}
			}
			if(!isConsistent)
				return isConsistent;
		}
		return isConsistent;
	}

	public static Map<String,Double> calculateEntropy(Map<String, ArrayList<String>> discretizedTable,Map<String, ArrayList<Integer>> conceptValuePairs) 
	{
		Map<String,Double> attributeEntropyPair = new LinkedHashMap<String,Double>();
		int numberOfDecisions = 0;
		for(Map.Entry<String, ArrayList<Integer>> conceptValuePair : conceptValuePairs.entrySet())
			numberOfDecisions+=conceptValuePair.getValue().size();

		//for each attribute calculate the entropy
		for(Map.Entry<String, ArrayList<String>> eachAttribute : discretizedTable.entrySet())
		{
			//stores the concept values of each attribute range eg: [5..7.5 = [yes,yes], 7.5..15.0 = [yes,no,no,no]]
			Map<String,ArrayList<String>> conceptValuesForEachAttributeRange = new LinkedHashMap<String,ArrayList<String>>();
			ArrayList<String> attributeValues = eachAttribute.getValue();
			int attributePositionCounter = 1;
			for(String attributeValue : attributeValues)
			{
				String conceptValueOfAttribute = "";
				for(Map.Entry<String, ArrayList<Integer>> conceptValuePair : conceptValuePairs.entrySet())
				{
					if(conceptValuePair.getValue().contains(attributePositionCounter))
						conceptValueOfAttribute = conceptValuePair.getKey();
				}
				if(!conceptValuesForEachAttributeRange.containsKey(attributeValue))
				{
					conceptValuesForEachAttributeRange.put(attributeValue,new ArrayList<String>());
					conceptValuesForEachAttributeRange.get(attributeValue).add(conceptValueOfAttribute);
				}
				else
				{
					conceptValuesForEachAttributeRange.get(attributeValue).add(conceptValueOfAttribute);
				}
				attributePositionCounter++;

			}

			double totalEntropy = 0.0;
			//for each different value of attribute i.e., for 5..7.5, for 7.5..15
			for(Map.Entry<String, ArrayList<String>> conceptValueForEachAttributeRange : conceptValuesForEachAttributeRange.entrySet())
			{
				ArrayList<String> conceptValues =conceptValueForEachAttributeRange.getValue();
				int numberOfDecisionsInAttributeRange = conceptValues.size();
				double entropy = (double)numberOfDecisionsInAttributeRange/numberOfDecisions;
				double internalValue = 0.0;
				Map<String,Integer> conceptCountPairs = new LinkedHashMap<String,Integer>();
				for(String eachConcept : conceptValues)
				{
					if(!conceptCountPairs.containsKey(eachConcept))
						conceptCountPairs.put(eachConcept, 1);
					else
						conceptCountPairs.put(eachConcept,(conceptCountPairs.get(eachConcept))+1 );
				}

				for(Map.Entry<String, Integer> conceptCountPair : conceptCountPairs.entrySet())
				{
					int eachConceptCount = conceptCountPair.getValue();
					internalValue+= (MathematicsUtility.log2(eachConceptCount)-MathematicsUtility.log2(numberOfDecisionsInAttributeRange))*-((double)eachConceptCount/numberOfDecisionsInAttributeRange);  

				}
				entropy*=internalValue;
				totalEntropy+=entropy;

			}
			attributeEntropyPair.put(eachAttribute.getKey(),totalEntropy);

		}
		return attributeEntropyPair;

	}

	public static String selectWorstAttribute(Map<String, Double> attributeEntropyPair, Map<String,Integer> attributeNumberOfIntervalPairs) 
	{
		String worstAttribute = "";
		double averageBlockEntropy = 0.0;
		for(Map.Entry<String, Double> eachAttributeEntropy : attributeEntropyPair.entrySet())
		{
			int numberOfIntervals = attributeNumberOfIntervalPairs.get(eachAttributeEntropy.getKey());
			if((double)eachAttributeEntropy.getValue()/numberOfIntervals>averageBlockEntropy)
			{
				averageBlockEntropy = (double)eachAttributeEntropy.getValue()/numberOfIntervals;
				worstAttribute = eachAttributeEntropy.getKey();
			}
		}

		//Logic to write data file
		try 
		{
			File file = new File("output/test.init");
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Worst Attribute:::"+worstAttribute);
			bw.newLine();
			bw.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return worstAttribute;

	}


	//following methods are for Equal Inerval Width Algorithm

	//determine cutpoints for equal interval width
	public static Map<String,Double> determineCutPointsForEqualIntervalWidthAlgorithm(Map<String,ArrayList<Double>> attributeValuePairsInDouble)
	{
		try 
		{
			File file = new File("output/test.init");
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Possible Cut Point for Each Attribute with k=2:::");
			bw.newLine();

			attributeValuePairsInDouble_Global.putAll(attributeValuePairsInDouble);
			Map<String,ArrayList<Double>> attributeCutPointPairs = new LinkedHashMap<String,ArrayList<Double>>();

			for(Map.Entry<String, ArrayList<Double>> attributeValuePair : attributeValuePairsInDouble.entrySet())
			{
				String attributeName = attributeValuePair.getKey();
				attributeCutPointPairs.put(attributeName, new ArrayList<Double>());
				ArrayList<Double> values = attributeValuePair.getValue();
				for(Double value : values)
				{
					if(!attributeCutPointPairs.get(attributeName).contains(value))
						attributeCutPointPairs.get(attributeName).add(value);
				}	
				Collections.sort(attributeCutPointPairs.get(attributeName));
			}

			Map<String,Double> attributeSelectedCutPointPair = new LinkedHashMap<String,Double>();
			for(Map.Entry<String, ArrayList<Double>> attributeCutPointPair : attributeCutPointPairs.entrySet())
			{

				String attributeName = attributeCutPointPair.getKey();
				ArrayList<Double> cutPoints = attributeCutPointPair.getValue();
				//Determine cut point max+min/2
				Double selectedCutPoint = (cutPoints.get(0)+cutPoints.get(cutPoints.size()-1))/2;
				attributeSelectedCutPointPair.put(attributeName,selectedCutPoint);
			}

			//Logic to write data file
			for(Map.Entry<String, Double> attributeCutPointPair : attributeSelectedCutPointPair.entrySet())
			{
				StringBuilder dataLine = new StringBuilder("Selected CutPoint for attribute "+attributeCutPointPair.getKey()+": ");
				dataLine.append(attributeCutPointPair.getValue()+" ");
				bw.write(dataLine.toString());
				bw.newLine();
			}
			bw.close();

			return attributeSelectedCutPointPair;		
		} 
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	//Determines the possible cut points for the worst attribute selected based on average block entropy. numberOfIntervals gives the value of K.
	public static ArrayList<Double> selectBestCutPointsForWorstAttributeForEqualIntervalWidthAlgorithm(String worstAttribute,ArrayList<Double> cutPointsOfWorstAttribute,int numberofIntervals)
	{

		FileWriter fw = null;
		BufferedWriter bw = null;
		try 
		{
			File file = new File("output/test.init");
			if(!file.exists())
				file.createNewFile();
			fw = new FileWriter(file.getAbsoluteFile(),true);
			bw = new BufferedWriter(fw);
			bw.write("Possible Cut Point Pairs for Selected Worst Attribute with k="+(numberofIntervals+1)+": ");
			bw.newLine();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		/*Logic added for attributes with single cut point*/
		if(cutPointsOfWorstAttribute.size()==1)
		{
			return cutPointsOfWorstAttribute;
		}

		/* Logic to select possible cut points with increase in K*/
		ArrayList<Double> returnValues = new ArrayList<Double>();
		double averagecutpoint = ((Collections.max(cutPointsOfWorstAttribute))-(Collections.min(cutPointsOfWorstAttribute)))/(numberofIntervals+1);
		double cutpoint = Collections.min(cutPointsOfWorstAttribute)+averagecutpoint;
		returnValues.add(cutpoint);

		for(int i=0;i<numberofIntervals-1;i++)
		{
			cutpoint = cutpoint+averagecutpoint;
			returnValues.add(cutpoint);
		}

		Set<Double> cutPoints = new HashSet<Double>();
		cutPoints.addAll(returnValues);
		returnValues.clear();
		returnValues.addAll(cutPoints);
		Collections.sort(returnValues);

		//Logic to write data file
		StringBuilder dataLine = new StringBuilder("");
		for(Double value : returnValues)
			dataLine.append(value+" ");
		try{
			bw.write(dataLine.toString());
			bw.newLine();}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try
		{
			bw.write("Best Cut Point Pairs for Selected Worst Attribute with k="+(numberofIntervals+1)+": ");
			for(Double value:returnValues)
				bw.append(value+" ");
			bw.newLine();
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return returnValues;
	}

	//The following method is for conditional entropy algorithm
	//Determines the best possible cut point for each attribute. This is initially when k=2.
	public static  Map<String,Double> selectBestCutPointsForConditionalEntropyAlgorithm(Map<String,ArrayList<Double>> attributeCutPointPairs,Map<String, ArrayList<Integer>> conceptValuePairs)
	{
		try 
		{
			File file = new File("output/test.init");
			if(!file.exists())
				file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Best Cut Point for Each Attribute:::");
			bw.newLine();


			Map<String,Double> attributeSelectedCutPointPair = new LinkedHashMap<String,Double>();
			for(Map.Entry<String, ArrayList<Double>> attributeCutPointPair : attributeCutPointPairs.entrySet())
			{
				String attributeName = attributeCutPointPair.getKey();
				ArrayList<Double> cutPoints = attributeCutPointPair.getValue();
				ArrayList<Double> actualValues = attributeValuePairsInDouble_Global.get(attributeName);

				int numberOfDecisions = 0;
				for(Map.Entry<String, ArrayList<Integer>> conceptValuePair : conceptValuePairs.entrySet())
					numberOfDecisions+=conceptValuePair.getValue().size();

				double minEntropy = 100.0;
				double selectedCutpoint = cutPoints.get(0);;

				// For each cutpoint calculate entrpoy
				for(Double cutPoint : cutPoints)
				{
					Map<String,ArrayList<String>> conceptValuesForEachCutPoint = new LinkedHashMap<String,ArrayList<String>>();
					int attributePositionCounter = 1;
					for(int i=0;i<actualValues.size();i++)
					{
						if(actualValues.get(i)<=cutPoint)
						{
							String attributeValue = "left";
							String conceptValueOfAttribute = "";
							for(Map.Entry<String, ArrayList<Integer>> conceptValuePair : conceptValuePairs.entrySet())
							{
								if(conceptValuePair.getValue().contains(attributePositionCounter))
									conceptValueOfAttribute = conceptValuePair.getKey();
							}
							if(!conceptValuesForEachCutPoint.containsKey(attributeValue))
							{
								conceptValuesForEachCutPoint.put(attributeValue,new ArrayList<String>());
								conceptValuesForEachCutPoint.get(attributeValue).add(conceptValueOfAttribute);
							}
							else
							{
								conceptValuesForEachCutPoint.get(attributeValue).add(conceptValueOfAttribute);
							}
						}
						else if(actualValues.get(i)>cutPoint)
						{
							String attributeValue = "right";
							String conceptValueOfAttribute = "";

							for(Map.Entry<String, ArrayList<Integer>> conceptValuePair : conceptValuePairs.entrySet())
							{
								if(conceptValuePair.getValue().contains(attributePositionCounter))
									conceptValueOfAttribute = conceptValuePair.getKey();
							}
							if(!conceptValuesForEachCutPoint.containsKey(attributeValue))
							{
								conceptValuesForEachCutPoint.put(attributeValue,new ArrayList<String>());
								conceptValuesForEachCutPoint.get(attributeValue).add(conceptValueOfAttribute);
							}
							else
							{
								conceptValuesForEachCutPoint.get(attributeValue).add(conceptValueOfAttribute);
							}
						}
						attributePositionCounter++;
					}

					double leftEntropy = 0.0;
					double rightEntropy = 0.0;
					double totalEntropy = 0.0;
					//for each different cut point
					for(Map.Entry<String, ArrayList<String>> conceptValueForEachCutPoint : conceptValuesForEachCutPoint.entrySet())
					{
						if(conceptValueForEachCutPoint.getKey() == "left")
						{
							ArrayList<String> conceptValues =conceptValueForEachCutPoint.getValue();
							int numberOfDecisionsInAttributeRange = conceptValues.size();
							double entropy = (double)numberOfDecisionsInAttributeRange/numberOfDecisions;
							double internalValue = 0.0;
							Map<String,Integer> conceptCountPairs = new LinkedHashMap<String,Integer>();
							for(String eachConcept : conceptValues)
							{
								if(!conceptCountPairs.containsKey(eachConcept))
									conceptCountPairs.put(eachConcept, 1);
								else
									conceptCountPairs.put(eachConcept,(conceptCountPairs.get(eachConcept))+1 );
							}

							for(Map.Entry<String, Integer> conceptCountPair : conceptCountPairs.entrySet())
							{
								int eachConceptCount = conceptCountPair.getValue();
								internalValue+= (MathematicsUtility.log2(eachConceptCount)-MathematicsUtility.log2(numberOfDecisionsInAttributeRange))*-((double)eachConceptCount/numberOfDecisionsInAttributeRange);  

							}
							entropy*=internalValue;
							leftEntropy+=entropy;
						}
						if(conceptValueForEachCutPoint.getKey() == "right")
						{
							ArrayList<String> conceptValues =conceptValueForEachCutPoint.getValue();
							int numberOfDecisionsInAttributeRange = conceptValues.size();
							double entropy = (double)numberOfDecisionsInAttributeRange/numberOfDecisions;
							double internalValue = 0.0;
							Map<String,Integer> conceptCountPairs = new LinkedHashMap<String,Integer>();
							for(String eachConcept : conceptValues)
							{
								if(!conceptCountPairs.containsKey(eachConcept))
									conceptCountPairs.put(eachConcept, 1);
								else
									conceptCountPairs.put(eachConcept,(conceptCountPairs.get(eachConcept))+1 );
							}

							for(Map.Entry<String, Integer> conceptCountPair : conceptCountPairs.entrySet())
							{
								int eachConceptCount = conceptCountPair.getValue();
								internalValue+= (MathematicsUtility.log2(eachConceptCount)-MathematicsUtility.log2(numberOfDecisionsInAttributeRange))*-((double)eachConceptCount/numberOfDecisionsInAttributeRange);  

							}
							entropy*=internalValue;
							rightEntropy+=entropy;
						}
					}
					totalEntropy = leftEntropy + rightEntropy ;
					if(totalEntropy<=minEntropy)
					{
						minEntropy = totalEntropy;
						selectedCutpoint = cutPoint;
					}
				}
				attributeSelectedCutPointPair.put(attributeName,selectedCutpoint);
			} 

			//Logic to write data file
			for(Map.Entry<String, Double> attributeCutPointPair : attributeSelectedCutPointPair.entrySet())
			{
				StringBuilder dataLine = new StringBuilder("Selected CutPoint for attribute "+attributeCutPointPair.getKey()+": ");
				dataLine.append(attributeCutPointPair.getValue()+" ");
				bw.write(dataLine.toString());
				bw.newLine();
			}
			bw.close();

			return attributeSelectedCutPointPair;
		}

		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
