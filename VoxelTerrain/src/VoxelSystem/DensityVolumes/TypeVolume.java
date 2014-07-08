package VoxelSystem.DensityVolumes;

public interface TypeVolume {
	/***
	 * Gets the typeID at the position x,y,z
	 * returned integer must be greater than or equal to zero
	 */
	int getType(float x, float y, float z);
}
