package VoxelSystem.SurfaceExtractors;
import idea.HermiteGrid;

import java.util.ArrayList;
import java.util.List;

import VoxelSystem.Hermite.HermiteCube;
import VoxelSystem.Hermite.HermiteExtractor;
import VoxelSystem.MeshBuilding.SurfacePoint;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class DualContour implements SurfaceExtractor{
		
	private void generateTriangles( SurfacePoint[][][] cubePoints, HermiteCube[][][] cubes, List<SurfacePoint> trianglesOut ){
		//TODO: Build Adjacency List for fun and profit.
		//Are quads guaranteed to be planar?
		
		for(int x=0; x<cubePoints[0].length-1; x++){
			for(int z=0; z<cubePoints[0][0].length-1; z++){
				HermiteCube hc = cubes[0][x][z];
				if( hc == null ){ continue; }
				
				int eI = hc.edgeInfo;
				//4 cubes per edge
				SurfacePoint v1 = cubePoints[0][x][z], v2, v3, v4;
				
				//if edge 5
				if((eI & (1<<5)) != 0){
					v2 = cubePoints[0][x+1][z];
					v3 = cubePoints[0][x+1][z+1];
					v4 = cubePoints[0][x][z+1];
					
					if(hc.materials[6] == -1){
						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,true,hc.materials[5]);
					}else if(hc.materials[5] == -1){
						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,false,hc.materials[6]);
					}else{
						// Sub-Surface Quad. Check if this is marked for generation
						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,2);
					}
					
				}
				
				//if edge 6
				if((eI & (1<<6)) != 0){
					v2 = cubePoints[1][x][z];
					v3 = cubePoints[1][x][z+1];
					v4 = cubePoints[0][x][z+1];
					
					if(hc.materials[7] == -1){
						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,true,hc.materials[6]);
//						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,hc.materials[6]);
					}else if(hc.materials[6] == -1){
						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,false,hc.materials[7]);
//						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, false,hc.materials[7]);
					}else{ // Wrapping does not matter for material quads.
						// Sub-Surface Quad. Check if this is marked for generation
						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,2);
					}
				}
				
				//if edge 10
				if((eI & (1<<10)) != 0){
					v2 = cubePoints[1][x][z];
					v3 = cubePoints[1][x+1][z];
					v4 = cubePoints[0][x+1][z];
					
					if(hc.materials[6] == -1){
						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,true,hc.materials[2]);
//						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,hc.materials[2]);
					}else if(hc.materials[2] == -1){
						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4,trianglesOut,false,hc.materials[6]);
