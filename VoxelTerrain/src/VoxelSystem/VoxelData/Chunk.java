package VoxelSystem.VoxelData;

import java.util.HashMap;
import java.util.Map;

import VoxelSystem.Operators.CSGHelpers;
import VoxelSystem.VoxelSystemTables.AXIS;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

/***
 * Class contains the information to exact any isosurface within (or on) the defining bounding box.
 * getCorner defines the minimum of this bounding box. 
 * @author 0xFFFF
 *
 */
public class Chunk extends ExtractorBase implements VoxelGrid{
	protected class Pair{
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
	
	
	public final int width,height,depth;
	protected int zOffSet;
	protected Vector3f corner;
	protected float vSize;
	protected int [][][] types;
	
	//TODO: HermiteEdge instead.
	protected Map<Pair, Float> intersections; 
	protected Map<Pair, Vector3f> normals;
	
	public Chunk(Vector3f corner, int width,int height,int depth, float size){
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.vSize = size;
		this.corner = corner;
		zOffSet = width*height;
		initializeArray();
		intersections = new HashMap<Pair, Float>();
		normals = new HashMap<Pair, Vector3f>();
	}
	
	protected void initializeArray(){
		types = new int[width+2][height+2][depth+2];
		//TODO: no.
		for (int x = -1; x < width + 1; x++) {
			for (int y = -1; y < height + 1; y++) {
				for (int z = -1; z < depth + 1; z++) {
					setType(x, y, z, -1);
				}
			}
		}
	}


	
	////////////// TO-DO //////////////
	@Override
	public HermiteEdge getEdge(Vector3f p1, Vector3f p2) {
		Vector3f v1 = p1.subtract(corner);
		Vector3f v2 = p2.subtract(corner);
		
		//Expect slight floating point errors:
		int x1 = round(v1.x/vSize);
		int y1 = round(v1.y/vSize);
		int z1 = round(v1.z/vSize);
		
		//Expect slight floating point errors:
		int x2 = round(v2.x/vSize);
		int y2 = round(v2.y/vSize);
		int z2 = round(v2.z/vSize);
		
		//TODO: slightly better method: select first then do based on axis
		//TODO: Support arbitrary (cubic) scaling 
		
		int vDifference;
		AXIS a;
		if(x1 != x2){
			vDifference= x1-x2;
			a =AXIS.X;
			
		}else if(y1 != y2){
			vDifference= y1-y2;
			a =AXIS.Y;
			
		}else if(z1 != z2){
			vDifference= z1-z2;
			a =AXIS.Z;
			
		}else{
			//Probably arbitrary scaling... which should be legal.....
			throw new RuntimeException("Do not differ by axis");
		}
		
		
		//Little error checking
		if( (x1 != x2 && y1 != y2) || (y1 != y2 && z1 != z2) || (x1 != x2 && z1 != z2) ){
			throw new RuntimeException("Not axis aligned...");
		}
		
		//Get voxel types
		int m1 = getType(x1, y1, z1);
		int m2 = getType(x2, y2, z2);
		
		HermiteEdge he = new HermiteEdge(m1,m2);
		//If 2 different materials interestion must exist!
		if(m1 != m2){ 
			//If voxel difference is only 1 then this is easy.
			if(Math.abs(vDifference) == 1){
				he.normal = getNormal(x1,y1,z1,x2,y2,z2);
				he.intersection = getIntersection(x1,y1,z1,x2,y2,z2);
			}else{
			
				//Now we have to downsample for correct edge.
				//1 - Utilizes trivial downsampling (as opposed to trilinear)
				//2 - being consistent (sampling left from right == right to left)
				//	  To be consistent we will get edge closest to lowest voxel value
				//	  aka vDifference will always be positive
				
				if(vDifference < 0 ){
					getDownsample(x2,y2,z2, -vDifference, a, he);
				}else{
					getDownsample(x1,y1,z1, vDifference, a, he);
				}
			}
		}
		
		return he;
	}

	private int round(float x){
		int r;
		float f = Math.abs(x);
		if( f - (int)f >= .5){
			r = (int)f + 1;
		}else{
			r = (int)f;
		}
		
		if(x < 0){ //add back the sign
			r = -r;
		}
		
		return r;
	}
	

