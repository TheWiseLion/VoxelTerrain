package voxelsystem.voxelobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import voxelsystem.VoxelSystemTables.AXIS;
import voxelsystem.meshbuilding.MeshOutput;
import voxelsystem.operators.CSGOperator;
import voxelsystem.voxeldata.ExtractorBase;
import voxelsystem.voxeldata.HermiteEdge;
import voxelsystem.voxeldata.VoxelExtractor;
import voxelsystem.voxelmaterials.VoxelType;

import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;

public class PagingVoxelObject extends ExtractorBase{
	private class QueuedExtraction{
		QueuedExtraction(VoxelExtractor ve, VoxelNode w,BoundingBox bb){
			this.vn = w;
			this.ve = ve;
			this.bb = bb;
		}
		BoundingBox bb;
		VoxelExtractor ve;
		VoxelNode vn;
		public void preformExtraction(){
			BoundingBox box = bb;
			int vMin[] = new int[3];
			int vMax[] = new int[3];
			if(box != null){
				Vector3f min = box.getMin(new Vector3f());
				Vector3f max = box.getMax(new Vector3f());
				vMin[0] = ((int) (min.x/voxelSize)) - 1;
				vMin[1] = ((int) (min.y/voxelSize)) - 1;
				vMin[2] = ((int) (min.z/voxelSize)) - 1;
	
				vMax[0] = ((int) (max.x/voxelSize)) + 1;
				vMax[1] = ((int) (max.y/voxelSize)) + 1;
				vMax[2] = ((int) (max.z/voxelSize)) + 1;
			}else{
				throw new RuntimeException("Herpy derpy");
			}
			VoxelNode other = vn.deepCopy();
			other.extractVolume(ve,vMin,vMax);
			vn.copy(other);
//			vn.extractVolume(ve, vMin, vMax);
		}
	}
	
	/**
	 * Either a voxel extractor or
	 * a operation (forms CSG tree)
	 * @author 0xFFFF
	 *
	 */
	private static class WorldLayer{
		CSGOperator operator;
		VoxelExtractor [] args;
		WorldLayer(CSGOperator o,VoxelExtractor [] args){
			this.operator = o;
			this.args = args;
		}
		
	}
	
	int bitOffSet = 10;
	int CHUNK_MASK = 0x3FF;//10 bits -> ~1000 chunks
	int CHUNK_OFF = CHUNK_MASK/2;
	int CHUNK_DIM = 16;
	private float voxelSize;
	private boolean debug;
	Material debugMats[];
	
	/***
	 * This stores decompressed chunks.
	 * All of the chunk data exists.
	 */
	//Map<Integer, VoxelNode> ChunkCache;
	
	
	/***
	 * packed chunks
	 */
	public Map<Integer, VoxelNode> chunks;
	
	public LinkedHashSet<VoxelNode> renderReadyNodes;
	
	
//	LinkedList<WorldLayer> worldLayers;//To be applied in order c.extract()c.extract()c.extract()....
	
//	HashMap<Integer,Integer> chunkQueueStatus;//Maps chunk ID to # of extraction left to perform on it...
//	LinkedList<QueuedExtraction> extractionQueue;
	
	Vector3f loadDistance;
	Map<Integer, VoxelType> materials;
	//TODO: keep a list and recompute if overlap
	
