package idea;

import java.util.HashMap;
import java.util.Map;

import VoxelSystem.Hermite.HermiteEdge;
import VoxelSystem.Hermite.HermiteExtractor;

import com.jme3.math.Vector3f;

public class Chunk implements HermiteGrid{
	private class Pair{
		int i,j;
		public Pair(int i, int j){
			this.i = i;
			this.j = j;
		}
		
		public boolean equals(Object o){
			Pair p = (Pair)o;
			if(p.i==i){
				if(p.j == j){
					return true;
				}
			}else if(p.j == i){
				if(p.i == j){
					return true;
				}
			}
			
			return false;
		}
		
		public int hashCode(){
			return i*j;
		}
	}
	
	
	public final int width,hieght,depth,zOffSet;
//	float [] densities; //Clamped densities between [0-1] and -1
	int [][][] types;
	
	Map<Pair, Float> intersections; 
	Map<Pair, Vector3f> normals;
	
	public Chunk(int width,int height,int depth){
		this.width = width;
		this.hieght = height;
		this.depth = depth;
		zOffSet = width*height;
		types = new int[width][height][depth];
		intersections = new HashMap<Pair, Float>();
		normals = new HashMap<Pair, Vector3f>();
	}
	
	
	
	@Override
	public int getType(int x, int y, int z) {
		return types[x][y][z];
	}

	@Override
	public Float getIntersection(int x, int y, int z, int a) {
		int i = index(x,y,z);
		int j = index(x,y,z,a);
		return getIntersection(i,j);
	}

	@Override
	public Vector3f getNormal(int x, int y, int z, int a) {
		int i = index(x,y,z);
		int j = index(x,y,z,a);
		return normals.get(new Pair(i,j));
	}

	
	@Override
	public void extract(Vector3f p, float res, HermiteExtractor he) {
		
		Vector3f qPoint = new Vector3f();
		Vector3f qPoint2 = new Vector3f();
		//8 Cube Points.
//		Vector3f [] cP = new Vector3f[]{
//			new Vector3f(),new Vector3f(),new Vector3f(),new Vector3f(),
//			new Vector3f(),new Vector3f(),new Vector3f(),new Vector3f()
//		};
		
		for (int y = -1; y < hieght; y++) {
			float dy = y * res;

			for (int x = -1; x < width; x++) {
				float dx = x * res;

				for (int z = -1; z < depth; z++) {
					float dz = z * res;

					//get up(+y) and to the right(+x, +z)
					qPoint.set(p.x+dx,p.y+dy,p.z+dz);
					
					if(x != -1 && z != -1 && y != -1){
						types[x][y][z] = he.getPoint(qPoint).material;
					}
					
					
					
					//+x
					qPoint2.set(p.x+dx+res,p.y+dy,p.z+dz);
					HermiteEdge edge0 = he.getEdge(qPoint,qPoint2);
					
//					if(x == 10 && y == 1 && z == 11){
//						qPoint2.set(p.x+dx,p.y+dy+res,p.z+dz);
//						System.out.println("Enrty "+types[x][y][z]+" vs "+ he.getPoint(qPoint2).material);
//					}
					
					if(edge0 != null){
						float f = getLerp(qPoint, qPoint2, edge0.intersection);
						putIntersection(index(x,y,z), index(x+1,y,z), f);
						putNormal(index(x,y,z), index(x+1,y,z), edge0.normal);
					}
					
					//+y
					qPoint2.set(p.x+dx,p.y+dy+res,p.z+dz);
					HermiteEdge edge4 = he.getEdge(qPoint,qPoint2);
					
					if(edge4 != null){
						float f = getLerp(qPoint, qPoint2, edge4.intersection);
						putIntersection(index(x,y,z), index(x,y+1,z), f);
						putNormal(index(x,y,z), index(x,y+1,z), edge4.normal);
					}
					

					//+z
					qPoint2.set(p.x+dx,p.y+dy,p.z+dz+res);
					HermiteEdge edge8 = he.getEdge(qPoint,qPoint2);
					
					if(edge8 != null){
						float f = getLerp(qPoint, qPoint2, edge8.intersection);
						putIntersection(index(x,y,z), index(x,y,z+1), f);
						putNormal(index(x,y,z), index(x,y,z+1), edge8.normal);
					}
					
				}
			}
		}
		
	}
	
//	public Chunk upSample(int factor){
//		
//		return null;
//	}
	
