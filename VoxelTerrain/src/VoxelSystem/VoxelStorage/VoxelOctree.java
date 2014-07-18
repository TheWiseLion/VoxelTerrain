package VoxelSystem.VoxelStorage;


public class VoxelOctree {
	int x,  y, z;//Top left corner
	int size; //must be power of 2 (or 0)
	int lod;
	
//	VoxelOctreeNode parent;
	VoxelOctree[] childern; // null or 8
	
	int [] types;//Only if leaf
//	Float intersections; //Only if leaf
//	Vector3f [] normals; //Only if leaf
	
	public VoxelOctree(int x, int y, int z, int size){
		//if size == 0
		
	}
	
	public void split(){
		if(size == 0){
			throw new RuntimeException();
		}
		
		childern = new VoxelOctree[8];
		for (int x = 0; x < 2; x++) {
			int xChild = size * x + this.x;
			
			for (int y = 0; y < 2; y++) {
				int yChild = size * y + this.y;

				for (int z = 0; z < 2; z++) {
					int zChild = size * z + this.z;
					childern[x + y * 2 + z * 4] = new VoxelOctree(xChild, yChild, zChild, size / 2);
				}
				
			}
			
		}
	}
	
	public boolean isLeaf(){
		return childern == null;
	}
	
	public VoxelOctree[] getChildren(){
		return childern;
	}
	
	
	
	
	
	
	
	
	
///////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////// HELPERS ////////////////////////////
////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////

	private int getChild(int x, int y, int z){
		//This matches the split method if you look closely.
		int c = 0; //0 - 7 (3 bits)
		if(x > this.x + size){
			c += 1;
		}
		
		if(y > this.y + size){
			c += 2; 
		}
		
		if(z > this.z + size){
			c += 4; 
		}
		return c;
	}
	
	
}
