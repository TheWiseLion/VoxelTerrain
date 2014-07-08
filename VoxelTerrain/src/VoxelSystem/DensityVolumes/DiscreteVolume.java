package VoxelSystem.DensityVolumes;

public interface DiscreteVolume extends DensityVolume{
	public void extract(DensityVolume source);
	//TODO: Hash Function (variable length?)
	
	public void setDensity(float x, float y, float z,float d);
	public void setType(float x, float y, float z, int t);
	
	//public float getResolution
}