	public PagingVoxelObject(float voxelSize, Map<Integer, VoxelType> iToM,Vector3f loadDistance){
		this.voxelSize = voxelSize;
		this.materials = iToM;
		this.loadDistance = loadDistance;
		
		renderReadyNodes = new LinkedHashSet<VoxelNode>();
//		worldLayers = new LinkedList<WorldLayer>();
//		
//		extractionQueue = new LinkedList<QueuedExtraction>();
//		chunkQueueStatus = new HashMap<Integer,Integer>();
		
		
		chunks = new HashMap<Integer, VoxelNode>();
	}
	
/////////////////////////////////////////////////////////////////////////	
/////////////////////////////////////////////////////////////////////////
				////	CORE FUNCTIONS	////
/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
	public void update(Node root, int maxWork){
		
		
		if(renderReadyNodes.size() > 0){
			float error = (float) (Math.sqrt(3)*voxelSize / 1000f);
			Vector3f cP[] = new Vector3f[]{
				new Vector3f(),new Vector3f(),new Vector3f(),new Vector3f(),
				new Vector3f(),new Vector3f(),new Vector3f(),new Vector3f()
			};
			BoundingBox bb= new BoundingBox();
			bb.setXExtent(voxelSize);
			bb.setYExtent(voxelSize);
			bb.setZExtent(voxelSize);
			
			List<Vector3f> normals = new ArrayList<Vector3f>();
			List<Vector3f> edges = new ArrayList<Vector3f>();
			int mats[] = new int[8];
			
			
			
			for(VoxelNode vn : renderReadyNodes){
				vn.genIsopoints(this, cP, edges, normals, mats, bb, error);
			}
			
			
			for(VoxelNode vn : renderReadyNodes){
				
				if(vn.generated!=null){
					for(Geometry g : vn.generated){
						root.detachChild(g);
					}
				}
				
				Map<Integer,MeshOutput> meshes = vn.genMesh(this, mats);
				vn.generated = new ArrayList<Geometry>();
				for(Integer i : meshes.keySet()){
					MeshOutput mo = meshes.get(i);
					Mesh [] me = mo.compile(debug);
					me[0].updateBound();
					me[0].updateCounts();
					if(me[0].getTriangleCount() > 0){
						Geometry g = new Geometry();
						g.setMesh(me[0]);
						g.setMaterial(this.materials.get(i).material);
						g.setShadowMode(ShadowMode.CastAndReceive);
						vn.generated.add(g);
						root.attachChild(g);
					}
				}
			}
		}
		
		renderReadyNodes.clear();
		
		
	}
	
	public VoxelNode getChunk(int x, int y, int z){
		int key = hash(x, y, z);
		VoxelNode vn = this.chunks.get(key);
		if(vn == null){
			vn = this.createChunkFromKey(key);
			this.chunks.put(key, vn);
		}
		return vn;
	}
	
	/***
	 * @param operator
	 * @param args - any additional arguments for the operator (e.g. other volumes)
	 * @param bb - bounding volume used to select which chunks to apply the operator (with the given arguments)
	 */
	public void preformOperation(CSGOperator operator, BoundingBox bb,VoxelExtractor ... args){
		BoundingBox box = bb;
		if(box == null){
			
			return;
		}
		
		Vector3f min = box.getMin(new Vector3f());
		Vector3f max = box.getMax(new Vector3f());
		int [] vMax = new int[3];
		int [] vMin = new int[3];
		
		vMin[0] = ((int) (min.x/voxelSize)) - 1;
		vMin[1] = ((int) (min.y/voxelSize)) - 1;
		vMin[2] = ((int) (min.z/voxelSize)) - 1;

		vMax[0] = ((int) (max.x/voxelSize)) + 1;
		vMax[1] = ((int) (max.y/voxelSize)) + 1;
		vMax[2] = ((int) (max.z/voxelSize)) + 1;
		
		
		List<Integer> chunkKeys = this.chunksFromAABB(box);
		VoxelExtractor [] args2 = new VoxelExtractor[args.length+1]; 
		if(args != null){
			for(int i=0; i< args.length; i++){
				args2[i+1]=args[i];
			}
		}
		
		for(int chunk : chunkKeys){
			VoxelNode c = this.chunks.get(chunk);
			
			if(c == null){
				c = createChunkFromKey(chunk);
				this.chunks.put(chunk,c);
			}
			
			
			VoxelExtractor ve;
			args2[0]=this;
			ve = operator.operate(args2);
			c.extractVolume(ve,  vMin, vMax);
			this.renderReadyNodes.add(c);
		}
		
	}
	
