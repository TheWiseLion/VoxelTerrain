package VoxelSystem.SurfaceExtractors;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class ExtractorUtils {
	private static final int MAX_ITERATIONS = 1000;
	private static float forceRatio = 0.05f;
	
	/**
	 * Computes the vertex for the cube, from Hermite data. Uses Leonardo
	 * Augusto Schmitz's excellent method, with exact normal at intersection
	 * points, to reduce complexity.
	 */
	public static Vector3f surfaceContour(Vector3f [] cubeEdges, Vector3f [] intersections, Vector3f [] normals, BoundingBox bb, float threshold) {
		threshold *= threshold;
	
		// Initial position is mid point between intersections:
		Vector3f masspoint = new Vector3f();
		for (Vector3f v: intersections) {
			masspoint.addLocal(v);
		}
		masspoint.divideLocal(intersections.length);
		Vector3f particlePosition = new Vector3f(masspoint);
		
		Vector3f forces[] = new Vector3f[8];
		
		//Only need to compute forces once per intersection
		//Compute the force at each cube corner
		for(int i = 0; i < forces.length; i++){
			Vector3f force = new Vector3f(0,0,0);
			//Sum of the forces between cube corner and distance to each intersection plane
			for(int z = 0; z < intersections.length; z++){
				force.addLocal(planeToPoint(intersections[z],normals[z],cubeEdges[i]));
			}
			forces[i] = force;
		}
		
		int iteration;
		Vector3f force = new Vector3f(0,0,0);
		
		for (iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
			force.set(0, 0, 0);

			//Compute force by tri-linearly interpolating from centroid (mass point)
			force.set(trilinear(particlePosition,forces,cubeEdges));
			
			//Now scale force by 5%
			force.multLocal(forceRatio);
			
			//Add force to centroid to obtain new centroid 
			particlePosition.addLocal(force);
			
			//do i return here? if needs to be clamped?
			
			//Check if force is within threshold
			if(force.lengthSquared() < threshold){
				break;
			}
			
		}
		

		//Check if centroid needs to be clamped
		if(!bb.contains(particlePosition)){
			particlePosition = clampToBox(particlePosition, bb);
		}
		
		return particlePosition;
	}
	
	/***
	 * Obtain the trilinear interpolation of the force at point P.
	 * p - the point to find the force for.
	 * forces - the forces at the cube corners
	 * corner - the position of the cube corers
	 */
	private static Vector3f trilinear(Vector3f p, Vector3f [] forces, Vector3f[] corners){
		Vector3f c000 = forces[0];
		Vector3f c100 = forces[1];
		Vector3f c101 = forces[5];
		Vector3f c001 = forces[4];
		
		Vector3f c010 = forces[3];
		Vector3f c110 = forces[2];
		Vector3f c111 = forces[6];
		Vector3f c011 = forces[7];

		float xPart = (p.x - corners[0].x)/ (corners[1].x-corners[0].x);//dx
		Vector3f c00 = c000.mult(1f-xPart).add(c100.mult(xPart));//c000*(1-xPart)+c100*(xPart)
		Vector3f c01 = c001.mult(1f-xPart).add(c101.mult(xPart));//c001*(1-xPart)+c101*(xPart)

		Vector3f c10 = c010.mult(1f-xPart).add(c110.mult(xPart));//c010*(1-xPart)+c110*(xPart)
		Vector3f c11 = c011.mult(1f-xPart).add(c111.mult(xPart));;//c011*(1-xPart)+c111*(xPart)

		float yPart = (p.y - corners[0].y)/(corners[3].y-corners[0].y);//dy
		Vector3f c0 = c00.mult(1f-yPart).add(c10.mult(yPart));//c00*(1-yPart)+c10*(yPart)
		Vector3f c1 = c01.mult(1f-yPart).add(c11.mult(yPart));//c01*(1-yPart)+c11*(yPart)

		
		float zPart = (p.z - corners[0].z)/(corners[4].z- corners[0].z);//dz
		Vector3f c = c0.mult(1-zPart).add(c1.mult(zPart));

		return c;
	}
	
	
	/**
	 * @param P - point on plane
	 * @param n - normal of plane
	 * @param Q - point not on plane
	 * @return vector from point Q to plane
	 */
	private static Vector3f planeToPoint(Vector3f P, Vector3f n, Vector3f Q){
		Vector3f PQ = Q.subtract(P);//point on plane to Q
		Vector3f proj = PQ.subtract(n.mult(PQ.dot(n)));//w
		Vector3f D = PQ.negate().add(proj);
		return D;
	}
	
	/***
	 * Clamps point to bounding box 
	 * @param p - point to be clamped
	 * @param bb - bounding box
	 * @return
	 */
	private static Vector3f clampToBox(Vector3f p, BoundingBox bb){
		Vector3f min = bb.getMin(new Vector3f());
		Vector3f max = bb.getMax(new Vector3f());
		if(p.x < min.x){
			p.x = min.x;
		}else if(p.x > max.x){
			p.x = max.x;
		}
		
		if(p.y < min.y){
			p.y = min.y;
		}else if(p.y > max.y){
			p.y = max.y;
		}
		
		
		if(p.z < min.z){
			p.z = min.z;
		}else if(p.z > max.z){
			p.z = max.z;
		}
		
		return p;
	}
	
	public static int getNearestType(Vector3f p, Vector3f[] cube, int materials[]){
		float min = Float.MAX_VALUE;
		int mat = -1;
		
		for(int i=0; i < cube.length; i++){
			if(materials[i] == -1){
				continue;
			}
			
			float d = cube[i].subtract(p).lengthSquared();
			if(d < min){
				min = d;
				mat = materials[i];
			}
		}
		
		return mat;
	}
}
