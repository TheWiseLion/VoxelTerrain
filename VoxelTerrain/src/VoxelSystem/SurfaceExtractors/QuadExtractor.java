package VoxelSystem.SurfaceExtractors;

import java.util.List;
import java.util.Map;

import VoxelSystem.MeshBuilding.MeshOutput;
import VoxelSystem.MeshBuilding.SurfacePoint;

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
		
		Vector3f lNormal;
		Vector2f uv0;
		Vector2f uv1;
		Vector2f uv2;
		Vector3f tangent;
		
		//Check for degenerate triangles:
		boolean invalid1 = fuzzyEquals(v1,v2) || fuzzyEquals(v2,v3) || fuzzyEquals(v1,v3);
		boolean invalid2 = fuzzyEquals(v1,v3) || fuzzyEquals(v3,v4) || fuzzyEquals(v4,v1);
		if(invalid1 && invalid2){
			return;
		}else if(!invalid1){
			if(cw){
				lNormal = computeNormal(v1,v2,v3);
				uv0 = generateUV(v1, lNormal);
				uv1 = generateUV(v2, lNormal);
				uv2 = generateUV(v3, lNormal);
				tangent = generateTangent(v1, v2, v3, uv0, uv1, uv2);
			}else{
				lNormal = computeNormal(v1,v3,v2);
				uv0 = generateUV(v1, lNormal);
				uv1 = generateUV(v2, lNormal);
				uv2 = generateUV(v3, lNormal);
				tangent = generateTangent(v1, v3, v2, uv0, uv2, uv1);
			}
		}else{
			if(cw){
				lNormal = computeNormal(v1,v3,v4);
				uv0 = generateUV(v1, lNormal);
				uv1 = generateUV(v3, lNormal);
				uv2 = generateUV(v4, lNormal);
				tangent = generateTangent(v1, v3, v4, uv0, uv1, uv2);
			}else{
				lNormal = computeNormal(v1,v4,v3);
				uv0 = generateUV(v1, lNormal);
				uv1 = generateUV(v4, lNormal);
				uv2 = generateUV(v3, lNormal);
				tangent = generateTangent(v1, v4, v3, uv0, uv1, uv2);
			}
		}
		
		
		if(cw){
			//Vector3f lNormal = new Vector3f(normal);
			if(!support){
				if(!invalid1){
					mo.triangles.add(getTriIndex(v1, lNormal, tangent, mo));
					mo.triangles.add(getTriIndex(v2, lNormal, tangent, mo));
					mo.triangles.add(getTriIndex(v3, lNormal, tangent, mo));
				}
				
				if(!invalid2){
					mo.triangles.add(getTriIndex(v1, lNormal, tangent, mo));
					mo.triangles.add(getTriIndex(v3, lNormal, tangent, mo));
					mo.triangles.add(getTriIndex(v4, lNormal, tangent, mo));
				}
			}else{
				if(!invalid1){
					getTriIndex(v1, lNormal, tangent, mo);
					getTriIndex(v2, lNormal, tangent, mo);
					getTriIndex(v3, lNormal, tangent, mo);
				}
				
				if(!invalid2){
					getTriIndex(v1, lNormal, tangent, mo);
					getTriIndex(v3, lNormal, tangent, mo);
					getTriIndex(v4, lNormal, tangent, mo);
				}
			}
		}else{
			//winding is reversed. So reverse our normal.
			//Vector3f lNormal = new Vector3f(normal).negateLocal();
			if(!support){
				if(!invalid1){
					mo.triangles.add(getTriIndex(v1,lNormal,tangent,mo));
					mo.triangles.add(getTriIndex(v3,lNormal,tangent,mo));
					mo.triangles.add(getTriIndex(v2,lNormal,tangent,mo));
				}
				
				if(!invalid2){
					mo.triangles.add(getTriIndex(v1,lNormal,tangent,mo));
					mo.triangles.add(getTriIndex(v4,lNormal,tangent,mo));
					mo.triangles.add(getTriIndex(v3,lNormal,tangent,mo));
				}
			}else{
				if(!invalid1){
					getTriIndex(v1, lNormal, tangent, mo);
					getTriIndex(v2, lNormal, tangent, mo);
					getTriIndex(v3, lNormal, tangent, mo);
				}
				
				if(!invalid2){
					getTriIndex(v1, lNormal, tangent, mo);
					getTriIndex(v3, lNormal, tangent, mo);
					getTriIndex(v4, lNormal, tangent, mo);
				}
			}
		}
	}
	
	private static int getTriIndex(Vector3f v, Vector3f normal, Vector3f tangent, MeshOutput mo) {
		Integer i = mo.vToI.get(v);
		if (i == null) {
			i = mo.verticies.size();
			mo.iToV.put(i, v);
			mo.vToI.put(v, i);
			mo.verticies.add(v);
			mo.vertexNormals.add(new Vector3f(normal));
			mo.vertexTangents.add(new Vector3f(tangent));
		}else{
			mo.vertexNormals.get(i).addLocal(normal);
			mo.vertexTangents.get(i).addLocal(tangent);
		}
		
		
		return i;
	}

	private static Vector2f generateUV(Vector3f v1, Vector3f normal) {
		Vector2f texCoord = new Vector2f();
		if (Math.abs(normal.y) > Math.abs(normal.z)) {
			if (Math.abs(normal.y) > Math.abs(normal.x)) {// y dominate so
															// quoods z,x
				texCoord.set(v1.z, v1.x);
			} else {// x dominate, quoods y,z
				texCoord.set(v1.y, v1.z);
			}
		} else {
			if (Math.abs(normal.z) > Math.abs(normal.x)) { // z dominant,
															// quoords y,x
				texCoord.set(v1.y, v1.x);
			} else { // x dominant y,z
				texCoord.set(v1.y, v1.z);
			}
		}
		return texCoord;
	}

	private static Vector3f generateTangent(Vector3f v0, Vector3f v1, Vector3f v2, Vector2f uv0, Vector2f uv1, Vector2f uv2) {
		// Edges of the triangle : postion delta
		Vector3f deltaPos1 = v1.subtract(v0);
		Vector3f deltaPos2 = v2.subtract(v0);

		// UV delta
		Vector2f deltaUV1 = uv1.subtract(uv0);
		Vector2f deltaUV2 = uv2.subtract(uv0);

		float r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);

		// (deltaPos1 * deltaUV2.y - deltaPos2 * deltaUV1.y)*r;
		Vector3f tangent = scale( scale(deltaPos1, deltaUV2.y).subtract( scale(deltaPos2, deltaUV1.y)), r);
		// (deltaPos2 * deltaUV1.x - deltaPos1 * deltaUV2.x)*r
		// Vector3f bitangent =  scale(scale(deltaPos2,deltaUV1.x).subtract(scale(deltaPos1, deltaUV2.x)),r);
		return tangent.normalizeLocal();
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
