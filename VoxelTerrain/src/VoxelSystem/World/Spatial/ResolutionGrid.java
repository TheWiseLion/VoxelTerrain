package VoxelSystem.World.Spatial;

import java.util.ArrayList;
import java.util.List;

import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Vector3f;

public class ResolutionGrid {
	private static class OctreeNode{
		private OctreeNode[] children;
		private final BoundingBox bb;
		private int depth;
		private float diag;
		private OctreeNode(Vector3f minBound, Vector3f maxBound, int depth) {
			bb = new BoundingBox(minBound,maxBound);
			this.depth = depth;
		}

		public OctreeNode(Vector3f minBound, Vector3f maxBound) {
			this(minBound, maxBound, 0);
		}

		/**
		 * Creates the eight children of this node.
		 */
		public void subdivide(){
		  // First compute the center of the current cell.
		 Vector3f minBound = bb.getMin(new Vector3f());
		 Vector3f maxBound = bb.getMax(new Vector3f());
		 Vector3f center = minBound.add(maxBound).divideLocal(2);

		  //The child 0bZYX is located at the X, Y ,Z quadrant.
		  children = new OctreeNode[8];
		  children[0] = new OctreeNode(minBound, center, depth + 1);
		  children[1] = new OctreeNode(new Vector3f(center.x, minBound.y, minBound.z), new Vector3f(maxBound.x, center.y, center.z), depth + 1);
		  children[2] = new OctreeNode(new Vector3f(minBound.x, center.y, minBound.z), new Vector3f(center.x, maxBound.y, center.z), depth + 1);
		  children[3] = new OctreeNode(new Vector3f(center.x, center.y, minBound.z), new Vector3f(maxBound.x, maxBound.y, center.z), depth + 1);
		  children[4] = new OctreeNode(new Vector3f(minBound.x, minBound.y, center.z), new Vector3f(center.x, center.y, maxBound.z), depth + 1);
		  children[5] = new OctreeNode(new Vector3f(center.x, minBound.y, center.z), new Vector3f(maxBound.x, center.y, maxBound.z), depth + 1);
		  children[6] = new OctreeNode(new Vector3f(minBound.x, center.y, center.z), new Vector3f(center.x, maxBound.y, maxBound.z), depth + 1);
		  children[7] = new OctreeNode(center, maxBound, depth + 1);

		  // Free the associated vertex, if there is one.
		}

		/**
		 * Returns true iff no child exists.
		 */
		public boolean isLeaf() {
			return children != null;
		}

		/**
		 * Returns true iff the the vertex is inside the cubes bounds.
		 */
		public boolean contains(Vector3f vertex) {
			return bb.contains(vertex);
		}

//		/** Returns the length of the cube's diagonal. */
//		public float getCubeDiagonal() {
//			return maxBound.distance(minBound);
//		}
		public void place(BoundingVolume bv,int depth,List<OctreeNode> leaves){
			if(bb.intersects(bv)){
				if(this.depth!=depth){ // must go deeper.
					//Subdivide
					if(isLeaf()){
						subdivide();
					}
					for(OctreeNode on:children){
						if(on.bb.intersects(bv)){
							on.place(bb, depth, leaves);
						}
					}
				}else{
					leaves.add(this);
				}
			}else{
				throw new RuntimeException("Bounding volumes don't intersect");
			}
		}
	}
	
	private int maxDepth;
	private OctreeNode tree;
	private List<OctreeNode> cache;
	private float viewDistance;
	private float localCube;
	BoundingBox localArea; //changes OFTEN
	BoundingBox WideArea; //Changes not often.
	
	public ResolutionGrid(int maxDepth, Vector3f postion,float localArea,float viewDistance){
		this.viewDistance = viewDistance;
		this.localCube = localArea;
		this.maxDepth = maxDepth;
		cache = new ArrayList<OctreeNode>();
		this.localArea = createBox(postion, localArea);
		this.WideArea = createBox(postion, viewDistance);
		tree = new OctreeNode(WideArea.getMin(new Vector3f()),WideArea.getMax(new Vector3f()));
	}
	
	private BoundingBox createBox(Vector3f point, float expand){
		Vector3f min = new Vector3f(point);
		Vector3f max = new Vector3f(point);
		min.subtractLocal(-expand, -expand, -expand);
		max.addLocal(expand, expand, expand);
		return new BoundingBox(min,max);
	}
	
	public void update(Vector3f position){
		//error in wide area
		
		//error in local area
		
	}

	
}
