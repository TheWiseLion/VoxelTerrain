package VoxelSystem.VoxelObjects;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import VoxelSystem.VoxelSystemTables;
import VoxelSystem.VoxelSystemTables.AXIS;
import VoxelSystem.MeshBuilding.MeshOutput;
import VoxelSystem.SurfaceExtractors.ExtractorUtils;
import VoxelSystem.SurfaceExtractors.QuadExtractor;
import VoxelSystem.VoxelData.Chunk;
import VoxelSystem.VoxelData.VoxelExtractor;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

/***
 * Class to be used with paging system.
 * Organization is laid out in @link{}
 */
public class VoxelNode extends Chunk{
	public final int vOffX, vOffY, vOffZ;
	public final int hash;
	Map<Integer,Vector3f> isoPoints;
	List<Geometry> generated;
	
	
	/****
	 * Takes world space voxel off sets (vOffX, vOffY, vOffZ) which defines lower left corner.
	 */
	public VoxelNode(int vOffX,int vOffY,int vOffZ, int width, int height, int depth, float vSize,int hash) {
		super(null, width, height, depth, vSize);
		this.corner = new Vector3f(vOffX * vSize, vOffY * vSize, vOffZ * vSize);
//		this.owner=pvo;
		this.zOffSet = (width+1)*(height+1);
		isoPoints = new HashMap<Integer, Vector3f>();
		this.vOffX = vOffX;
		this.vOffY = vOffY;
		this.vOffZ = vOffZ;
		this.hash = hash;
	}
	
