package VoxelSystem.Hermite;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public interface HermiteExtractor {
//	public float getResolution();
//	public void setResolution(float res);
	
//	public HermiteCube getCube(int x, int y, int z);
//	public Vector3f fromFloat(float x, float y, float z);
	
	public BoundingBox getBoundingBox();
	
	/***
	 * To promote acceleration. Ordering of cube corners can be found in
	 * @link{VoxelSystemTable}. 
	 */
	public HermiteCube getCube(Vector3f [] cubeCorners);
	
	/***
	 * Returns the edge between the two points.
	 * If null is returned then there is no hermite data for the edge.
	 * It is implied that p1 and p2 only differ by 1 axis.
	 */
	public HermiteEdge getEdge(Vector3f p1, Vector3f p2);
	
	/***
	 * Returns the material type for the given point.
	 * Material -1 is air.
	 */
	public HermitePoint getPoint(Vector3f p);
}
