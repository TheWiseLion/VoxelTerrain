package VoxelSystem.Hermite;

import idea.VoxelGrid;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;


public interface VoxelExtractor {

	/***
	 * Returns the edge between the two points.
	 * It is implied that p1 and p2 only differ by 1 axis.
	 * The first type in the HermiteEdge must correlate
	 * to the first point p1. Same follows for p2.
	 * 
	 * 
	 */
	public HermiteEdge getEdge(Vector3f p1, Vector3f p2);
	
	public VoxelGrid extract(Vector3f point, int dimX, int dimY, int dimZ, float resolution);
	
	
	/***
	 * The bounds for this extractor. 
	 * It follows the same definition as @link{DensityVolume}
	 * If infinite then null is returned.
	 * 
	 * @return
	 */
	public BoundingBox getBoundingBox();
}
