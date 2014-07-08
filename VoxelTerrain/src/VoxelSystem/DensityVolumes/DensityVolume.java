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
	 * direction can be used to define surface normals, potentially predict
	 * collisions, etc..
	 */
	public Vector3f getFieldDirection(float x, float y, float z);
	
	/***
	 * The bounding range where this volume has positive density.
	 * if null this volume has a potentially infinite range 
	 */
	public BoundingBox getEffectiveVolume();
	
	/***
	 * Determintes whether this volume implements
	 * @link{DiscreteVolume}
	 */
	public boolean isDiscrete();
	

	
}
