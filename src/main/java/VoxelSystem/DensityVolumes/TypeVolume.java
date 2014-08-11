package VoxelSystem.DensityVolumes;

public interface TypeVolume {
	/***
	 * Gets the typeID at the position x,y,z
	 * If point is outside the volume -1 must be returned.
	 */
	int getType(float x, float y, float z);
}
