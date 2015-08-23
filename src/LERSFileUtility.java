import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class LERSFileUtility 
{
	static int attributeCount = 0;
	static ArrayList<String> attributeAndConceptNames;
	static Map<String,ArrayList<String>> attributeValuePairs;
	
	public static Map<String, ArrayList<String>> readLERSFileData(String inputFileName) throws IOException
	{
		FileInputStream fstream = null;
		BufferedReader reader = null;
		attributeValuePairs = new LinkedHashMap<String,ArrayList<String>>();
		
		try 
		{
			fstream = new FileInputStream(inputFileName);
			reader = new BufferedReader(new InputStreamReader(fstream));
			String stringLine;
			
			if(reader != null)
			{
				//Read File Line By Line
				while ((stringLine = reader.readLine()) != null)  
				{
					Pattern p= Pattern.compile("(\\s*(!.*((\\r\\n)|(\\n)|(\\r)))\\s*)|(\\s+)");
					String[] lineData = stringLine.split(p.toString());
					if(lineData.length > 0)
					{
						if(lineData[0].equalsIgnoreCase("<"))
						{
							for(int i=0; i<lineData.length; i++)
							{
								if(lineData[i].equals("a"))
									attributeCount++;						
							}
						}
						else if(lineData[0].equalsIgnoreCase("["))
						{
							attributeAndConceptNames = new ArrayList<String>();
							for(int i=1; i<lineData.length-1; i++)
							{	
								attributeAndConceptNames.add(lineData[i]);
							}
							for(int j=0; j<attributeAndConceptNames.size(); j++)
							{
								attributeValuePairs.put(attributeAndConceptNames.get(j),new ArrayList<String>());
							}
							
						}
						else if(lineData[0].equalsIgnoreCase("!"))
						{
							continue;
						}
						else
						{
							for(int i=0;i<attributeAndConceptNames.size();i++)
							{
								String attributeName = attributeAndConceptNames.get(i);
								ArrayList<String> tempAttributeValue = attributeValuePairs.get(attributeName);
								attributeValuePairs.remove(attributeName);
								tempAttributeValue.add(lineData[i]);
								attributeValuePairs.put(attributeName, tempAttributeValue);	
							}
						}
					}
				}
			}
			
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			reader.close();
		}
		return attributeValuePairs;
	
	}

	public static void writeDiscretizedTableInLERSFormat(Map<String, ArrayList<String>> discretizedTable,Map<String, ArrayList<Integer>> conceptValuePairs) 
	{
		try
		{
			File file = new File("output/test.data");
			if(file.exists())
				file.delete();
			file.createNewFile();
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			String conceptName = attributeAndConceptNames.get(attributeAndConceptNames.size()-1);
			StringBuilder firstLine = new StringBuilder("< ");
			StringBuilder secondLine = new StringBuilder("[ ");
			for(int i=0;i<attributeAndConceptNames.size()-1;i++)
			{
				firstLine.append("a ");
				secondLine.append(attributeAndConceptNames.get(i)+" ");
			}
			firstLine.append("d >");
			secondLine.append(conceptName+" ]");
			bw.write(firstLine.toString());
			bw.newLine();
			bw.write(secondLine.toString());
			bw.newLine();
			
			for(int i=0;i<discretizedTable.get(attributeAndConceptNames.get(0)).size();i++)
			{
				StringBuilder fileData = new StringBuilder("");
				for(Map.Entry<String, ArrayList<String>> eachAttribute : discretizedTable.entrySet())
				{
					fileData.append(eachAttribute.getValue().get(i)+" ");
				}
				fileData.append(attributeValuePairs.get(conceptName).get(i));
				bw.write(fileData.toString());
				bw.newLine();
			}
			bw.close(); 
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}
}