	public Chunk downSample(){
		Chunk c = new Chunk(width/2,hieght/2,depth/2);
//		Vector3f scaleDiff = new Vector3f();
		
		//Assuming power of 2. Just copy material data
		for(int x = 0; x < c.width; x++){
			for(int y = 0; y < c.hieght; y++){
				for(int z = 0; z < c.depth; z++){
					c.types[x][y][z] = types[x*2][y*2][z*2]; 
				}
			}
		}
		
		//Now evaluate edge changes and try to approximate intersection point and normal
		
		for(int x = 0; x < c.width-1; x++){
			for(int y = 0; y < c.hieght-1; y++){
				for(int z = 0; z < c.depth-1; z++){
					
					Float f;
					Vector3f normal;
					
					
					if(c.types[x][y][z] != c.types[x][y][z+1]){
						//A intersection must have fallen on the original between [x][y][z] and [x][y][z+2]
						if(types[x*2][y*2][z*2] != types[x*2][y*2][z*2+1]){
							f = getIntersection(index(x*2,y*2,z*2), index(x*2,y*2,z*2+1)) * .5f;
							normal = getNormal(x*2,y*2,z*2,3);
						}else{
							f = getIntersection(index(x*2,y*2,z*2+1), index(x*2,y*2,z*2+2)) * .5f + .5f;
							normal = getNormal(x*2,y*2,z*2+1,3);
						}
						c.putIntersection(c.index(x,y,z), c.index(x,y,z+1), f);
						c.putNormal(c.index(x,y,z), c.index(x,y,z+1), normal);
						
					}
					
					if(c.types[x][y][z] != c.types[x][y+1][z]){
						
						if(types[x*2][y*2][z*2] != types[x*2][y*2+1][z*2]){
							f = getIntersection(index(x*2,y*2,z*2), index(x*2,y*2+1,z*2))  * .5f;
							normal = getNormal(x*2,y*2,z*2,2);
						}else{
							f = getIntersection(index(x*2,y*2+1,z*2), index(x*2,y*2+2,z*2)) * .5f + .5f;
							normal = getNormal(x*2,y*2+1,z*2,2);
							
						}
						c.putIntersection(c.index(x,y,z), c.index(x,y+1,z), f);
						c.putNormal(c.index(x,y,z), c.index(x,y+1,z), normal);
						
					}
					
					if(c.types[x][y][z] != c.types[x+1][y][z]){
						if(types[x*2][y*2][z*2] != types[x*2+1][y*2][z*2]){
							f = getIntersection(index(x*2,y*2,z*2), index(x*2+1,y*2,z*2)) * .5f;
							normal = getNormal(x*2,y*2,z*2,1);
						}else{
							f = getIntersection(index(x*2+1,y*2,z*2), index(x*2+2,y*2,z*2))*.5f + .5f;
							normal = getNormal(x*2+1,y*2,z*2,1);
						}
						
						c.putIntersection(c.index(x,y,z), c.index(x+1,y,z), f);
						c.putNormal(c.index(x,y,z), c.index(x+1,y,z), normal);
					}
					
				}
			}
		}
		
		
		return c;
	}
	
	/**
	 * returns percent from v1 to v2
	 */
	private static float getLerp(Vector3f v1, Vector3f v2, Vector3f mp){
		Vector3f l = v1.subtract(v2);
		Vector3f n = v2.subtract(mp);
		return 1.0f - (Math.abs(n.x+n.y+n.z)/Math.abs(l.x+l.y+l.z));
	}
	
	
	
	
	
	/////// Helpers ///////
	private int index(int x, int y, int z){

		
		return x + y*width + zOffSet*z;
	}
	
	private int index(int x, int y, int z, int a){
		int j;
		if(a == 1){
			j = index(x+1,y,z);
		}else if(a == -1){
			j = index(x-1,y,z);
		}else if(a == 2){
			j = index(x,y+1,z);
		}else if(a == -2){
			j = index(x,y-1,z);
		}else if(a == 3){
			j = index(x,y,z+1);
		}else if(a == -3){
			j = index(x,y,z-1);
		}else{
			throw new RuntimeException("Invalid axis");
		}
		return j;
	}
	
	
	private void putIntersection(int i, int j,Float f){
		Pair p = new Pair(i,j);

		// always stored intersection from low to high index
		if(j > i && f !=null){
			f = (1.0f-f);
		}
		
		
		intersections.put(p, f);
	}
	
	private Float getIntersection(int i, int j){
		Pair p = new Pair(i,j);
		
		// always stored intersection from low to high index
		Float f = intersections.get(p);
		
		//Check if float needs to be changed
		if(j > i && f !=null){
			f = (1.0f-Math.abs(f));
		}
		
		return f;
	}
	
	private void removeIntersection(int i, int j){
		Pair p = new Pair(i,j);
		intersections.remove(p);
	}
	
	private void putNormal(int i, int j,Vector3f normal){
		Pair p = new Pair(i,j);
		
//		if(normal == null || normal.length() > 1.01f || normal .length() < .98f){
//			System.out.println("Entry");
//		}
		
		normals.put(p, normal);
	}
	
	private void removeNormal(int i, int j){
		Pair p = new Pair(i,j);
		normals.remove(p);
	}



	@Override
	public int getWidth() {
		return width;
	}


	@Override
	public int getHieght() {
		return hieght;
	}

	@Override
	public int getDepth() {
		return depth;
	}
}
