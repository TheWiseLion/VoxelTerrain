package VoxelSystem.Hermite;

import com.jme3.math.Vector3f;

public class HermiteEdge {
	public int t1, t2;
	public Float intersection;
	public Vector3f normal;
	
	
	public HermiteEdge(){
		t1 = -1;
		t2 = -1;
		intersection = null;
		normal = null;
	}
	
	public HermiteEdge(int t1, int t2){
		this.t1 = t1;
		this.t2 = t2;
		intersection = null;
		normal = null;
	}
	
	public HermiteEdge(int t1, int t2, Float intersection, Vector3f normal){
		this.t1 = t1;
		this.t2 = t2;
		this.intersection = intersection;
		this.normal = normal;
	}
	
	
}
