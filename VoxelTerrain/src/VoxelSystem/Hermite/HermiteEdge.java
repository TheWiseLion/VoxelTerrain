package VoxelSystem.Hermite;

import com.jme3.math.Vector3f;

public class HermiteEdge {
//	public int t1;
//	public int t2;
	public Vector3f intersection;
	public Vector3f normal;
	//, int t1, int t2
	public HermiteEdge(Vector3f intersection, Vector3f normal){
		this.intersection = intersection;
		this.normal = normal;
	}
	
	
}
