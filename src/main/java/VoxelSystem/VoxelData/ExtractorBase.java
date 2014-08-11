package voxelsystem.voxeldata;

import voxelsystem.VoxelSystemTables.AXIS;
import voxelsystem.operators.CSGHelpers;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;


/***
 * This is a convince class as it implements the extract function for you.
 * 
 */
public abstract class ExtractorBase implements VoxelExtractor{
	
	@Override
	public VoxelGrid extract(Vector3f p, int dimX, int dimY, int dimZ, float res) {
		Chunk c = new Chunk(p,dimX,dimY,dimZ, res);
		return extract(this, c);
	}
	
	public VoxelGrid extract(VoxelExtractor ve, Chunk c){
		//Recall the legality of bound rules. 
		//	1. At least one of the query points must be in the bounding volume 
		
		//So basically we need to bound what we iterate over.
		
		//Lets start with the exact intersection
		float res = c.getVoxelSize();
		BoundingBox cb = c.getBoundingBox();
		Vector3f p = cb.getMin(new Vector3f()); // ve.getBoundingBox().getMin(new Vector3f());
		
		BoundingBox queryCheck = new BoundingBox();
		
//		BoundingBox oldBox = ve.getBoundingBox();
//		Vector3f mi = oldBox.getMin(new Vector3f());
//		Vector3f ma = oldBox.getMax(new Vector3f());
//		mi.subtractLocal(res, res, res);
//		ma.addLocal(res, res ,res);
//		BoundingBox newBox = new BoundingBox(mi,ma);
		
		Boolean b = CSGHelpers.getIntersection(c.getBoundingBox(),ve.getBoundingBox() , queryCheck);
		
		int minX,minY,minZ;
		int maxX,maxY,maxZ;
		Vector3f min = queryCheck.getMin(new Vector3f()),max = queryCheck.getMax(new Vector3f());
		
		if(b == null){
			throw new RuntimeException("Impossible.");
		}else if(b == false){// they dont intersect any where..
			return c;//maybe null?
		}else{
			//Lets iterate effectively...
			//query check is a box
			//we want over lay the maximum amount of the grid that fits
			
			//Have to use double precision...
//			double d= 
			min = min.subtract(p);
			max = max.subtract(p);
			minX = ((int) (min.x/res)) - 1;
			minY = ((int) (min.y/res)) - 1;
			minZ = ((int) (min.z/res)) - 1;
//			
			maxX = ((int) (max.x/res)) + 1;
			maxY = ((int) (max.y/res)) + 1;
			maxZ = ((int) (max.z/res)) + 1;
//			minX = -1;
//			minY = -1;
//			minZ = -1;
//			maxX = 20;
//			maxY = 20;
//			maxZ = 20;
		}
		
		
		Vector3f qPoint1 =  new Vector3f();
		Vector3f qPoint2 =  new Vector3f();
		for (int y = minY; y <= maxY; y++) {
			float dy = y * res;

			for (int x = minX; x <= maxX; x++) {
				float dx = x * res;

				for (int z = minZ; z <= maxZ; z++) {
					float dz = z * res;
					
					//20,19,5
//					int tx = 9,ty=9,tz=9;
//					if(x == tx && y == ty && z == tz){
//						System.out.println("Entry");
//					}else if(x == tx && y == ty && z == tz){
//						System.out.println("Entry");
//					}
////					
					
					boolean oX = x==maxX?true:false;
					boolean oY = y==maxY?true:false;
					boolean oZ = z==maxZ?true:false;
					
					qPoint1.set(p.x+dx, p.y+dy, p.z+dz);
					
					
					HermiteEdge edge0 = null;
					HermiteEdge edge4 = null;
					HermiteEdge edge8 = null;
					
					
					//+x
					if(!oX){
						qPoint2.set(p.x+dx+res, p.y+dy, p.z+dz);
						edge0 = ve.getEdge(qPoint1, qPoint2);				
						if(edge0 != null && edge0.intersection != null){
							c.setIntersection(x, y, z, AXIS.X, edge0.intersection);
							c.setNormal(x,y,z, AXIS.X, edge0.normal);
						}
					}
					
					//+y
					if(!oY){
						qPoint2.set(p.x+dx, p.y+dy+res, p.z+dz);
						edge4 = ve.getEdge(qPoint1, qPoint2);
						if(edge4 != null && edge4.intersection != null){
							c.setIntersection(x, y, z, AXIS.Y, edge4.intersection);
							c.setNormal(x,y,z, AXIS.Y, edge4.normal);
						}
					}

					//+z
					if(!oZ){
						qPoint2.set(p.x+dx, p.y+dy, p.z+dz+res);
						edge8 = ve.getEdge(qPoint1, qPoint2);
						if(edge8!=null && edge8.intersection != null){
							c.setIntersection(x, y, z, AXIS.Z, edge8.intersection);
							c.setNormal(x,y,z, AXIS.Z, edge8.normal);
						}
					}
					
						if (edge0 != null) {
//							if(edge0.intersection!=null){
//								Vector3f v = new Vector3f(qPoint1);
//								v.addLocal(0f, res*edge0.intersection,0f);
//								System.out.println("I0: "+v);
//							}
							c.setType(x, y, z, edge0.t1);
							c.setType(x + 1, y, z, edge0.t2);
						}
						if (edge4 != null) {
//							if(edge4.intersection!=null){
//								Vector3f v = new Vector3f(qPoint1);
//								v.addLocal(0f, res*edge4.intersection,0f);
//								System.out.println("I4: "+v);
//							}
							
							c.setType(x, y, z, edge4.t1);
							c.setType(x, y + 1, z, edge4.t2);
						}
						
						if (edge8 != null) {
//							if(edge8.intersection!=null){
//								Vector3f v = new Vector3f(qPoint1);
//								v.addLocal(0f, res*edge8.intersection,0f);
//								System.out.println("I4: "+v);
//							}
							
							c.setType(x, y, z, edge8.t1);
							c.setType(x, y, z + 1, edge8.t2);
						}
					
					//Inconsistency resolution
					
					
					checkEdge(edge0);
					checkEdge(edge4);
					checkEdge(edge8);
					
					
				}
			}
		}
		
		
		return c;
	}
	
	
	
	protected void checkEdge(HermiteEdge he){
		if(he != null && he.t1 != he.t2 && he.intersection == null){
			throw new RuntimeException("Edge contains transistion. But not intersection found!");
		}
	}
}
