package VoxelSystem.VoxelStorage;

import com.jme3.math.Vector3f;

public interface VoxelTree {
	
	public int getType(int x, int y, int z, int LOD);
	public Float getIntersection(int x, int y, int z,int a, int LOD);
	public Vector3f getNormal(int x, int y, int z,int a, int LOD);
	public int getLODLevels();
	
}