	public void enableDebugging(Material normalColor, Material tangentColor, Material bitangent){
		debug = true;
		debugMats = new Material[3];
		debugMats[0] = normalColor;
		debugMats[1] = normalColor;
		debugMats[2] = normalColor;
	}
	
	public void disableDebugging(){
		debug = false;
		debugMats = null;
	}
	
	@Override
	public BoundingBox getBoundingBox() {
		return null;
	}
	
	public float getVoxelSize(){
		return voxelSize;
	}
	
	public int getType(float x, float y, float z){
		return getType((int)round(x/voxelSize), (int)round(y/voxelSize), (int)round(z/voxelSize));
	}
	
/////////////////////////////////////////////////////////////////////////	
/////////////////////////////////////////////////////////////////////////
					////	HELPER FUNCTIONS	////
/////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
	int getType(int wx, int wy, int wz){
//		System.out.println("AIEOUU asking about:" +(wx*voxelSize) +","+(wy*voxelSize)+","+(wz*voxelSize));
		int chunkX = (int) Math.floor((double)wx/CHUNK_DIM);
		int chunkY = (int) Math.floor((double)wy/CHUNK_DIM);
		int chunkZ = (int) Math.floor((double)wz/CHUNK_DIM);
		VoxelNode vn = getChunk(chunkX,chunkY,chunkZ);
		if(vn == null){
			return -1;
		}else{
			//ask in chunk local space:
			int cx = wx - chunkX*CHUNK_DIM;
			int cy = wy - chunkY*CHUNK_DIM;
			int cz = wz - chunkZ*CHUNK_DIM;
			
			return vn.getType(cx,cy,cz);
		}
	}
	
	Float getIntersection(int wx, int wy, int wz, AXIS a){
		
		int chunkX = (int) Math.floor((double)wx/CHUNK_DIM);
		int chunkY = (int) Math.floor((double)wy/CHUNK_DIM);
		int chunkZ = (int) Math.floor((double)wz/CHUNK_DIM);
		VoxelNode vn = getChunk(chunkX,chunkY,chunkZ);
		if(vn == null){
			return null;
		}else{
			//ask in chunk local space:
			int cx = wx - chunkX*CHUNK_DIM;
			int cy = wy - chunkY*CHUNK_DIM;
			int cz = wz - chunkZ*CHUNK_DIM;
			
			return vn.getIntersection(cx, cy, cz, a);
		}
	}
	
	Vector3f getNormal(int wx, int wy, int wz, AXIS a){
		int chunkX = (int) Math.floor((double)wx/CHUNK_DIM);
		int chunkY = (int) Math.floor((double)wy/CHUNK_DIM);
		int chunkZ = (int) Math.floor((double)wz/CHUNK_DIM);
		VoxelNode vn = getChunk(chunkX,chunkY,chunkZ);
		if(vn == null){
			return null;
		}else{
			//ask in chunk local space:
			int cx = wx - chunkX*CHUNK_DIM;
			int cy = wy - chunkY*CHUNK_DIM;
			int cz = wz - chunkZ*CHUNK_DIM;
			
			return vn.getNormal(cx, cy, cz, a);
		}
	}
	
	Vector3f getIsopoint(int wx, int wy, int wz){
		int chunkX = (int) Math.floor((double)wx/CHUNK_DIM);
		int chunkY = (int) Math.floor((double)wy/CHUNK_DIM);
		int chunkZ = (int) Math.floor((double)wz/CHUNK_DIM);
		VoxelNode vn = getChunk(chunkX,chunkY,chunkZ);
		if(vn == null){
			return null;
		}else{
			//ask in chunk local space:
			int cx = wx - chunkX*CHUNK_DIM;
			int cy = wy - chunkY*CHUNK_DIM;
			int cz = wz - chunkZ*CHUNK_DIM;
			return vn.isoPoints.get(vn.index(cx,cy,cz));
		}
	}
	
