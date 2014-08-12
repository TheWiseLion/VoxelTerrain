package voxelsystem;

import voxelsystem.voxeldata.HermiteCube;

import com.jme3.math.Vector3f;

public final class VoxelSystemTables {
	private static final float Isolevel = 0;
	//Intersection Table: (axis aligned)
	public static final int iTable[] = { //ith edge -> i*2,i*2+1 indexs
		0, 1, //0  
		1, 2, //1
		3, 2, //2
		0, 3, //3
		4, 5, //4
		5, 6, //5
		7, 6, //6
		4, 7, //7
		0, 4, //8
		1, 5, //9
		2, 6, //10
		3, 7 //11
	};
	
	public static final int cTable[] = { //dx,dy,dz from zero for each edge
		0, 0, 0, //0  
		1, 0, 0, //1
		1, 1, 0, //2
		0, 1, 0, //3
		0, 0, 1, //4
		1, 0, 1, //5
		1, 1, 1, //6
		0, 1, 1, //7
	};
	
	public static final AXIS aTable[] = { //axis table
		AXIS.X, //x (edge AXIS.PX)  
		AXIS.Y, //y (edge AXIS.PY)
		AXIS.X, //x (edge AXIS.PZ)
		AXIS.Y, //y (edge 3)
		AXIS.X, //x (edge 4)
		AXIS.Y, //y (edge 5)
		AXIS.X, //x (edge 6)
		AXIS.Y, //y (edge 7)
		AXIS.Z, //z (edge 8)
		AXIS.Z, //z (edge 9)
		AXIS.Z, //z (edge AXIS.PYAXIS.PX)
		AXIS.Z, //z (edge AXIS.PYAXIS.PY)
		
	};
	
	public static final int edgeTable[] = { 0x0, 0x109, 0x203, 0x30a, 0x406,
			0x50f, 0x605, 0x70c, 0x80c, 0x905, 0xa0f, 0xb06, 0xc0a, 0xd03,
			0xe09, 0xf00, 0x190, 0x99, 0x393, 0x29a, 0x596, 0x49f, 0x795,
			0x69c, 0x99c, 0x895, 0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90,
			0x230, 0x339, 0x33, 0x13a, 0x636, 0x73f, 0x435, 0x53c, 0xa3c,
			0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30, 0x3a0, 0x2a9,
			0x1a3, 0xaa, 0x7a6, 0x6af, 0x5a5, 0x4ac, 0xbac, 0xaa5, 0x9af,
			0x8a6, 0xfaa, 0xea3, 0xda9, 0xca0, 0x460, 0x569, 0x663, 0x76a,
			0x66, 0x16f, 0x265, 0x36c, 0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a,
			0x963, 0xa69, 0xb60, 0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0xff,
			0x3f5, 0x2fc, 0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9,
			0xaf0, 0x650, 0x759, 0x453, 0x55a, 0x256, 0x35f, 0x55, 0x15c,
			0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950, 0x7c0,
			0x6c9, 0x5c3, 0x4ca, 0x3c6, 0x2cf, 0x1c5, 0xcc, 0xfcc, 0xec5,
			0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0, 0x8c0, 0x9c9, 0xac3,
			0xbca, 0xcc6, 0xdcf, 0xec5, 0xfcc, 0xcc, 0x1c5, 0x2cf, 0x3c6,
			0x4ca, 0x5c3, 0x6c9, 0x7c0, 0x950, 0x859, 0xb53, 0xa5a, 0xd56,
			0xc5f, 0xf55, 0xe5c, 0x15c, 0x55, 0x35f, 0x256, 0x55a, 0x453,
			0x759, 0x650, 0xaf0, 0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5,
			0xdfc, 0x2fc, 0x3f5, 0xff, 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0,
			0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c, 0x36c,
			0x265, 0x16f, 0x66, 0x76a, 0x663, 0x569, 0x460, 0xca0, 0xda9,
			0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac, 0x4ac, 0x5a5, 0x6af,
			0x7a6, 0xaa, 0x1a3, 0x2a9, 0x3a0, 0xd30, 0xc39, 0xf33, 0xe3a,
			0x936, 0x83f, 0xb35, 0xa3c, 0x53c, 0x435, 0x73f, 0x636, 0x13a,
			0x33, 0x339, 0x230, 0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f,
			0x895, 0x99c, 0x69c, 0x795, 0x49f, 0x596, 0x29a, 0x393, 0x99,
			0x190, 0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c,
			0x70c, 0x605, 0x50f, 0x406, 0x30a, 0x203, 0x109, 0x0 
	};
	
	
	/***
	 * Defines:
	 * positive x, negative x,
	 * positive y, negative y,
	 * positive z, negative z
	 * @author 0xFFFF
	 *
	 */
	public enum AXIS {
		X,
		Y,
		Z,
	}

	
	/**
	 * Get the cube index by inspecting the density
	 * values of the cube corners
	 */
	public static int getCubeIndex(float[] val) {
		int cubeindex = 0;
		// Find the cube index:
		if (val[0] < Isolevel)
			cubeindex |= 1;
		if (val[1] < Isolevel)
			cubeindex |= 2;
		if (val[2] < Isolevel)
			cubeindex |= 4;
		if (val[3] < Isolevel)
			cubeindex |= 8;
		if (val[4] < Isolevel)
			cubeindex |= 16;
		if (val[5] < Isolevel)
			cubeindex |= 32;
		if (val[6] < Isolevel)
			cubeindex |= 64;
		if (val[7] < Isolevel)
			cubeindex |= 128;
		return cubeindex;
	}
	
