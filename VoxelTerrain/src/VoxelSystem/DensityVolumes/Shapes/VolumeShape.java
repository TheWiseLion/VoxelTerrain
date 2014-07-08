package VoxelSystem.DensityVolumes.Shapes;

import VoxelSystem.DensityVolumes.DensityVolume;
import VoxelSystem.DensityVolumes.TypeVolume;

public abstract class VolumeShape implements DensityVolume{
	
	private TypeVolume tv;
	private int type = 0;
	public VolumeShape(){
		final int t = type;
		tv = new TypeVolume(){
			@Override
			public int getType(float x, float y, float z) {
				return t;
			}
		};
	}
	
	public TypeVolume getTypeVolume() {
		return tv;
	}

	public void setTypeVolume(TypeVolume tv) {
		this.tv = tv;
	}
	
	public final int getType(float x, float y,float z){
		return tv.getType(x, y, z);
	}
	
	/***
	 * Sets all the voxel type's of this volume to the given parameter
	 */
	public void setSimpleType(final int voxelType){
		tv = new TypeVolume(){
			@Override
			public int getType(float x, float y, float z) {
				return voxelType;
			}
		};
	}
	
}
