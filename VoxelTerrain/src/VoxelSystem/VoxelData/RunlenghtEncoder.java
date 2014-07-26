package VoxelSystem.VoxelData;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/***
 * Used in chunk for run length encoding.
 * Very simple design. Reapeat#-Value-Repeat#-Value.....
 * @author 0xFFFF
 *
 */
public class RunlenghtEncoder {
	LinkedList<Integer> storage = new LinkedList<Integer>();
	
	int size;
	public RunlenghtEncoder(int [] array){
		this.size = array
	}
	
	public int[] decompress(){
		
	}
	

}
