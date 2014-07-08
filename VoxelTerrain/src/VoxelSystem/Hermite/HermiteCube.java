package VoxelSystem.Hermite;

import com.jme3.math.Vector3f;

/***
 * Underlying data for all terrain.
 * Produced from operations, and extracted density volumes.
 * 
 * TODO: by cube or by sub-cube?
 */
public class HermiteCube {
	public int materials[];
	public int edgeInfo;//from VoxelSystemTables
	public Vector3f intersections[];
	public Vector3f normals[];
//	float resolution;
//	GridPoint [][][] grid;
	
	//int [][][] materials
	//Cubes[][][]
	
	//For each cube
	//int cubeValue
	//int [] indexes
	//Map<Integer,Float> indexToIntersection
	//Map<Integer,Vector3f> normal
	
	
	
	
	
}
