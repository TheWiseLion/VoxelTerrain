package VoxelSystem;

import idea.Chunk;
import idea.VoxelGrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import VoxelSystem.Hermite.VoxelExtractor;
import VoxelSystem.MeshBuilding.BasicMesher;
import VoxelSystem.MeshBuilding.SurfacePoint;
import VoxelSystem.Operators.CSGHelpers;
import VoxelSystem.Operators.CSGOperators;
import VoxelSystem.SurfaceExtractors.DualContour;

import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;

public class PagingVoxelObject {
	
	public class VoxelNode extends Chunk{
		private List<Geometry> generated;
		private boolean isDirty = true;
		private PagingVoxelObject owner;
		public VoxelNode(Vector3f corner, int width, int height, int depth, float size, PagingVoxelObject pvo) {
			super(corner, width, height, depth, size);
			this.owner=pvo;
		}
		
		private void gen(Node parent, Map<Integer, Material> iToM){
			if(generated != null){
				for(Geometry g : generated){
					parent.detachChild(g);
				}
			}
			
			DualContour dc = new DualContour();
			List<SurfacePoint> surface = dc.extractSurface(this.corner, this, vSize);
			BasicMesher bm = new BasicMesher();
			System.out.println("Compiling: "+getCorner()+" with "+surface.size());
			bm.addTriangles(surface);
			Map<Integer, Mesh> meshes= bm.compileMeshes();
			generated = new ArrayList<Geometry>(meshes.size());
			
			for(Integer i : meshes.keySet()){
				Mesh m = meshes.get(i);
				m.updateBound();
				m.updateCounts();
				if(m.getTriangleCount() > 0){
					Geometry g = new Geometry();
					g.setMesh(m);
					g.setMaterial(iToM.get(i));
					generated.add(g);
					parent.attachChild(g);
					g.setLocalTranslation(this.getCorner());
				}
			}
			
			//Mark as processed:
			isDirty = false;
		}
		
		@Override
		public void extract(VoxelExtractor ve){
			super.extract(ve);
			this.isDirty = true;
			owner.dirtyNodes.add(this);
		}
		
		//TODO: override to get from adjacent chunks (if chunk.contains point)
	}
	int bitOffSet = 10;
	int CHUNK_MASK = 0x3FF;//10 bits -> ~1000 chunks
	int CHUNK_OFF = CHUNK_MASK/2;
	int CHUNK_DIM = 16;
	private float voxelSize;
	Map<Integer, VoxelNode> chunks;
	List<VoxelNode> dirtyNodes;
//	List<VoxelExtractor> worldBase;
	//private Map<Spatial, CSG-TREE> #holds functions that have not been fully generated
	//private Map<Spatial, Chunk> #holds generated chunk data
	
	
	Map<Integer, Material> materials;
	//TODO: keep a list and recompute if overlap
	
	public PagingVoxelObject(float voxelSize, Map<Integer, Material> iToM){
		this.voxelSize = voxelSize;
		this.materials = iToM;
		dirtyNodes = new ArrayList<VoxelNode>();
		chunks = new HashMap<Integer, VoxelNode>();
	}
	
	public void update(Node root){
		//Re-gen any dirty nodes
		for(VoxelNode vn : dirtyNodes){
			if(vn.isDirty){
				vn.gen(root, this.materials);
				vn.isDirty = false;
			}
		}	
		dirtyNodes.clear();
	}
	
//	public void add(Vector3f p){
//		BoundingBox b = new BoundingBox();
//		b.setCenter(p);
//		b.setXExtent(2f);b.setXExtent(2f);
//		b.setXExtent(2f);
//		VoxelDensityExtractor bv = new VoxelDensityExtractor(new BoxVolume(p, 1, 1, 1));
//		for(VoxelNode vn : nodes){
//			if(vn.getBoundingBox().intersects(b)){
//				VoxelExtractor ve = CSGOperators.union(false, vn,bv).extract(vn.getCorner(),vn.getWidth(),vn.getHieght(),vn.getDepth(), voxelSize);
//				vn.extract(ve);
//			}
//		}
//	}
	
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
		
