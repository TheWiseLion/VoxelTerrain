package VoxelSystem.SurfaceExtractors;

import java.util.List;

import VoxelSystem.MeshBuilding.SurfacePoint;

import com.jme3.math.Vector3f;

public class QuadExtractor {
	/***
	 * Worst case: quad decomposes into 8 triangles each of a different type
	 * 
	 * @param quad corners
	 * @param edges - intercept points (and 'inside' type) 
	 * 				  
	 * @return
	 */
	void extractTriangles(List<Vector3f> quadCorners,List<SurfacePoint> edges,List<SurfacePoint> triangles, boolean rev){
		//Up to 4 types in this quad.
		//(0 -> 0,1),(1-> 1,2),(2 -> 2,3),(3 -> 3,4),(4 -> 4,1)
		//We'll split each case as to reduce the number of triangles in this mesh
		boolean b = (edges.get(0).type == edges.get(1).type) 
					&& (edges.get(1).type == edges.get(2).type) 
					&& (edges.get(2).type == edges.get(3).type) 
					&& (edges.get(3).type == edges.get(4).type);
		Vector3f q1 = quadCorners.get(0);
		Vector3f q2 = quadCorners.get(1);
		Vector3f q3 = quadCorners.get(2);
		Vector3f q4 = quadCorners.get(3);
		if(b){ //most common case
			int  t =edges.get(0).type;
			
			SurfacePoint s1 = new SurfacePoint(q1,t);
			SurfacePoint s2 = new SurfacePoint(q2,t);
			SurfacePoint s3 = new SurfacePoint(q3,t);
			SurfacePoint s4 = new SurfacePoint(q4,t);
			
			winding(s1,s4,s3, triangles, rev);
			winding(s1,s2,s3, triangles, rev);
			return;
		}
		
		
//		if(hs.size() == 2){
//			
//		}else if(hs.size() == 3){
//			
//		}else{
//			
//		}
		
		LineSegment s1 = new LineSegment(edges.get(0).point, edges.get(2).point);
		LineSegment s2 = new LineSegment(edges.get(1).point, edges.get(3).point);
		Vector3f I = findIntersectionPoint(s1,s2); //sorta slow...
		
		SurfacePoint e1 = edges.get(0);
		SurfacePoint e2 = edges.get(1);
		SurfacePoint e3 = edges.get(2);
		SurfacePoint e4 = edges.get(3);
		
		//Triangle1: q1 + I + e1
		winding(new SurfacePoint(q1, e1.type), e1, new SurfacePoint(I,e1.type), triangles, rev);
		
		//Triangle2: e1 + I + q2
		winding(e1,new SurfacePoint(I,e1.type), new SurfacePoint(q2, e1.type), triangles, rev);
		
		//Triangle3: q2 + I + e2
		winding(new SurfacePoint(q2, e2.type), new SurfacePoint(I,e2.type),e2, triangles, rev);
		
		//Triangle4: I + q3 + e2
		winding(new SurfacePoint(I,e2.type), new SurfacePoint(q3, e2.type),e2, triangles, rev);
		
		//Triangle5: I + e3 + q3
		winding(new SurfacePoint(I,e3.type), e3, new SurfacePoint(q3, e3.type), triangles, rev);
		
		//Triangle6: I + q4 + e3
		winding(new SurfacePoint(I,e3.type), new SurfacePoint(q4, e3.type), e3,  triangles, rev);
		
		//Triangle7: I + e4 + q4
		winding(new SurfacePoint(I,e4.type), e4, new SurfacePoint(q4, e4.type), triangles, rev);
		
		//Triangle 8: q1 + e4 + I
		winding(new SurfacePoint(q1, e4.type), e4, new SurfacePoint(I,e4.type), triangles, rev);
		
//		return null;
	}
	
	
	
	private static class LineSegment{
		Vector3f p1,p2;
		public LineSegment(Vector3f p1,Vector3f p2){
			this.p1= p1;
			this.p2 = p2;
		}
	}
	
	private Vector3f findIntersectionPoint(LineSegment s1, LineSegment s2){
		// Point = s1.p1 + t*(s1.p2-s1.p1) = s2.p1 + u*(s2.p2-s2.p1)
		// stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
		
		Vector3f r = s1.p2.subtract(s1.p1);
		Vector3f s = s2.p2.subtract(s2.p1);
		float f = s2.p1.subtract(s2.p1).cross(r).lengthSquared()/(r.cross(s).lengthSquared());
		f = (float) Math.sqrt(f);
		if(f < 0 || f > 1){
			throw new RuntimeException("Line segment error");
		}
		
		Vector3f point = new Vector3f(s).scaleAdd(f, s2.p1);
		return point;
	}
	private static boolean error(SurfacePoint sp){
		if(sp == null || sp.point==null){
			return true;
		}else{
//			if(sp.type==1 && sp.point.x > 0){
//				System.out.println("new Vector3f("+sp.point.x+"f"+","+sp.point.y+"f"+","+sp.point.z+"f"+"),");
//			}
			
			return false;
		}
	}
	public static void winding(SurfacePoint sp1, SurfacePoint sp2, SurfacePoint sp3, List<SurfacePoint> tri, boolean rev){
		if(error(sp1)||error(sp2)||error(sp3)){
			return;
		}

		//Check foe degenerate case:
		if(sp1.point.equals(sp2.point)|| sp1.equals(sp2.point) || sp2.point.equals(sp3.point)){
			return;
		}
		
		if(rev){
			tri.add(sp1);
			tri.add(sp2);
			tri.add(sp3);
			
		}else{
			tri.add(sp1);
			tri.add(sp3);
			tri.add(sp2);
		}
	}
	
	public static void winding(SurfacePoint sp1, SurfacePoint sp2, SurfacePoint sp3, SurfacePoint sp4, List<SurfacePoint> tri, boolean rev){
		if(error(sp1)||error(sp2)||error(sp3)||error(sp4)){
			return;
		}
		
		if(rev){
			tri.add(sp1);
			tri.add(sp2);
			tri.add(sp3);
			tri.add(sp4);
			
		}else{
			tri.add(sp1);
			tri.add(sp4);
			tri.add(sp3);
			tri.add(sp2);
		}
	}
}
