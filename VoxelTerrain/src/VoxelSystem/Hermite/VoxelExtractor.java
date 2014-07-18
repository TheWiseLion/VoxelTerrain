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
	 * In addition either p1 or p2 must be inside the bounding box
	 * for this extractor. If this is not the case it is <b>expected</b>
	 * that the return value is null.
	 */
	public HermiteEdge getEdge(Vector3f p1, Vector3f p2);
	
	public VoxelGrid extract(Vector3f point, int dimX, int dimY, int dimZ, float resolution);
	
	
	/***
	 * The bounds for this extractor. 
	 * If infinite then null is returned.
	 * 
	 * The bounding region defines either where the extractor is
	 * strictly valid (as in the case of VoxelGrid's) or it may describe where 
	 * the extractor is 'effective' (i.e. bounds for the described volume).
	 * 
	 * @return
	 */
	public BoundingBox getBoundingBox();
}
