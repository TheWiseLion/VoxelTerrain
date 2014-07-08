package VoxelSystem.SurfaceExtractors;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import VoxelSystem.Hermite.HermiteCube;
import VoxelSystem.Hermite.HermiteExtractor;
import VoxelSystem.MeshBuilding.SurfacePoint;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;

public class DualContour implements SurfaceExtractor{
	private static final int MAX_ITERATIONS = 1000;
//	private static int sumIterations = 0;
//	private static int numberOfIterations = 0;
//	private static float FORCE_TRESHOLD = 0.00001f;
	private static float forceRatio = 0.75f;


	/**
	 * Computes the vertex for the cube, from Hermite data. Uses Leonardo
	 * Augusto Schmitz's excellent method, with exact normal at intersection
	 * points, to reduce complexity.
	 * 
	 */
	public static Vector3f[] surfaceContour(HermiteCube hc, Vector3f[] cP, float threshold) {
		threshold *= threshold;
		
//		int [] surfaceIndexes = new int[hc.intersections.length];
//		for (int i=0; i<surfaceIndexes.length; ++i) { surfaceIndexes[i] = i; }
		
		// Center the particle on the masspoint.
//		Vector3f masspoint = new Vector3f();
//		for (int i : surfaceIndexes) {
//			masspoint.addLocal(hc.intersections[i]);
//		}
//		masspoint.divideLocal(surfaceIndexes.length);
//		Vector3f particlePosition = new Vector3f(masspoint);

		Vector3f masspoint = new Vector3f();
		for (Vector3f v : hc.intersections) {
			masspoint.addLocal(v);
		}
		masspoint.divideLocal(hc.intersections.length);
		Vector3f particlePosition = new Vector3f(masspoint);
		
//		int [] subSurfaceIndexes = VoxelSystemTables.subSurfaceEdges(hc);
//		Vector3f typepoint = new Vector3f();
//		for (int i : subSurfaceIndexes) {
//			typepoint.addLocal(hc.intersections[i]);
//		}
//		typepoint.divideLocal(subSurfaceIndexes.length);
		
		Vector3f typepoint = new Vector3f();
		for (Vector3f v : hc.intersections) {
			typepoint.addLocal(v);
		}
		typepoint.divideLocal(hc.intersections.length);
		//particlePosition.set(typepoint);
		
//		// Start iterating:
		Vector3f force = new Vector3f();
		int iteration;
	
		for (iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
			force.set(0, 0, 0);

			// For each intersection point:
			for (int i = 0; i < hc.intersections.length; i++) {
				Vector3f planePoint = hc.intersections[i];
				Vector3f planeNormal = hc.normals[i];// normal;//
				
				// Compute distance vector to plane.
				// To do that, compute the normal.dot(AX).
				float d = planeNormal.dot(particlePosition.subtract(planePoint));

				force.addLocal(planeNormal.mult(-d));
			}
			// Average the force over all the intersection points, and multiply
			// with a ratio and some damping to avoid instabilities.
			float damping = 1f - ((float) iteration) / MAX_ITERATIONS;

			force.multLocal(forceRatio * damping / hc.intersections.length);

			// Apply the force.
			particlePosition.addLocal(force);

			// If the force was almost null, break.
			if (force.lengthSquared() < threshold) {
				break;
			}
		}
		
		return new Vector3f[]{ particlePosition, typepoint };
	}
	
