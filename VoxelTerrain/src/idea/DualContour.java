package idea;

import java.util.ArrayList;
import java.util.List;

import VoxelSystem.VoxelSystemTables;
import VoxelSystem.MeshBuilding.SurfacePoint;
import VoxelSystem.SurfaceExtractors.ExtractorUtils;
import VoxelSystem.SurfaceExtractors.QuadExtractor;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class DualContour {
	public List<SurfacePoint> extractSurface(HermiteGrid hg, Vector3f scale) { //TODO: Vector
		//TODO: iterate over smallest axis
		BoundingBox bb = new BoundingBox();
		bb.setXExtent(scale.x);
		bb.setYExtent(scale.y);
		bb.setZExtent(scale.z);
		float threshold = (float) (Math.sqrt(scale.x*scale.x + scale.y*scale.y + scale.z*scale.z) / 1000.0);
		Vector3f [][][] xzCache0 = new Vector3f[2][hg.getWidth()][hg.getDepth()];
		
		Vector3f [] cP = new Vector3f[]{
			new Vector3f(),new Vector3f(),new Vector3f(),new Vector3f(),
			new Vector3f(),new Vector3f(),new Vector3f(),new Vector3f()
		};
		
		int [] materials = new int[8];
		List<Vector3f> edges = new ArrayList<Vector3f>();
		List<Vector3f> normals = new ArrayList<Vector3f>();
		
		List<SurfacePoint> triangles = new ArrayList<SurfacePoint>();
		
		//Compute initial cache
		for(int z = 0; z < hg.getDepth()-2; z++){
			for(int x = 0; x < hg.getWidth()-1; x++){
				setCubeEdges(x,0,z,cP,scale);
				int cI = getCubeInfo(cP,hg,x,z,0,edges,normals, materials);
				xzCache0[0][x][z] = getIsoPoint(cI,cP,edges,normals,bb,threshold);
			}
		}
		
		
		for(int y =  0; y < hg.getHieght()-2; y++){
			
			//Compute xzCache0[1][0][0] xzCache0[1][0][1]
			for(int z = 0; z < hg.getDepth()-2; z++){
				setCubeEdges(0,y+1,z,cP,scale);
				int cI = getCubeInfo(cP,hg,0,y+1,z,edges,normals, materials);
				xzCache0[1][0][z] = getIsoPoint(cI,cP,edges,normals,bb,threshold);
			}
			
			
			
			
			for(int z = 0; z < hg.getDepth()-2; z++){
				
				
				for(int x = 0; x < hg.getWidth()-2; x++){
					
					//Get from cache
					Vector3f cube000 = xzCache0[0][x][z]; //x
					Vector3f cube100 = xzCache0[0][x+1][z]; //x+1 
					Vector3f cube001 = xzCache0[0][x][z+1]; //z+1 
					Vector3f cube101 = xzCache0[0][x+1][z+1]; //z+1 
					
					Vector3f cube010 = xzCache0[1][x][z]; //y+1 (computed last iterations)
					Vector3f cube011 = xzCache0[1][x][z+1]; //z+1,y+1 (computed last iterations)
					
					
//					if(x+1 == y+1,z+1)
					
					//Get x+1, y+1
					setCubeEdges(x+1,y+1,z,cP,scale);
					int cI = getCubeInfo(cP,hg,x+1,y+1,z,edges,normals, materials);
					Vector3f cube110 = getIsoPoint(cI,cP,edges,normals,bb,threshold);
					
					
					setCubeEdges(x+1,y+1,z+1,cP,scale);
					cI = getCubeInfo(cP,hg,x+1,y+1,z+1,edges,normals, materials);
					Vector3f cube111 = getIsoPoint(cI,cP,edges,normals,bb,threshold);
					
					//add triangles
					getMaterials(x,y,z,hg,materials);
					getTriangles(cube000,cube100,cube010,cube001,cube110,cube101,cube011, materials,triangles);
					
					//Save computed cubes
					xzCache0[1][x+1][z+1] = cube111;
					
					
					xzCache0[1][x+1][z] = cube110;
					
				}
				
				
			}
			
			//swap
			Vector3f [][] swap = xzCache0[0];
			xzCache0[0] = xzCache0[1];
			xzCache0[1] = swap;
			
		}
		
		return triangles;
	}
	
	private void setCubeEdges(int x, int y, int z, Vector3f [] cP,Vector3f scale){
		cP[0].y = cP[1].y = cP[4].y = cP[5].y = (float)y*scale.y;
		cP[2].y = cP[3].y = cP[6].y = cP[7].y = (float)(y+1)*scale.y;
		
		cP[0].x = cP[3].x = cP[4].x = cP[7].x = (float)x*scale.x;
		cP[1].x = cP[2].x = cP[5].x = cP[6].x = (float)(x+1)*scale.x;
		
		cP[0].z = cP[1].z = cP[2].z = cP[3].z = (float)z*scale.z;
		cP[4].z = cP[5].z = cP[6].z = cP[7].z = (float)(z+1)*scale.z;
	}
	
	
	private void getMaterials(int x, int y, int z, HermiteGrid hg, int matCache[]){
		matCache[0] = hg.getType(x, y, z);
		matCache[1] = hg.getType(x+1, y, z);
		matCache[2] = hg.getType(x+1, y+1, z);
		matCache[3] = hg.getType(x, y+1, z);
		matCache[4] = hg.getType(x, y, z+1);
		matCache[5] = hg.getType(x+1, y,z+1);
		matCache[6] = hg.getType(x+1, y+1, z+1);
		matCache[7] = hg.getType(x, y+1, z+1);
	}
	
	private int getCubeInfo(Vector3f [] cubeCorners, HermiteGrid hg, int x, int y, int z, List<Vector3f> edges, List<Vector3f> normals,int [] matCache){
		getMaterials(x,y,z,hg,matCache);
		
		int edgeInfo = VoxelSystemTables.getEdgeFromMaterials(matCache);
		edges.clear();
		normals.clear();
		
	
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
			
			int axis = VoxelSystemTables.aTable[i]; //axis of edge
			
			Float f = hg.getIntersection(x + dx, y + dy, z + dz, axis+1);
			normals.add(hg.getNormal(x + dx, y + dy, z + dz, axis+1));
			
			if(f == null){
				System.out.println("Entry "+(x+dx)+","+(y+dy)+","+(z+dz));
			}
			
			edges.add(lerp(cubeCorners[e0],cubeCorners[e1],f));
			
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
	
	private Vector3f getIsoPoint(int edgeInfo, Vector3f [] corners, List<Vector3f> edges, List<Vector3f> normals,  BoundingBox bb,float error){ //int[] materials,
		
		if(edgeInfo != 0){
			Vector3f center = corners[0].add(corners[6]).multLocal(.5f);
			bb.setCenter(center);
			
			Vector3f isoPoint = ExtractorUtils.surfaceContour(corners,
								edges.toArray(new Vector3f[edges.size()]),
								normals.toArray(new Vector3f[normals.size()]), 
								bb, error);
			
		
//			int type =  ExtractorUtils.getNearestType(isoPoint, corners, materials);
			
			return isoPoint;//new SurfacePoint(isoPoint, type);
		}
		
		
		return null;
	}
	
	private void getTriangles(Vector3f c000,Vector3f c100,Vector3f c010,Vector3f c001,Vector3f c110,Vector3f c101,Vector3f c011, int [] materials,List<SurfacePoint> trianglesOut){
		int eI = VoxelSystemTables.getEdgeFromMaterials(materials); 
		Vector3f v1= c000,v2,v3,v4;
		//if edge 5
		if((eI & (1<<5)) != 0){
			v2 = c100;
			v3 = c101;
			v4 = c001;
			if(materials[6] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,true, materials[5]);
			}else if(materials[5] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,false, materials[6]);
			}else{
				// Sub-Surface Quad. Check if this is marked for generation
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,2);
			}
			
		}
		
		//if edge 6
		if((eI & (1<<6)) != 0){
			v2 = c010;
			v3 = c011;
			v4 = c001;
			
			if(materials[7] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,true,materials[6]);
//				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,hc.materials[6]);
			}else if(materials[6] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,false,materials[7]);
//				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, false,hc.materials[7]);
			}else{ // Wrapping does not matter for material quads.
				// Sub-Surface Quad. Check if this is marked for generation
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,2);
			}
		}
		
		//if edge 10
		if((eI & (1<<10)) != 0){
			v2 = c010;
			v3 = c110;
			v4 = c100;
			
			if(materials[6] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,true,materials[2]);
//				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,hc.materials[2]);
			}else if(materials[2] == -1){
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,false,materials[6]);
			}else{ 
				// Sub-Surface Quad. Check if this is marked for generation
				QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,2);
			}
		}
		
		
	}
	
}
