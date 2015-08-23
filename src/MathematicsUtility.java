import java.util.ArrayList;
import java.util.List;


public class MathematicsUtility 
{
	
	//Method to form all possible r combinations of given array of n integers
	public static ArrayList<ArrayList<Double>> combinationUtil(List<Double> cutPoints, ArrayList<ArrayList<Double>> returnValues,List<Double> data, int start, int end, int index, int r)
	{
		if (index == r)
		{
			ArrayList<Double> returnValue = new ArrayList<Double>();
			for (int j=0; j<r; j++)
			{
				returnValue.add(data.get(j));
			}
			returnValues.add(returnValue);
		}

		for (int i=start; i<=end && end-i+1 >= r-index; i++)
		{
			data.add(index,cutPoints.get(i));
			combinationUtil(cutPoints, returnValues,data, i+1, end, index+1, r);
		}
		return returnValues;
	}

	
	//Function to calculate log2 values
	public static double log2( double a )
	{
		double result = Math.log(a)/Math.log(2);
		return result;
	}
}
