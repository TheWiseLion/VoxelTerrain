package VoxelSystem.MeshBuilding;

import com.jme3.math.Vector3f;

/**
 * This is the surface data that is extracted from the underlying volume data.
 */
public class SurfacePoint {
	public final Vector3f point;
	//Vector3f normal; ?
	public  int type;
	public SurfacePoint(Vector3f point,int type){
		this.point = point;
		this.type = type;
	}

}