	@Override
	public void extract(VoxelExtractor ve) {
		super.extract(ve,this);
	}
	

	
	///////////////////Getters//////////////////////
	
	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox(corner, corner.add( (float) (width-1)*vSize, (float) (height-1)*vSize,(float) (depth-1) * vSize));
	}
	
	@Override
	public Vector3f getCorner() {
		return corner;
	}



	@Override
	public float getVoxelSize() {
		return this.vSize;
	}
	
	
	@Override
	public int getType(int x, int y, int z) {
		x+=1;
		y+=1;
		z+=1;
		return types[x][y][z];
	}

	@Override
	public Float getIntersection(int x, int y, int z, AXIS a) {
		int i = index(x,y,z);
		int j = index(x,y,z,a);
		return getIntersection(i,j);
	}

	@Override
	public Vector3f getNormal(int x, int y, int z, AXIS a) {
		int i = index(x,y,z);
		int j = index(x,y,z,a);
		return normals.get(new Pair(i,j));
	}
	

	public Float getIntersection(int x, int y, int z, int x1, int y1, int z1) {
		int i = index(x,y,z);
		int j = index(x1,y1,z1);
		return getIntersection(i,j);
	}

	public Vector3f getNormal(int x, int y, int z, int x1, int y1, int z1) {
		int i = index(x,y,z);
		int j = index(x1,y1,z1);
		return normals.get(new Pair(i,j));
	}

	////////////////Setters////////////////////
	
	@Override
	public void setType(int x, int y, int z, int type) {
		x+=1;
		y+=1;
		z+=1;
		
		this.types[x][y][z] = type;
	}



	@Override
	public void setNormal(int x, int y, int z, AXIS a, Vector3f n) {
		int i = index(x,y,z);
		int j = index(x,y,z,a);
		putNormal(i, j, n);
	}



	@Override
	public void setIntersection(int x, int y, int z, AXIS a, float f) {
		int i = index(x,y,z);
		int j = index(x,y,z,a);
		putIntersection(i, j, f);
	}
	
	
	
	/////////////// Helpers ////////////////
	protected int index(int x, int y, int z){
		x+=1;
		y+=1;
		z+=1;
		return x + y*width + zOffSet*z;
	}
	
	private int index(int x, int y, int z, AXIS a){
		int j;
		if(a == AXIS.X){
			j = index(x+1,y,z);
//		}else if(a == AXIS.NX){
//			j = index(x-1,y,z);
		}else if(a == AXIS.Y){
			j = index(x,y+1,z);
//		}else if(a == AXIS.NY){
//			j = index(x,y-1,z);
		}else if(a == AXIS.Z){
			j = index(x,y,z+1);
//		}else if(a == AXIS.NZ){
//			j = index(x,y,z-1);
		}else{
			throw new RuntimeException("Invalid axis");
		}
		return j;
	}
	
	
	private void putIntersection(int i, int j,Float f){
		Pair p = new Pair(i,j);

//		if(i==26 && j ==31 || i == 31 && j== 26 ){
//			System.out.println("Entry");
//		}
		
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
	public int getHeight() {
		return height;
	}

	@Override
	public int getDepth() {
		return depth;
	}

	private HermiteEdge getDownsample(int x0, int y0, int z0, int difference, AXIS a, HermiteEdge he){
		
		//Step one find the first edge.
		int i =0;
		for(i = 0; i < difference; i++){
			
			if(getIntersection(x0,y0,z0, a) != null){
				he.intersection= getIntersection(x0,y0,z0, a);
				he.normal = getNormal(x0, y0, z0, a);
				break;
			}
			
			
			if(a == AXIS.X){
				x0 += 1;
			}else if(a == AXIS.Y){
				y0 += 1;
			}else{//(a == AXIS.Z)
				z0 += 1;
			}
			
		}
		
		//now recompute edge value
		// ith voxel 
		he.intersection = ((float)he.intersection/difference) + ((float)i/difference);
		return he;
	}



	
	
}
