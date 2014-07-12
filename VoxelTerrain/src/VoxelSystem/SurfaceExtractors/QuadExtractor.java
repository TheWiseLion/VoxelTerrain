package VoxelSystem.SurfaceExtractors;

import java.util.List;

import VoxelSystem.MeshBuilding.SurfacePoint;

import com.jme3.math.Vector3f;

public class QuadExtractor {

	
	public static void windingQuadToQuad(SurfacePoint sp1, SurfacePoint sp2, SurfacePoint sp3, SurfacePoint sp4, List<SurfacePoint> tri, boolean cw){
		//TODO: Check for degenerate cases
		
		if(cw){
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
	
	//TODO: add triangle... (CHECKS FOR DEGENERATE TRIANGLE)//Step 1: Check for degenerate case
	public static void addTriangle(Vector3f v1, Vector3f v2, Vector3f v3,List<SurfacePoint> tri, boolean cw, int type){
		SurfacePoint sp1 = new SurfacePoint(v1,type);
		SurfacePoint sp2 = new SurfacePoint(v2,type);
		SurfacePoint sp3 = new SurfacePoint(v3,type);
		if(cw){
			tri.add(sp1);
			tri.add(sp2);
			tri.add(sp3);
		}else{
			tri.add(sp1);
			tri.add(sp3);
			tri.add(sp2);
		}
	}
	
	public static void windingQuadToTriangle(SurfacePoint sp1, SurfacePoint sp2, SurfacePoint sp3, SurfacePoint sp4, List<SurfacePoint> tri, boolean cw,int type){//int type
		//TODO: Check for degenerate cases
		
		//2 cases:
		//1. all types match
		//2. 3 or few types match (8 triangles get generated
		if(sp1.type==sp2.type && sp2.type == sp3.type && sp3.type == sp4.type){
//			addTriangle(sp1,sp2,sp3,tri,cw,type);
//			addTriangle(sp1,sp3,sp4,tri,cw,type);
			
			if(cw){
				addTriangle(sp1.point,sp2.point,sp3.point,tri,cw,type);
//				tri.add(sp1);
//				tri.add(sp2);
//				tri.add(sp3);
				
				addTriangle(sp1.point,sp3.point,sp4.point,tri,cw,type);
//				tri.add(sp1);
//				tri.add(sp3);
//				tri.add(sp4);
			}else{
				addTriangle(sp1.point,sp3.point,sp2.point,tri,cw,type);
//				tri.add(sp1);
//				tri.add(sp3);
//				tri.add(sp2);
				
				addTriangle(sp1.point,sp4.point,sp3.point,tri,cw,type);
//				tri.add(sp1);
//				tri.add(sp4);
//				tri.add(sp3);
			}
		}else{
			//Step 1: "find" transition point (for now its just the mid point)
			
			Vector3f midPoint1 = new Vector3f(sp1.point).addLocal(sp2.point).multLocal(.5f);// between 1 and 2
			Vector3f midPoint2 = new Vector3f(sp2.point).addLocal(sp3.point).multLocal(.5f);// between 2 and 3
			Vector3f midPoint3 = new Vector3f(sp3.point).addLocal(sp4.point).multLocal(.5f);// between 3 and 4
			Vector3f midPoint4 = new Vector3f(sp1.point).addLocal(sp4.point).multLocal(.5f);// between 1 and 4
			
			//Step 2: get centroid of quad
			Vector3f centroid = new Vector3f();
			centroid.addLocal(sp1.point).addLocal(sp2.point).addLocal(sp3.point).addLocal(sp4.point).multLocal(.25f);
			
			addTriangle(sp1.point,midPoint1,centroid,tri,cw,type);
			addTriangle(midPoint4,sp1.point,centroid,tri,cw,type);
//			
			addTriangle(midPoint1,sp2.point,centroid,tri,cw,type);
			addTriangle(centroid,sp2.point,midPoint2,tri,cw,type);
//	
			addTriangle(centroid,midPoint2,sp3.point,tri,cw,type);
			addTriangle(centroid,sp3.point,midPoint3,tri,cw,type);
			
			addTriangle(centroid,midPoint3,sp4.point,tri,cw,type);
			addTriangle(midPoint4,centroid,sp4.point,tri,cw,type);
		}
		
		
		
		
		
		
		
	}
	
	public static void windingQuadToTriangle(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, List<SurfacePoint> tri, boolean cw, int type){
		//TODO: Check for degenerate cases
		
		if(v1 == null || v2 == null || v3 == null || v4 == null){
			System.out.println("Entry");
		}
		
		SurfacePoint sp1 = new SurfacePoint(v1,type);
		SurfacePoint sp2 = new SurfacePoint(v2,type);
		SurfacePoint sp3 = new SurfacePoint(v3,type);
		SurfacePoint sp4 = new SurfacePoint(v4,type);
		
		
		if(cw){
			tri.add(sp1);
			tri.add(sp2);
			tri.add(sp3);
			
			tri.add(sp1);
			tri.add(sp3);
			tri.add(sp4);
		}else{
			tri.add(sp1);
			tri.add(sp3);
			tri.add(sp2);
			
			tri.add(sp1);
			tri.add(sp4);
			tri.add(sp3);
		}
	}
	
}