	/***
	 * Get the edge info by inspecting the
	 * materials at each of the cube corners.
	 * Edges are active if there is a material change
	 * on that edge.
	 */
	public static int getEdgeFromMaterials(int [] val) {
		int edgeInfo = 0;
		//For each edge:
		if (val[0] != val[1])//0, 1
			edgeInfo |= 1;
		
		if (val[1] != val[2])//1, 2
			edgeInfo |= 2;
		
		if (val[2] != val[3])//2, 3
			edgeInfo |= 4;
		
		if (val[3] != val[0])//3, 0
			edgeInfo |= 8;
		
		if (val[4] != val[5])//4, 5
			edgeInfo |= 16;
		
		if (val[5] != val[6])//5, 6
			edgeInfo |= 32;
		
		if (val[6] != val[7])//6, 7
			edgeInfo |= 64;
		
		if (val[7] != val[4])//7, 4
			edgeInfo |= 128;
		
		if (val[0] != val[4])//0, 4
			edgeInfo |= 256;
		
		if (val[1] != val[5])//1, 5
			edgeInfo |= 512;
		
		if (val[2] != val[6])//2, 6
			edgeInfo |= 1024;
		
		if (val[3] != val[7])//3, 7
			edgeInfo |= 2048;
		
		return edgeInfo;
	}
	
	public static Vector3f getIntersection(Vector3f p1, Vector3f p2, float valp1,float valp2) {
		if (Math.abs(valp1) == 0)
			return (p1);
		if (Math.abs(valp2) == 0)
			return (p2);
		if (Math.abs(valp1 - valp2) <= 0)
			return (p1);
		
		float v1 = Math.abs(valp1);
	    float v2 = Math.abs(valp2);
		float v = v1 + v2;
		// mu is always between 1 and 0
		Vector3f p = new Vector3f();
		p.x = (v2 * p1.x + v1 * p2.x) / v;
		p.y = (v2 * p1.y + v1 * p2.y) / v;
		p.z = (v2 * p1.z + v1 * p2.z) / v;
		
		return (p);
	}
	