	/**
	 * Retrieves a list of keys to the chunks associated with an AABB.
	 * @param bb
	 * @return
	 */
	private List<Integer> chunksFromAABB(BoundingBox bb){
		Vector3f min = bb.getMin(new Vector3f());
		Vector3f max = bb.getMax(new Vector3f());
	
		//First clamp to voxel space.
		int minX = ((int) (min.x/voxelSize)) - 1;
		int minY = ((int) (min.y/voxelSize)) - 1;
		int minZ = ((int) (min.z/voxelSize)) - 1;
		int maxX = ((int) (max.x/voxelSize)) + 1;
		int maxY = ((int) (max.y/voxelSize)) + 1;
		int maxZ = ((int) (max.z/voxelSize)) + 1;
		
//		min.divideLocal(voxelSize);
//		max.divideLocal(voxelSize);
		
		//Then clamp to chunk space (rounding up)
		minX = roundChunkF(minX);
		minY = roundChunkF(minY);
		minZ = roundChunkF(minZ);
		
		maxX = roundChunkU(maxX);
		maxY = roundChunkU(maxY);
		maxZ = roundChunkU(maxZ);
		
		//Then iterate over all the chunks...]
		ArrayList<Integer> chunks = new ArrayList<Integer>();
		for(int x= minX; x<= maxX; x++){
			for(int y= minY; y<= maxY; y++){
				for(int z= minZ; z<= maxZ; z++){
					chunks.add(hash(x, y, z));
				}
			}
		}
		return chunks;
		
	}
	
	private int roundChunkF(int c){
		float x = (float)c/CHUNK_DIM;
		return (int) Math.floor(x);
	}
	
	private int roundChunkU(int c){
		float x = (float)c/CHUNK_DIM;
		return (int) Math.ceil(x);
	}
	
	private int hash(int x, int y, int z){
		return  ((x+CHUNK_OFF)& CHUNK_MASK) |
				((y+CHUNK_OFF)<<bitOffSet) |
				((z+CHUNK_OFF) << (bitOffSet*2));
	}
	
	/***
	 * Creates and queue's new chunk
	 * (aka queues anything in world base)
	 * @param key
	 * @return
	 */
	private VoxelNode createChunkFromKey(int key){
		int oX = (key & CHUNK_MASK);
		int oY = ((key>>bitOffSet) & CHUNK_MASK);
		int oZ = ((key>>(bitOffSet*2)) & CHUNK_MASK);
		oX -= CHUNK_OFF;
		oY -= CHUNK_OFF;
		oZ -= CHUNK_OFF;
		
		//Now put it in units of voxels:
		oX *= CHUNK_DIM;
		oY *= CHUNK_DIM;
		oZ *= CHUNK_DIM;
		
		return new VoxelNode(oX, oY, oZ, CHUNK_DIM, CHUNK_DIM, CHUNK_DIM, voxelSize,key);	
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
	public HermiteEdge getEdge(Vector3f p1, Vector3f p2) {
		int x1 = (int) round(p1.x/voxelSize);
		int y1 = (int) round(p1.y/voxelSize);
		int z1 = (int) round(p1.z/voxelSize);
		
		int x2 = (int) round(p2.x/voxelSize);
		int y2 = (int) round(p2.y/voxelSize);
		int z2 = (int) round(p2.z/voxelSize);
		
		HermiteEdge he = new HermiteEdge();
		he.t1 = getType(x1,y1,z1);
		he.t2 = getType(x2,y2,z2);
		
		if(he.t1 != he.t2){
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
			
			//WRONG:
			he.intersection = getIntersection(x1,y1,z1,a);
			he.normal = getNormal(x1,y1,z1,a);
			//TODO: Scale Search...
		}
		return he;
		
		
	}
	

	
	
			
			
}