//						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, false,hc.materials[6]);
					}else{ 
						// Sub-Surface Quad. Check if this is marked for generation
						QuadExtractor.windingQuadToTriangle(v1,v2,v3,v4, trianglesOut, true,2);
					}
				}
			}
		}

		
	}
	
	
	private void generateQuads( SurfacePoint[][][] cubePoints, HermiteCube[][][] cubes, List<SurfacePoint> quadsOut ){
		//TODO: Build Adjacency List for fun and profit.
		//Are quads guaranteed to be planar?
		
		for(int x=0; x<cubePoints[0].length-1; x++){
			for(int z=0; z<cubePoints[0][0].length-1; z++){
				HermiteCube hc = cubes[0][x][z];
				if( hc == null ){ continue; }
				
				int eI = hc.edgeInfo;
				//4 cubes per edge
				SurfacePoint v1 = cubePoints[0][x][z], v2, v3, v4;
				
				//if edge 5
				if((eI & (1<<5)) != 0){
					v2 = cubePoints[0][x+1][z];
					v3 = cubePoints[0][x+1][z+1];
					v4 = cubePoints[0][x][z+1];
					QuadExtractor.windingQuadToQuad(v1,v2,v3,v4,quadsOut,true);
					
				}
				
				//if edge 6
				if((eI & (1<<6)) != 0){
					v2 = cubePoints[1][x][z];
					v3 = cubePoints[1][x][z+1];
					v4 = cubePoints[0][x][z+1];
					
					QuadExtractor.windingQuadToQuad(v1,v2,v3,v4,quadsOut,true);
				}
				
				//if edge 10
				if((eI & (1<<10)) != 0){
					v2 = cubePoints[1][x][z];
					v3 = cubePoints[1][x+1][z];
					v4 = cubePoints[0][x+1][z];
					QuadExtractor.windingQuadToQuad(v1,v2,v3,v4,quadsOut,true);
				}
			}
		}

		
	}

	@Override
	public List<SurfacePoint> extractSurface(HermiteExtractor hermiteData, BoundingBox bb, float resolution) {
		return extractSurface( hermiteData,  bb,  resolution,  false);
	}
	
	
	public List<SurfacePoint> extractSurface(HermiteExtractor hermiteData, BoundingBox bb, float resolution, boolean quads) {
		Vector3f min = bb.getMin(new Vector3f());
		Vector3f max = bb.getMax(new Vector3f());
		float dx = (max.x-min.x);
		float dy = (max.y-min.y);
		float dz = (max.z-min.z);
		
		int stepsX = (int)Math.ceil(dx/resolution);
		int stepsY = (int)Math.ceil(dy/resolution);
		int stepsZ = (int)Math.ceil(dz/resolution);
//		float actualRes = Math.min(Math.min(dx/stepsX,dy/stepsY), Math.min(dx/stepsX,dz/stepsZ));
		float stepX = dx/stepsX;
		float stepY = dx/stepsY;
		float stepZ = dx/stepsZ;
		
		float diag = (float) (Math.sqrt(stepX*stepX+stepY*stepY+stepZ*stepZ)/1000.0f);
		
		//8 Cube Points.
		Vector3f [] cP = new Vector3f[]{
		new Vector3f(),new Vector3f(),new Vector3f(),new Vector3f(),
		new Vector3f(),new Vector3f(),new Vector3f(),new Vector3f()};
		
		//Should Be a way around storing all this:
		SurfacePoint vertexPoints[][][] = new SurfacePoint[2][stepsX+2][stepsY+2];//[y],x,z
		HermiteCube cube[][][] = new HermiteCube[2][stepsX+2][stepsY+2];
		int yOff = 0;
		
		List<SurfacePoint> triangles = new ArrayList<SurfacePoint>(); 
		BoundingBox cellBox = new BoundingBox();
		cellBox.setXExtent(resolution/2f);
		cellBox.setYExtent(resolution/2f);
		cellBox.setZExtent(resolution/2f);
		
		for(int y=-1;y<=stepsY;y++){
			//Set y values:
			cP[0].y = cP[1].y = cP[4].y = cP[5].y = min.y+(y*stepY);
			cP[2].y = cP[3].y = cP[6].y = cP[7].y =  min.y+((y+1)*stepY);
			for(int x=-1;x<=stepsX;x++){
				//Set x values:
				cP[0].x = cP[3].x = cP[4].x = cP[7].x = min.x+(x*stepX);
				cP[1].x = cP[2].x = cP[5].x = cP[6].x = min.x+((x+1)*stepX);
				for(int z=-1;z<=stepsZ;z++){
					//Set z values:
					cP[0].z = cP[1].z = cP[2].z = cP[3].z = min.z+(z*stepZ);
					cP[4].z = cP[5].z = cP[6].z = cP[7].z = min.z+((z+1)*stepZ);
					
					//Get Cube Value
					HermiteCube hc = hermiteData.getCube(cP);
					
					//Get normals
					if(hc.edgeInfo>0){
						cellBox.setCenter(cP[0].add(cP[6]).mult(.5f));
						
						//Compute Cube Center:
						Vector3f center = ExtractorUtils.surfaceContour(cP, hc.intersections, hc.normals, cellBox, diag);
						
						
						vertexPoints[yOff][x+1][z+1] = new SurfacePoint(center, ExtractorUtils.getNearestType(center, cP, hc.materials));
						cube[yOff][x+1][z+1] = hc;
					}else{
						//vertexPoints[yOff][x+1][z+1] = null;
						cube[yOff][x+1][z+1] = null;
						vertexPoints[yOff][x+1][z+1]= null;
					}
				
				}
			}
			
			//2 rows of Grid Cubes
			yOff++;
			if(yOff==2){
				yOff=1;
				//Generate Quads
				if(quads){
					generateQuads(vertexPoints, cube, triangles);
				}else{
					generateTriangles(vertexPoints,cube,triangles);
				}
				
				SurfacePoint[][] storeV=vertexPoints[0];
				HermiteCube[][] storeC=cube[0];
				
				
				vertexPoints[0] = vertexPoints[1];//last cube is now first
				vertexPoints[1] = storeV;
				
				cube[0] = cube[1];//last cube is now first
				cube[1] = storeC;
				
			}
			
		}
		
		return triangles;
	}

	@Override
	public List<SurfacePoint> extractSurface(HermiteGrid hg, float resolution) {
		HermiteCube cube[][][] = new HermiteCube[2][hg.getHieght()+2][hg.getWidth()+2];
		for(int y =  0; y < hg.getHieght(); y++){
			for(int x = 0; x < hg.getWidth(); x++){
				for(int z = 0; z < hg.getDepth(); z++){
					
				}
			}
			
			
		}
		
		
		
		
		
		
		
		
		
		
		return null;
	}
	
}