	@Override
	protected void initializeArray(){
		types = new int[width][height][depth];
		//TODO: no.
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < depth; z++) {
					setType(x, y, z, -1);
				}
			}
		}
	}
	
	@Override
	public void extract(VoxelExtractor ve){
		throw new RuntimeException("Incorrect use.");
	}
	
	public void extractVolume(VoxelExtractor ve, int vMin[], int[] vMax){
		GridUtils.extractData(this, ve, vMin, vMax);
	}
	
	
	@Override
	protected int index(int x, int y, int z){
		return x + y*(width+1) + zOffSet*z;
	}
	
	@Override
	public void setType(int x, int y, int z, int type) {
		this.types[x][y][z] = type;
	}
	
	@Override
	public int getType(int x, int y, int z) {
		return this.types[x][y][z];
	}

	@Override
	public BoundingBox getBoundingBox() {
		throw new RuntimeException("Thall shall not call.");
	}
	
	public void genIsopoints(PagingVoxelObject pvo, Vector3f cP[], List<Vector3f> edges, List<Vector3f> normals, int materials[], BoundingBox bb, float error){
		isoPoints.clear();
		int cx = this.vOffX/width;
		int cy = this.vOffY/height;
		int cz = this.vOffZ/depth;
		
		//Get neighboring chunks
		VoxelNode n[] = new VoxelNode[3];
		n[0] = pvo.getChunk(cx+1, cy, cz);
		n[1] = pvo.getChunk(cx, cy+1, cz);
		n[2] = pvo.getChunk(cx, cy, cz+1);
		
		Vector3f p = this.getCorner();
		//(DIM+1)(DIM+1) 'external' edges
		for(int x = 0; x < width; x++){
			cP[0].x = cP[3].x = cP[4].x = cP[7].x = p.x+(x*vSize);
			cP[1].x = cP[2].x = cP[5].x = cP[6].x = p.x+((x+1)*vSize);
			
			for(int y = 0; y < height; y++){
				cP[0].y = cP[1].y = cP[4].y = cP[5].y = p.y +(y*vSize);
				cP[2].y = cP[3].y = cP[6].y = cP[7].y =  p.y+((y+1)*vSize);
				
				for(int z = 0; z < depth; z++){
					cP[0].z = cP[1].z = cP[2].z = cP[3].z = p.z+(z*vSize);
					cP[4].z = cP[5].z = cP[6].z = cP[7].z = p.z+((z+1)*vSize);
					
					int edgeInfo = getEdges(x, y, z, cP, n, materials, edges, normals, pvo);
					Vector3f v = getIsoPoint(edgeInfo, cP, edges, normals, bb, error);
					if(v != null){
						isoPoints.put(index(x,y,z), v);
					}
				}
			}
		}
		
	}
	
	public Map<Integer,MeshOutput> genMesh(PagingVoxelObject pvo, int [] materials){
		//TODO: 
		Map<Integer,MeshOutput> meshOutputs = new HashMap<Integer,MeshOutput>(); 
		VoxelNode n[] = new VoxelNode[6];
		Vector3f normals[] = new Vector3f[3];
		int cx = this.vOffX/width;
		int cy = this.vOffY/height;
		int cz = this.vOffZ/depth;
		
		n[0] = pvo.getChunk(cx+1, cy, cz);
		n[1] = pvo.getChunk(cx, cy+1, cz);
		n[2] = pvo.getChunk(cx, cy, cz+1);
		
		n[3] = pvo.getChunk(cx-1, cy, cz);
		n[4] = pvo.getChunk(cx, cy-1, cz);
		n[5] = pvo.getChunk(cx, cy, cz-1);
		
		for(int x = -1; x <= width; x++){
			for(int y = -1; y <= height; y++){
				for(int z = -1; z <= depth; z++){
//					if(x == 10 && y ==16 && z == 2){
//						System.out.println("Entry");
//					}
					
					getMaterials(x, y, z, materials, n, pvo);
					Vector3f cube000 = getIsoPoint(x, y, z, n, pvo);
					Vector3f cube100 = getIsoPoint(x+1, y, z, n, pvo);
					Vector3f cube010 = getIsoPoint(x, y+1, z, n, pvo);
					Vector3f cube001 = getIsoPoint(x, y, z+1, n, pvo);
					Vector3f cube110 = getIsoPoint(x+1, y+1, z, n, pvo);
					Vector3f cube101 = getIsoPoint(x+1, y, z+1, n, pvo);
					Vector3f cube011 = getIsoPoint(x, y+1, z+1, n, pvo);
					//Normal on edges: 5, 6, 10

					normals[0] = getNormal(x+1, y, z+1, AXIS.Y, n, pvo); //edge 5
					normals[1] = getNormal(x, y+1, z+1, AXIS.X, n, pvo); //edge 6
					normals[2] = getNormal(x+1, y+1, z, AXIS.Z, n, pvo); //edge 10
					boolean support = x<0 || y < 0 || z<0 || x==width || y == height || z == depth;
					
					getTriangles(cube000,cube100,cube010,cube001,cube110,cube101,cube011,materials,normals,meshOutputs,support);
				}
			}
		}
		
		return meshOutputs;
	}
	
/////////////////////////////////////////////////////////////////////////	
/////////////////////////////////////////////////////////////////////////
				////	HELPER FUNCTIONS	////
