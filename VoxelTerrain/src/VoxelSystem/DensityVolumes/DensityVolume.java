package VoxelSystem.DensityVolumes;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

/***
 * Represents a volume of space from which density
 * values may be sampled. 
 * @author Inspired by Paul Speed
 *
 */
public interface DensityVolume extends TypeVolume{


	/**
	 * Retrieves a density value from the specified point in this volume.
	 */
	public float getDensity(float x, float y, float z);

	/**
	 * Retrieves the field direction at a particular point in this volume. Field
	 * direction can be used to define surface normals,
	 */
	public Vector3f getSurfaceNormal(float x, float y, float z);
	
	/***
	 * The bounding volume which defintes a region where the isosurface may lay.
	 * Isosurface may lie directly on or within the bounding volume.
	 * If infinite then null is returned.
	 */
	public BoundingBox getEffectiveVolume();
	
	//public boolean containsIntersection(Vector3f p1, Vector3f p2)
	
}
