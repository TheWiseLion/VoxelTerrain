package idea;

import VoxelSystem.Hermite.HermiteExtractor;

import com.jme3.math.Vector3f;

public interface HermiteGrid {
	
	
	public int getType(int x, int y, int z);
	
	
	public Float getIntersection(int x, int y, int z, int a);
	
	
	public Vector3f getNormal(int x, int y, int z, int a);
	
	
	public void extract(Vector3f point,float res,HermiteExtractor he);
	
	public int getWidth();
	public int getHieght();
	public int getDepth();
	
	
}