/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
	
	private int getEdges(int x, int y, int z, Vector3f cP[],VoxelNode []n,int [] materials, List<Vector3f> edges, List<Vector3f> normals,PagingVoxelObject pvo ){
		edges.clear();
		normals.clear();
		getMaterials(x, y, z, materials, n, pvo);
		
		///Now get edges:
		int edgeInfo = VoxelSystemTables.getEdgeFromMaterials(materials);
		for (int i = 0; i < 12; i++){ // 12 edges
			if ((edgeInfo & (1 << i)) == 0){
				continue;
			}
			
			// 'i' is the edge #
			// e0, e1 are the cube edges
			int e0 = VoxelSystemTables.iTable[i*2];
			int e1 = VoxelSystemTables.iTable[i*2 +1];
			
			int dx = VoxelSystemTables.cTable[e0*3];
			int dy = VoxelSystemTables.cTable[e0*3 +1];
			int dz = VoxelSystemTables.cTable[e0*3 + 2];
			
			AXIS axis = VoxelSystemTables.aTable[i]; //axis of edge
			
			Float f = getIntersection(x + dx, y + dy, z + dz, axis,n,pvo);
			normals.add(getNormal(x + dx, y + dy, z + dz, axis,n,pvo));
			
			if(f == null){
				System.out.println("Entry "+(x+dx)+","+(y+dy)+","+(z+dz));
				System.out.println("Entry "+(x+dx+this.vOffX)+","+(y+dy+this.vOffY)+","+(z+dz+this.vOffZ));
			}
			
			edges.add(lerp(cP[e0],cP[e1],f));
			
		}
		return edgeInfo;
	}
	
	private Vector3f lerp(Vector3f v1, Vector3f v2,float f){
		Vector3f v =  new Vector3f();
		v.x = v1.x + (v2.x - v1.x)*f;
		v.y = v1.y + (v2.y - v1.y)*f;
		v.z = v1.z + (v2.z - v1.z)*f;
		return v;
	}
	
	private void getMaterials(int x, int y, int z, int  [] materials, VoxelNode [] n, PagingVoxelObject pvo){
		materials[0] = getType(x, y, z, n, pvo);
		materials[1] = getType(x+1, y, z, n, pvo);
		materials[2] = getType(x+1, y+1, z, n, pvo);
		materials[3] = getType(x, y+1, z, n, pvo);
		materials[4] = getType(x, y, z+1, n, pvo);
		materials[5] = getType(x+1, y,z+1, n, pvo);
		materials[6] = getType(x+1, y+1, z+1, n, pvo);
		materials[7] = getType(x, y+1, z+1, n, pvo);
	}
	
	
	
	private Vector3f getNormal(int x, int y, int z, AXIS a, VoxelNode []n, PagingVoxelObject pvo){
		if(x < 0 || y < 0 || z < 0){
			return pvo.getNormal(x+this.vOffX, y+this.vOffY, z+this.vOffZ,a);
			
		}else{
			boolean oX = x >= width ? true:false;
			boolean oY = y >= height ? true:false;
			boolean oZ = z >= depth ? true:false;
			if(!oX && !oY && !oZ){
				return getNormal(x, y, z, a);
			}else if(oX && !oZ && ! oY){
				return n[0].getNormal(x - this.width, y, z, a);
			}else if(oY && !oX && !oZ){
				return n[1].getNormal(x, y - this.height, z, a);
			}else if(oZ && !oX && !oY){
				return n[2].getNormal(x, y, z - this.depth, a);
			}else{//slowest case.
				return pvo.getNormal(x+this.vOffX, y+this.vOffY, z+this.vOffZ,a);
			}
		}
	}

	private Float getIntersection(int x, int y, int z, AXIS a, VoxelNode []n, PagingVoxelObject pvo){
		boolean oX = x >= width ? true:false;
		boolean oY = y >= height ? true:false;
		boolean oZ = z >= depth ? true:false;
		if(!oX && !oY && !oZ){
			return getIntersection(x, y, z, a);
		}else if(oX && !oZ && ! oY){
			return n[0].getIntersection(x - this.width, y, z, a);
		}else if(oY && !oX && !oZ){
			return n[1].getIntersection(x, y - this.height, z, a);
		}else if(oZ && !oX && !oY){
			return n[2].getIntersection(x, y, z - this.depth, a);
		}else{//slowest case.
			return pvo.getIntersection(x+this.vOffX, y+this.vOffY, z+this.vOffZ,a);
		}
	}
	
	private int getType(int x, int y, int z, VoxelNode n[], PagingVoxelObject pvo){
		
		if(x < 0 || y < 0 || z < 0){
			return pvo.getType(x+this.vOffX,y+this.vOffY,z+this.vOffZ);
			
		}else{
			boolean oX = x >= width ? true:false;
			boolean oY = y >= height ? true:false;
			boolean oZ = z >= depth ? true:false;
			if(!oX && !oY && !oZ){
				return this.getType(x, y, z);
			}else if(oX && !oZ && ! oY){
				return n[0].getType(x - this.width, y, z);
			}else if(oY && !oX && !oZ){
				return n[1].getType(x, y - this.height, z);
			}else if(oZ && !oX && !oY){
				return n[2].getType(x, y, z - this.depth);
			}else{//slowest case.
				return pvo.getType(x+this.vOffX,y+this.vOffY,z+this.vOffZ);
			}
		}
	}
	
	private Vector3f getIsoPoint(int x, int y, int z, VoxelNode [] n, PagingVoxelObject pvo){
		if(x < 0 || y < 0 || z < 0){
			return pvo.getIsopoint(x+this.vOffX,y+this.vOffY,z+this.vOffZ);
		}
		
		boolean oX = x >= width ? true:false;
		boolean oY = y >= height ? true:false;
		boolean oZ = z >= depth ? true:false;
		if(!oX && !oY && !oZ){
			return this.isoPoints.get(index(x,y,z));
		}else if(oX && !oZ && ! oY){
			return n[0].isoPoints.get(n[0].index(x - this.width, y, z));
		}else if(oY && !oX && !oZ){
			return n[1].isoPoints.get(n[1].index(x, y - this.height, z));
		}else if(oZ && !oX && !oY){
			return n[2].isoPoints.get(n[2].index(x, y, z - this.depth));
		}else{//slowest case.
			return pvo.getIsopoint(x+this.vOffX, y+this.vOffY, z+this.vOffZ);
		}
	}
	
	private Vector3f getIsoPoint(int edgeInfo, Vector3f [] corners, List<Vector3f> edges, List<Vector3f> normals, BoundingBox bb,float error){ //int[] materials,
		
		if(edgeInfo != 0){
			Vector3f center = corners[0].add(corners[6]).multLocal(.5f);
			bb.setCenter(center);
			
			
			Vector3f isoPoint = ExtractorUtils.surfaceContour(corners,
								edges.toArray(new Vector3f[edges.size()]),
								normals.toArray(new Vector3f[normals.size()]), 
								bb, error);
			
		
			
			return isoPoint;//new SurfacePoint(isoPoint, type);
		}
		
		
		return null;
	}
	
	
	
	private void getTriangles(Vector3f c000,Vector3f c100,Vector3f c010,Vector3f c001,Vector3f c110,Vector3f c101,Vector3f c011, int [] materials, Vector3f[] normals, Map<Integer, MeshOutput> meshOutputs,boolean support){
		int eI = VoxelSystemTables.getEdgeFromMaterials(materials); 
		Vector3f v1= c000,v2,v3,v4;
		//if edge 5
		if((eI & (1<<5)) != 0){
			v2 = c100;
			v3 = c101;
			v4 = c001;
			
			if(v3 == null){
				throw new RuntimeException("ARG");
			}
			
			if(materials[6] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,normals[0],meshOutputs,false, materials[5], support);
			}else if(materials[5] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,normals[0],meshOutputs,true, materials[6], support);
			}else{
				// Sub-Surface Quad. Check if this is marked for generation
//				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,1);
			}
			
		}
		
		//if edge 6
		if((eI & (1<<6)) != 0){
			v2 = c010;
			v3 = c011;
			v4 = c001;
			
			if(materials[7] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,normals[1],meshOutputs,false,materials[6], support);
//				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,hc.materials[6]);
			}else if(materials[6] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,normals[1],meshOutputs,true,materials[7], support);
//				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, false,hc.materials[7]);
			}else{ // Wrapping does not matter for material quads.
				// Sub-Surface Quad. Check if this is marked for generation
//				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,1);
			}
		}
		
		//if edge 10
		if((eI & (1<<10)) != 0){
			v2 = c010;
			v3 = c110;
			v4 = c100;
			
			if(materials[6] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,normals[2],meshOutputs,false,materials[2], support);
				//QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,hc.materials[2]);
			}else if(materials[2] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,normals[2],meshOutputs,true,materials[6], support);
			}else{ 
				// Sub-Surface Quad. Check if this is marked for generation
				//QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,1);
			}
		}
	}
	
}