		min.divideLocal(voxelSize);
		max.divideLocal(voxelSize);
		
		//Then clamp to chunk space (rounding up)
		minX = roundChunkF(minX);
		minY = roundChunkF(minY);
		minZ = roundChunkF(minZ);
		
		maxX = roundChunkF(maxX);
		maxY = roundChunkF(maxY);
		maxZ = roundChunkF(maxZ);
		
		//Then iterate over all the chunks...]
		ArrayList<Integer> chunks = new ArrayList<Integer>();
		int count = 0;
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
		if(x < 0){
			return (int) -Math.ceil(-x);
		}else{
			return (int) Math.floor(x);
		}
		
	}
	private int roundChunkU(int c){
		float x = (float)c/CHUNK_DIM;
		if(x < 0){
			return (int) -Math.floor(-x);
		}else{
			return (int) Math.floor(x);
		}
		
	}
	private int hash(int x, int y, int z){
		return  ((x+CHUNK_OFF)& CHUNK_MASK) |
				((y+CHUNK_OFF)<<bitOffSet) |
				((z+CHUNK_OFF) << (bitOffSet*2));
	}
	
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
		//Now in real coorindates:
		Vector3f corner = new Vector3f(oX*voxelSize,oY*voxelSize,oZ*voxelSize);
		return new VoxelNode(corner, CHUNK_DIM+4, CHUNK_DIM+4, CHUNK_DIM+4, voxelSize, this);	
	}
	
	public void set(VoxelExtractor ve){
		List<Integer> chunkKeys = this.chunksFromAABB(ve.getBoundingBox());
		for(int chunk : chunkKeys){
			VoxelNode c = this.chunks.get(chunk);
			if(c == null){
				c = createChunkFromKey(chunk);
			}
			c.extract(ve);
			this.chunks.put(chunk,c);
		}
	}
	
	public void add(VoxelExtractor ve){
		List<Integer> chunkKeys = this.chunksFromAABB(ve.getBoundingBox());
		for(int chunk : chunkKeys){
			VoxelNode c = this.chunks.get(chunk);
			if(c == null){
				c = createChunkFromKey(chunk);
			}
			BoundingBox b = new BoundingBox();
			CSGHelpers.getIntersection(ve.getBoundingBox(),c.getBoundingBox(),b);
			
//			System.out.println("Added to chunk: "+c.getCorner()+":"+chunk);
//			System.out.println("Intersecting AABB: "+b);
			VoxelGrid vg = CSGOperators.union(true, c,ve).extract(c.getCorner(), CHUNK_DIM+4,CHUNK_DIM+4,CHUNK_DIM+4,voxelSize);
			c.extract(vg);
//			c.extract(ve);
			
			this.chunks.put(chunk,c);
		}
	}
	
	
	public void remove(VoxelExtractor ve){
		List<Integer> chunkKeys = this.chunksFromAABB(ve.getBoundingBox());
		for(int chunk : chunkKeys){
			VoxelNode c = this.chunks.get(chunk);
			if(c == null){
				c = createChunkFromKey(chunk);
			}
			BoundingBox b = new BoundingBox();
			CSGHelpers.getIntersection(ve.getBoundingBox(),c.getBoundingBox(),b);
			
			System.out.println("Added to chunk: "+c.getCorner()+":"+chunk);
			System.out.println("Intersecting AABB: "+b);
			VoxelGrid vg = CSGOperators.difference(c,ve).extract(c.getCorner(), CHUNK_DIM+4,CHUNK_DIM+4,CHUNK_DIM+4,voxelSize);
			c.extract(vg);
//			c.extract(ve);
			
			this.chunks.put(chunk,c);
		}
	}
	

	
}
