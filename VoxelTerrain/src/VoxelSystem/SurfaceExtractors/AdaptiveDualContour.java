package VoxelSystem.SurfaceExtractors;

import java.util.List;

import VoxelSystem.MeshBuilding.SurfacePoint;
import VoxelSystem.VoxelStorage.VoxelOctree;
import VoxelSystem.VoxelStorage.VoxelTree;

public class AdaptiveDualContour {
	
	List<SurfacePoint> getSurface(VoxelTree vgt){
		//Contour it.
		
		
		return null;
	}
	
	private void faceProc(VoxelOctree q1, VoxelOctree q2, int a){
		
		if(!q1.isLeaf() || !q2.isLeaf()){
			VoxelOctree[] kids1 = q1.getChildren();
			VoxelOctree[] kids2 = q2.getChildren();
			
			if(a == 1){ // X axis
				// X axis
                // 4 calls to faceProc.
                faceProc(kids1[1], kids2[0], 1);
                faceProc(kids1[3], kids2[2], 1);
                faceProc(kids1[5], kids2[4], 1);
                faceProc(kids1[7], kids2[6], 1);
                edgeProc(kids1[2], kids2[0], kids1[6], kids2[4], 1);
                edgeProc(kids1[3], kids2[1], kids1[7], kids2[5], 1);
                edgeProc(kids1[2], kids1[3], kids2[0], kids2[1], 3);
                edgeProc(kids1[6], kids1[7], kids2[4], kids2[5], 3);
			}else if(a == 2){//Y
				// 4 calls to faceProc.
                faceProc(kids1[2], kids2[0], 2);
                faceProc(kids1[3], kids2[1], 2);
                faceProc(kids1[6], kids2[4], 2);
                faceProc(kids1[7], kids2[5], 2);
                
                // 4 calls to edgeProc
                edgeProc(kids1[2], kids2[0], kids1[6], kids2[4], 1);
                edgeProc(kids1[3], kids2[1], kids1[7], kids2[5], 1);
                edgeProc(kids1[2], kids1[3], kids2[0], kids2[1], 3);
                edgeProc(kids1[6], kids1[7], kids2[4], kids2[5], 3);
                
			}else{//Z axis
				 faceProc(kids1[4], kids2[0], 3);
                 faceProc(kids1[5], kids2[1], 3);
                 faceProc(kids1[6], kids2[2], 3);
                 faceProc(kids1[7], kids2[3], 3);
                 
                 // 4 calls to edgeProc
                 edgeProc( kids1[4], kids1[5], kids2[0], kids2[1], 2);
                 edgeProc(kids1[6], kids1[7], kids2[2], kids2[3], 2);
                 edgeProc(kids1[4], kids1[6], kids2[0], kids2[2], 1);
                 edgeProc(kids1[5], kids1[7], kids2[1], kids2[3], 1); 
			}
			
			
		}
		
		
	}
	
	
	private void edgeProc(VoxelOctree q0,VoxelOctree q1,VoxelOctree q2,VoxelOctree q3, int a){
		  if (q0 != null && q1 != null && q2 != null && q3 != null){
	            // If all cubes are leaves, stop recursion.
	            if (q0.isLeaf() && q1.isLeaf() && q2.isLeaf() && q3.isLeaf()){
	            	int corners1[] = null, corners2[] = null;
	            	if(a == 1){
	            		corners1 = new int[]{ 6, 4, 2, 0};
	            		corners2 = new int[] { 7, 5, 3, 1 };
	            	}else if(a == 2){
						corners1 = new int[] { 5, 4, 1, 0 };
						corners2 = new int[] { 7, 6, 3, 2 };
	            	}else{
						corners1 = new int[] { 3, 2, 1, 0 };
						corners2 = new int[] { 7, 6, 5, 4 };
	            	}
	            	
					int v1, v2;
					boolean intersectionFound = false;
					for (int i = 0; i < 4 && !intersectionFound; i++) {
						v1 = 1;// Hull.getValueAt(q[i].getCorner(corners1[i]),
								// primitives);
						v2 = 1;// Hull.getValueAt(q[i].getCorner(corners2[i]),
								// primitives);
						// Check if the signs are different.
						if (v1 != v2) {
							intersectionFound = true;
	
							// If so, create a quad with the right triangle
							// orientation.
							if (a == 2) {
								v1 = -v1;
							}
	
							if (v1 < 0) {
								// triangles.add(new Vector3i(q[0].getVertexIndex(),q[1].getVertexIndex(), q[2].getVertexIndex()));
								// triangles.add(new Vector3i(q[2].getVertexIndex(),q[1].getVertexIndex(), q[3].getVertexIndex()));
							} else {
								// triangles.add(new Vector3i(q[2].getVertexIndex(),q[1].getVertexIndex(), q[0].getVertexIndex()));
								// triangles.add(new Vector3i(q[3].getVertexIndex(),q[1].getVertexIndex(), q[2].getVertexIndex()));
							}
						}
					}
	            	
	            	
	            }
	     }else{ //Not all leaves. 
	    	 VoxelOctree[] kids1 = q0.getChildren();
	    	 VoxelOctree[] kids2 = q1.getChildren();
	    	 VoxelOctree[] kids3 = q2.getChildren();
	    	 VoxelOctree[] kids4 = q3.getChildren();

             if(a == 1){
                     edgeProc(kids1[6], kids2[4], kids3[2], kids4[0], 1);
                     edgeProc(kids1[7], kids2[5], kids3[3], kids4[1], 1);
             }else if(a == 2){
                     edgeProc(kids1[5], kids2[4], kids3[1], kids4[0], 2);
                     edgeProc(kids1[7], kids2[6], kids3[3], kids4[2], 2);
             }else{
                     edgeProc(kids1[3], kids2[2], kids3[1], kids4[0], 3);
                     edgeProc(kids1[7], kids2[6], kids3[5], kids4[4], 3);
             }
	    	 
	    	 
	     }
	}
	
}
