package VoxelSystem.VoxelObjects;

import VoxelSystem.VoxelData.HermiteEdge;
import VoxelSystem.VoxelData.VoxelExtractor;
import VoxelSystem.VoxelSystemTables.AXIS;

import com.jme3.math.Vector3f;

public class GridUtils {
	
	/***
	 * Extracts the data in the voxel extractor into the given voxel node.
	 * The image @link{} outlines exactly what is stored
	 */
	public static void extractData(VoxelNode c, VoxelExtractor ve, int vMin[], int[] vMax){
		Vector3f p = c.getCorner();
		float vSize = c.getVoxelSize();
		
		//If only it were that simple....
		//Check if edge needs to be stored 
		
		//Compute Voxel Intersections: (voxel space)
		int minX = Math.max(vMin[0],c.vOffX);
		int minY = Math.max(vMin[1],c.vOffY);
		int minZ = Math.max(vMin[2],c.vOffZ);
		int maxX = Math.min(vMax[0], c.vOffX + c.width  + 1);
		int maxY = Math.min(vMax[1], c.vOffY + c.height + 1);
		int maxZ = Math.min(vMax[2], c.vOffZ + c.depth  + 1);
		
		//Convert to local space
		minX -= c.vOffX; minX = Math.max(minX, 0);
		minY -= c.vOffY; minY = Math.max(minY, 0);
		minZ -= c.vOffZ; minZ = Math.max(minZ, 0);
		
		maxX -= c.vOffX; maxX = Math.min(maxX, c.width);
		maxY -= c.vOffY; maxY = Math.min(maxY, c.height);
		maxZ -= c.vOffZ; maxZ = Math.min(maxZ, c.depth);
		
		
		Vector3f qP1 = new Vector3f();
		Vector3f qP2 = new Vector3f();
		
		for (int x = minX; x < maxX; x++) {
			qP1.x = p.x + x * vSize;
			for (int z = minZ; z < maxZ; z++) {
				qP1.z = p.z + z * vSize;
				for (int y = minY; y < maxY; y++) {
					qP1.y = p.y + y * vSize;
					
//					int tx = -2, ty=0, tz = -1;//Entry -2,0,-1
//					if(x+c.vOffX == tx &&  y+c.vOffY == ty && z+c.vOffZ == tz){
//						System.out.println("Entry");
//					}else if(x+c.vOffX == tx &&  y+c.vOffY == ty && z+c.vOffZ == tz+1){
//						System.out.println("Entry");
//					}
					
					
					boolean oX = x==(maxX) ? true:false;
					boolean oY = y==(maxY) ? true:false;
					boolean oZ = z==(maxZ) ? true:false;
					
					//Odd iterations, in odd places..
					HermiteEdge edge0 = null;
					HermiteEdge edge4 = null;
					HermiteEdge edge8 = null;
					
					//+x
					if(!oX){
						qP2.set(qP1.x + vSize, qP1.y, qP1.z);
						edge0 = ve.getEdge(qP1, qP2);				
						if(edge0 != null && edge0.intersection != null){
							c.setIntersection(x, y, z, AXIS.X, edge0.intersection);
							c.setNormal(x,y,z, AXIS.X, edge0.normal);
						}
					}
					
					//+y
					if(!oY){
						qP2.set(qP1.x, qP1.y + vSize, qP1.z);
						edge4 = ve.getEdge(qP1, qP2);
						if(edge4 != null && edge4.intersection != null){
							c.setIntersection(x, y, z, AXIS.Y, edge4.intersection);
							c.setNormal(x,y,z, AXIS.Y, edge4.normal);
						}
					}

					//+z
					if(!oZ){
						qP2.set(qP1.x, qP1.y, qP1.z + vSize);
						edge8 = ve.getEdge(qP1, qP2);
						if(edge8!=null && edge8.intersection != null){
							c.setIntersection(x, y, z, AXIS.Z, edge8.intersection);
							c.setNormal(x,y,z, AXIS.Z, edge8.normal);
						}
					}
					
					if(edge0 != null) {
						c.setType(x,y,z,edge0.t1);
					}else if(edge4 != null){
						c.setType(x,y,z,edge4.t1);
					}else if(edge8 != null){
						c.setType(x,y,z,edge8.t1);
					}
					
					
				}
			}
		}
		
		

		
		
		
		
	}
	
}
