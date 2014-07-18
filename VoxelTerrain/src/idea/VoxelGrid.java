package idea;

import VoxelSystem.VoxelSystemTables.AXIS;
import VoxelSystem.Hermite.VoxelExtractor;

import com.jme3.math.Vector3f;

public interface VoxelGrid extends VoxelExtractor{
	
	
	Vector3f getCorner();
	public float getVoxelSize();
	
	
	public int getType(int x, int y, int z);
	
	public Float getIntersection(int x, int y, int z, AXIS a);
	
	public Vector3f getNormal(int x, int y, int z, AXIS a);
	
	public void setType(int x, int y, int z, int type);
	
	public void setNormal(int x, int y, int z, AXIS a, Vector3f n);
	
	/***
	 * @param a - axis (1 - positive x), (2 - positive y), (3 - positive z), (-1 -> negative x).....
	 * @param f - [0-1] defines how far between the 2 voxels lies an intersection.
	 */
	public void setIntersection(int x, int y, int z, AXIS a, float f);
	
	public void extract(VoxelExtractor ve);
	
	public int getWidth();
	public int getHieght();
	public int getDepth();
	
	
}
