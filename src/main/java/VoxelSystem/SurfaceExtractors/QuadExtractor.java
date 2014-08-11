package voxelsystem.surfaceextractors;

import java.util.List;
import java.util.Map;

import voxelsystem.meshbuilding.MeshOutput;
import voxelsystem.meshbuilding.SurfacePoint;

import com.jme3.math.Vector2f;
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
//			System.out.println("Entry");
			throw new RuntimeException("asdasd");
		}
		
		SurfacePoint sp1 = new SurfacePoint(v1,type);
		SurfacePoint sp2 = new SurfacePoint(v2,type);
		SurfacePoint sp3 = new SurfacePoint(v3,type);
		SurfacePoint sp4 = new SurfacePoint(v4,type);
		
//		if(v1.y < 0 && v1.x<2.0 && v1.z>-2.0){
//			System.out.println(v1 +","+v2+","+v3 +","+v4 );
//			
//		}
		
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
	
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////CORE MESH MAKING///////////////////////////
	/////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////
	/***
	 * Takes 4 verticies of the quad.
	 * Takes the normal of the quad.
	 * Takes rotation of quad.
	 * Takes quad's type
	 * If it's a "supporting" quad then it's used to compute
	 * vertex normals only (aka shares boundary with this mesh)
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param v4
	 * @param normal
	 * @param tri
	 * @param cw
	 * @param type
	 * @param support
	 */
	public static void windingQuadToTriangle(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, Vector3f normal, Map<Integer, MeshOutput> tri, boolean cw, int type, boolean support){
		if(v1 == null || v2 == null || v3 == null || v4 == null){
			throw new RuntimeException("asdasd");
		}
		
		//TODO: confirm triangle winding...
		//TODO: Store in float buffer.
		MeshOutput mo = tri.get(type);
		if(mo == null){
			mo = new MeshOutput();
			tri.put(type, mo);
		}
		
	
		
		//Check for degenerate triangles:
		boolean invalid1 = fuzzyEquals(v1,v2) || fuzzyEquals(v2,v3) || fuzzyEquals(v1,v3);
		boolean invalid2 = fuzzyEquals(v1,v3) || fuzzyEquals(v3,v4) || fuzzyEquals(v4,v1);
		if(invalid1 && invalid2){
			return;
		}
		
		Vector3f lNormal;
		Vector2f uv0 = new Vector2f();
		Vector2f uv1  = new Vector2f();
		Vector2f uv2  = new Vector2f();
		Vector3f tangent = new Vector3f();
		Vector3f biTangent = new Vector3f();
		
		if(!invalid1){
			if(cw){
				lNormal = computeNormal(v1,v2,v3);
				generateTangent(v1, v2, v3,lNormal,tangent,biTangent);
			}else{
				lNormal = computeNormal(v1,v3,v2);
				generateTangent(v1, v3, v2,lNormal,tangent,biTangent);
			}
		}else{
			if(cw){
				lNormal = computeNormal(v1,v3,v4);
				generateTangent(v1, v3, v4, lNormal,tangent,biTangent);
			}else{
				lNormal = computeNormal(v1,v4,v3);
				generateTangent(v1, v4, v3,lNormal,tangent,biTangent);
			}
		}
		
		
		if(cw){
			//Vector3f lNormal = new Vector3f(normal);
			if(!support){
				if(!invalid1){
					mo.triangles.add(getTriIndex(v1, lNormal, tangent, biTangent, mo));
					mo.triangles.add(getTriIndex(v2, lNormal, tangent, biTangent, mo));
					mo.triangles.add(getTriIndex(v3, lNormal, tangent, biTangent, mo));
				}
				
				if(!invalid2){
					mo.triangles.add(getTriIndex(v1, lNormal, tangent, biTangent, mo));
					mo.triangles.add(getTriIndex(v3, lNormal, tangent, biTangent, mo));
					mo.triangles.add(getTriIndex(v4, lNormal, tangent, biTangent, mo));
				}
			}else{
				if(!invalid1){
					getTriIndex(v1, lNormal, tangent, biTangent, mo);
					getTriIndex(v2, lNormal, tangent, biTangent, mo);
					getTriIndex(v3, lNormal, tangent, biTangent, mo);
				}
				
				if(!invalid2){
					getTriIndex(v1, lNormal, tangent, biTangent, mo);
					getTriIndex(v3, lNormal, tangent, biTangent, mo);
					getTriIndex(v4, lNormal, tangent, biTangent, mo);
				}
			}
		}else{
			//winding is reversed. So reverse our normal.
			//Vector3f lNormal = new Vector3f(normal).negateLocal();
			if(!support){
				if(!invalid1){
					mo.triangles.add(getTriIndex(v1,lNormal,tangent, biTangent,mo));
					mo.triangles.add(getTriIndex(v3,lNormal,tangent, biTangent,mo));
					mo.triangles.add(getTriIndex(v2,lNormal,tangent, biTangent,mo));
				}
				
				if(!invalid2){
					mo.triangles.add(getTriIndex(v1,lNormal,tangent, biTangent,mo));
					mo.triangles.add(getTriIndex(v4,lNormal,tangent, biTangent,mo));
					mo.triangles.add(getTriIndex(v3,lNormal,tangent, biTangent,mo));
				}
			}else{
				if(!invalid1){
					getTriIndex(v1, lNormal, tangent, biTangent, mo);
					getTriIndex(v2, lNormal, tangent, biTangent, mo);
					getTriIndex(v3, lNormal, tangent, biTangent, mo);
				}
				
				if(!invalid2){
					getTriIndex(v1, lNormal, tangent, biTangent, mo);
					getTriIndex(v3, lNormal, tangent, biTangent, mo);
					getTriIndex(v4, lNormal, tangent, biTangent, mo);
				}
			}
		}
	}
	
	private static int getTriIndex(Vector3f v, Vector3f normal, Vector3f tangent,Vector3f bitangent, MeshOutput mo) {
		Integer i = mo.vToI.get(v);
		if (i == null) {
			i = mo.verticies.size();
			mo.iToV.put(i, v);
			mo.vToI.put(v, i);
			mo.verticies.add(v);
			mo.vertexNormals.add(new Vector3f(normal));
			mo.vertexTangents.add(new Vector3f(tangent));
			mo.vertexBiTangents.add(new Vector3f(bitangent));
		}else{
			mo.vertexNormals.get(i).addLocal(normal);
			mo.vertexTangents.get(i).addLocal(tangent);
			mo.vertexBiTangents.get(i).addLocal(bitangent);
		}
		
//		if(Math.abs(normal.dot(tangent)) > .001){
//			System.out.println("Bitch i got problems.");
//		}
		
		return i;
	}

	private static int generateUV(Vector3f v1, Vector3f normal,Vector2f store) {
		int i = 0;
		if (Math.abs(normal.y) > Math.abs(normal.z)) {
			
			//Y,Z dom -
			
			if (Math.abs(normal.y) > Math.abs(normal.x)) {// y dominate
				if(Math.abs(normal.z) > Math.abs(normal.x)){
					store.set(v1.x, v1.z);
				}else{
					store.set(v1.z, v1.x);
				}
				
			} else {// x dominate
				store.set(v1.y, v1.z);
			}
		} else {
			if (Math.abs(normal.z) > Math.abs(normal.x)) { // z dominant
				store.set(v1.y, v1.x);
			} else { // x dominant y,z
				store.set(v1.y, v1.z);
			}
		}
		return i;
	}

	private static void generateTangent(Vector3f v0, Vector3f v1, Vector3f v2,Vector3f normal, Vector3f tangent, Vector3f bitangent) {
		// Edges of the triangle : postion delta
//		System.out.println(uv0);
//		System.out.println(uv1);
//		System.out.println(uv2);
		Vector2f uv0 = new Vector2f();
		Vector2f uv1 = new Vector2f();
		Vector2f uv2 = new Vector2f();
		int a = generateUV(v0,normal,uv0);
		generateUV(v1,normal,uv1);
		generateUV(v2,normal,uv2);
		
		//Method from:
		//http://www.terathon.com/code/tangent.html
		
		 float x1 = v1.x - v0.x;
	     float x2 = v2.x - v0.x;
	     float y1 = v1.y - v0.y;
	     float y2 = v2.y - v0.y;
	     float z1 = v1.z - v0.z;
	     float z2 = v2.z - v0.z;
	        
	     float s1 = uv1.x - uv0.x;
	     float s2 = uv2.x - uv0.x;
	     float t1 = uv1.y - uv0.y;
	     float t2 = uv2.y - uv0.y;

		float r = 1.0f / (s1 * t2 - s2 * t1);
		
		tangent.set((t2 * x1 - t1 * x2) * r, (t2 * y1 - t1 * y2) * r, (t2 * z1 - t1 * z2) * r);
		bitangent.set((s1 * x2 - s2 * x1) * r, (s1 * y2 - s2 * y1) * r, (s1 * z2 - s2 * z1) * r);
	}

	private static Vector3f scale(Vector3f v, float f) {
		return new Vector3f(v.x * f, v.y * f, v.z * f);
	}

	private static Vector2f scale(Vector2f v, float f) {
		return new Vector2f(v.x * f, v.y * f);
	}

	private static Vector3f computeNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
		Vector3f e1 = new Vector3f(v2);
		Vector3f e2 = new Vector3f(v3);
		e1 = e1.subtract(v1);
		e2 = e2.subtract(v1);
		return e1.cross(e2).normalize();
//		return new Vector3f(0,1,0);
	}
	
	private static boolean fuzzyEquals(Vector3f v1, Vector3f v2){
		if(v1.subtract(v2).lengthSquared() < .0001f){
			return true;
		}else{
			return false;
		}
	}
	
}