	public static int[] getCubeEdges(int edgeInfo){
		int numIntersections = Integer.bitCount(edgeInfo);
		int [] cubePoints = new int[numIntersections*2];
		int off = 0;
		for (int i = 0; i < 12; i++){ // 12 edges
			if ((edgeInfo & (1 << i)) == 0){
				continue;
			}
			cubePoints[off]=iTable[i*2];
			cubePoints[off+1]=iTable[i*2 +1];
			
			off +=2;
		}
		return cubePoints;
	}
	
	
	/**
	 * 0 -> x axis
	 * 1 -> y axis
	 * 2 -> z axis
	 * @param edgeInfo
	 * @return
	 */
	public static AXIS[] edgeInfoToAxis(int edgeInfo){
		int numIntersections = Integer.bitCount(edgeInfo);
		AXIS [] axis = new AXIS[numIntersections];
		int off = 0;
		for (int i = 0; i < 12; i++){ // 12 edges
			if ((edgeInfo & (1 << i)) == 0){
				continue;
			}
			if(i==0 || i==4 || i== 2 || i==6){
				axis[off] = AXIS.X; //x 
			}else if(i== 1 || i ==3 || i==7 || i==5){
				axis[off] = AXIS.Y; //y 
			}else{
				axis[off] = AXIS.Z; //z
			}
			
			off++;
		}
		return axis;
	}
	
	
	
	/**
	 * 0 -> x axis
	 * 1 -> y axis
	 * 2 -> z axis
	 * @param edgeInfo
	 * @return
	 */
	public static AXIS edgeToAxis(int i) {
		if (i == 0 || i == 4 || i == 2 || i == 6) {
			return AXIS.X; // x
		} else if (i == 1 || i == 3 || i == 7 || i == 5) {
			return AXIS.Y; // y
		} else {
			return AXIS.Z; // z
		}
	}
	
	/**
	 * edge index to normal of axis
	 * @return normal of axis x-> <1,0,0>, y-> <0,1,0>
	 */
	public static Vector3f edgeToNormal(int i) {
		if (i == 0 || i == 4 || i == 2 || i == 6) {
			return new Vector3f(1,0,0); // x
		} else if (i == 1 || i == 3 || i == 7 || i == 5) {
			return new Vector3f(0,1,0); // y
		} else {
			return new Vector3f(0,0,1); // z
		}
	}
	
	/**
	 * Returns indexes for surface contour (as opposed to sub-surface contours)
	 */
	public static int[] surfaceEdges(HermiteCube hc){
		int [] s = getCubeEdges(hc.edgeInfo);
		int [] r = null;
		if(s.length > 0){
			int count = 0;
			for(int i =0; i < s.length; i+=2){
				if(hc.materials[s[i]] == -1 || hc.materials[s[i+1]] == -1){
					count++;
				}
			}	
			
			r = new int[count];
			count = 0;
			for(int i =0; i < s.length; i+=2){
				if(hc.materials[s[i]] == -1 || hc.materials[s[i+1]] == -1){
					r[count] = i/2;
					count++;
				}
			}			
		}
		
		return r;
	}
	
	/**
	 * Returns indexes for surface contour (as opposed to sub-surface contours)
	 */
	public static int[] subSurfaceEdges(HermiteCube hc){
		int [] s = getCubeEdges(hc.edgeInfo);
		int [] r = null;
		if(s.length > 0){
			int count = 0;
			for(int i =0; i < s.length; i+=2){
				if(hc.materials[s[i]] != -1 && hc.materials[s[i+1]] != -1){
					count++;
				}
			}	
			
			r = new int[count];
			count = 0;
			for(int i =0; i < s.length; i+=2){
				if(hc.materials[s[i]] != -1 && hc.materials[s[i+1]] != -1){
					r[count] = i/2;
					count++;
				}
			}			
		}
		
		return r;
	}
	
	public static void main(String[] args){
		
		for(int i=0; i<edgeTable.length;i++){
			System.out.print(edgeTable[i]+",");
		}
	}
	
	//TODO: Triangle Table
}
