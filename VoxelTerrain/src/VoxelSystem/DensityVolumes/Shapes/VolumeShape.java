package VoxelSystem.DensityVolumes.Shapes;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

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
	

	public static boolean contains(BoundingBox bb, Vector3f point) {
		Vector3f center = bb.getCenter();
		return FastMath.abs(center.x - point.x) <= bb.getXExtent()
				&& FastMath.abs(center.y - point.y) <= bb.getYExtent()
				&& FastMath.abs(center.z - point.z) <= bb.getZExtent();
	}
	
}