	private void generateTriangles( Vector3f[][][][] cubePoints, HermiteCube[][][] cubes, HermiteExtractor he, List<SurfacePoint> trianglesOut ){
		//TODO: Build Adjacency List for fun and profit.
		//Are quads guaranteed to be planar?
		LinkedList<Triangle> isogons = new LinkedList<Triangle>();
		LinkedList<Triangle> matgons = new LinkedList<Triangle>();
		
		for(int x=0; x<cubePoints[0].length-1; x++){
			for(int z=0; z<cubePoints[0][0].length-1; z++){
				HermiteCube hc = cubes[0][x][z];
				if( hc == null ){ continue; }
				
				int eI = hc.edgeInfo;
				//4 cubes per edge
				Vector3f[] v1 = cubePoints[0][x][z], v2, v3, v4;
				
				//if edge 5
				if((eI & (1<<5)) != 0){
					v2 = cubePoints[0][x+1][z];
					v3 = cubePoints[0][x+1][z+1];
					v4 = cubePoints[0][x][z+1];
					
					if(hc.materials[6] == -1){
						isogons.push(new Triangle(v1[0], v2[0], v3[0]));
						isogons.push(new Triangle(v1[0], v3[0], v4[0]));
					}else if(hc.materials[5] == -1){
						isogons.push(new Triangle(v1[0], v3[0], v2[0]));
						isogons.push(new Triangle(v1[0], v4[0], v3[0]));
					}else{ // Wrapping does not matter for material quads.
//						matgons.push(new Triangle(v1[1], v2[1], v3[1]));
//						matgons.push(new Triangle(v1[1], v3[1], v4[1]));
						matgons.push(new Triangle(v1[0], v2[0], v3[0]));
						matgons.push(new Triangle(v1[0], v3[0], v4[0]));
						
					}
				}
				
				//if edge 6
				if((eI & (1<<6)) != 0){
					v2 = cubePoints[1][x][z];
					v3 = cubePoints[1][x][z+1];
					v4 = cubePoints[0][x][z+1];
					
					if(hc.materials[7] == -1){
						isogons.push(new Triangle(v1[0], v2[0], v3[0]));
						isogons.push(new Triangle(v1[0], v3[0], v4[0]));
					}else if(hc.materials[6] == -1){
						isogons.push(new Triangle(v1[0], v3[0], v2[0]));
						isogons.push(new Triangle(v1[0], v4[0], v3[0]));
					}else{ // Wrapping does not matter for material quads.
//						matgons.push(new Triangle(v1[1], v2[1], v3[1]));
//						matgons.push(new Triangle(v1[1], v3[1], v4[1]));
						matgons.push(new Triangle(v1[0], v2[0], v3[0]));
						matgons.push(new Triangle(v1[0], v3[0], v4[0]));
					}
				}
				
				//if edge 10
				if((eI & (1<<10)) != 0){
					v2 = cubePoints[1][x][z];
					v3 = cubePoints[1][x+1][z];
					v4 = cubePoints[0][x+1][z];
					
					if(hc.materials[6] == -1){
						isogons.push(new Triangle(v1[0], v2[0], v3[0]));
						isogons.push(new Triangle(v1[0], v3[0], v4[0]));
					}else if(hc.materials[2] == -1){
						isogons.push(new Triangle(v1[0], v3[0], v2[0]));
						isogons.push(new Triangle(v1[0], v4[0], v3[0]));
					}else{ // Wrapping does not matter for material quads.
//						matgons.push(new Triangle(v1[1], v2[1], v3[1]));
//						matgons.push(new Triangle(v1[1], v3[1], v4[1]));
						matgons.push(new Triangle(v1[0], v2[0], v3[0]));
						matgons.push(new Triangle(v1[0], v3[0], v4[0]));
					}
				}
			}
		}
		
		for (Triangle t : isogons){
			trianglesOut.add(new SurfacePoint(t.get1(), 0));
			trianglesOut.add(new SurfacePoint(t.get2(), 0));
			trianglesOut.add(new SurfacePoint(t.get3(), 0));
		}
		for (Triangle t : matgons){
			trianglesOut.add(new SurfacePoint(t.get1(), 2));
			trianglesOut.add(new SurfacePoint(t.get2(), 2));
			trianglesOut.add(new SurfacePoint(t.get3(), 2));
		}
		
	}

	@Override
	public List<SurfacePoint> extractSurface(HermiteExtractor hermiteData, BoundingBox bb, float resolution) {
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
		Vector3f vertexPoints[][][][] = new Vector3f[2][stepsX+2][stepsY+2][2];//[y],x,z
		HermiteCube cube[][][] = new HermiteCube[2][stepsX+2][stepsY+2];
		int yOff = 0;
		
		List<SurfacePoint> triangles = new ArrayList<SurfacePoint>(); 
		List<Triangle> isogons = new LinkedList<Triangle>(); 
		List<Triangle> matgons = new LinkedList<Triangle>();
		
		
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
//						int [] subSurf = VoxelSystemTables.subSurfaceEdges(hc);
//						for(int i: subSurf){
//
//							System.out.println("new Vector3f("+hc.normals[i].x+"f,"+hc.normals[i].y+"f,"+hc.normals[i].z+"f),");
//						}
						
						
						//Compute Cube Center:
						Vector3f centers[] = surfaceContour(hc,cP, diag);
						
						Vector3f center = cP[0].add(cP[1]).add(cP[2]).add(cP[3]).add(cP[4]).add(cP[5]).add(cP[6]).add(cP[7]);
						center = center.divide(8);
						
//						for(Vector3f v : hc.intersections){
//							if(v.distance(center) > .25f  * Math.sqrt(2)){
//								System.out.println("But why2 "+v.distance(center)+" "+ v + " "+ center);
//							}
//						}
						
						if(centers[0].distance(center) > .25f * Math.sqrt(2)){
							System.out.println("But why " + centers[0].distance(center) + " " + centers[0] + " " + center);
							centers = surfaceContour(hc,cP, diag);
						}
						vertexPoints[yOff][x+1][z+1][0] = centers[0];
						vertexPoints[yOff][x+1][z+1][1] = centers[1];
						cube[yOff][x+1][z+1] = hc;
					}else{
						//vertexPoints[yOff][x+1][z+1] = null;
						cube[yOff][x+1][z+1] = null;
						vertexPoints[yOff][x+1][z+1][0] = null;
						vertexPoints[yOff][x+1][z+1][1] = null;
					}
				
				}
			}
			
			//2 rows of Grid Cubes
			yOff++;
			if(yOff==2){
				yOff=1;
				//Generate Quads
				
				generateTriangles(vertexPoints,cube,hermiteData,triangles);
				Vector3f[][][] storeV=vertexPoints[0];
				HermiteCube[][] storeC=cube[0];
				
				
				vertexPoints[0] = vertexPoints[1];//last cube is now first
				vertexPoints[1] = storeV;
				
				cube[0] = cube[1];//last cube is now first
				cube[1] = storeC;
				
			}
			
		}
		
		
		//second pass, slice isogons
		
		
		return triangles;
	}
	
}
