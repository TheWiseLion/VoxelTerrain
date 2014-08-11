package voxelsystem.voxeldata;

import voxelsystem.VoxelSystemTables.AXIS;

import com.jme3.math.Vector3f;

/***
 * Grid based extractors implicitly define voxel resolution.
 * They <b>MUST</b> support queries in the range:
 * [-1,-1,-1] to [getWidth()+1,getHieght()+1,getDepth()+1]
 * @author 0xFFFF
 *
 */
public interface VoxelGrid extends VoxelExtractor{
	
	/***
	 * Defines minimum of bounding box
	 * @return
	 */
	Vector3f getCorner();
	public float getVoxelSize();
	
	public int getType(int x, int y, int z);
	
	public Float getIntersection(int x, int y, int z, AXIS a);
	
	public Vector3f getNormal(int x, int y, int z, AXIS a);
	
	public void setType(int x, int y, int z, int type);
	
	public void setNormal(int x, int y, int z, AXIS a, Vector3f n);
	
	/***
	 * @param a - axis 
	 * @param f - [0-1] defines how far between the 2 voxels lies an intersection.
	 */
	public void setIntersection(int x, int y, int z, AXIS a, float f);
	
	public void extract(VoxelExtractor ve);
	
	public int getWidth();
	public int getHeight();
	public int getDepth();
	
	
}
