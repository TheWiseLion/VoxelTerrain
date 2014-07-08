package VoxelSystem.MeshBuilding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

/***
 * In this basic mesh builder triangles that have multiple types are
 * averaged to only one type.
 * @author 0xFFFF
 *
 */
public class BasicMesher {
	private HashMap<Integer, Vector3f> indexToVertex;
	private HashMap<Integer, Vector3f> indexToNormal;
	private HashMap<Integer, Vector3f[]> indexToTangents;
	private HashMap<Vector3f, Integer> vertexToIndex;
	private HashMap<Integer, List<int[]>> typeToTriangles;
	private static int triV1 =0, triV2 =1, triV3=2; //Vertex indicies
	public BasicMesher(){
		indexToVertex = new HashMap<Integer, Vector3f>();
		indexToNormal = new HashMap<Integer, Vector3f>();
		vertexToIndex = new HashMap<Vector3f, Integer>();
		indexToTangents = new HashMap<Integer,Vector3f[]>();
		typeToTriangles = new HashMap<Integer,List<int[]>>();
	}
	
	public void addTriangle(SurfacePoint sp1,SurfacePoint sp2,SurfacePoint sp3){
		//Pick the first type that matches all types
		//If non exists pick at random
		int type = sp1.type;
		if(sp1.type == sp2.type || sp1.type==sp3.type){
			type = sp1.type;
		}else{
			type = sp2.type;
		}
//		for(int t: sp1.types){
//			if(sp2.types.contains(t) && sp3.types.contains(t)){
//				type = t;
//				break;
//			}else{
//				type = t;
//			}
//		}
		
		Vector3f v1 = sp1.point;
		Vector3f v2 = sp2.point;
		Vector3f v3 = sp3.point;
		
		

		
		int [] triangleData = new int[3];
		Vector3f normal = computeNormal(v1,v2,v3);
		Vector2f [] texCoords = generateUV(v1, v2, v3, normal);
		Vector3f [] tangents = generateTangents(v1,v2,v3,texCoords);
		triangleData[triV1] = insertVertex(v1,normal,tangents);
		triangleData[triV2] = insertVertex(v2,normal,tangents);
		triangleData[triV3] = insertVertex(v3,normal,tangents);
		
		List<int[]> triangles = typeToTriangles.get(type);
		if(triangles==null){
			triangles = new ArrayList<int[]>();
			typeToTriangles.put(type,triangles);
		}
		triangles.add(triangleData);
		
	}
	
	public void addTriangles(List<SurfacePoint> surfacePoints){
		for(int i=0; i<surfacePoints.size();i+=3){
			addTriangle(surfacePoints.get(i),surfacePoints.get(i+1),surfacePoints.get(i+2));
		}
	}
	
	/***
	 * Returns a mesh for every "type" received as input
	 * @return
	 */
	public Map<Integer,Mesh> compileMeshes(){
		Map<Integer,Mesh> meshesOut = new HashMap<Integer,Mesh>();
		List<Integer> indexList = new ArrayList<Integer>();
		List<Vector3f> vertList = new ArrayList<Vector3f>();
		List<Vector3f> normalList = new ArrayList<Vector3f>();
		List<Vector4f> tangentList = new ArrayList<Vector4f>();
		
		for (Integer type : typeToTriangles.keySet()) {
//			System.out.println("Basic Mesher: "+type);
			List<int[]> triangle = typeToTriangles.get(type);
			System.out.println(type+": "+triangle.size());
			for (int i = 0; i < triangle.size(); i++) {
				int tri[] = triangle.get(i);
				for (int k = 2; k >= 0; k--) {
					indexList.add(tri[triV1 + k]);
					vertList.add(indexToVertex.get(tri[triV1 + k]));
					normalList.add(indexToNormal.get(tri[triV1 + k]));
					// As per jME documentation for tangent:
					Vector3f[] v = indexToTangents.get(tri[triV1 + k]);
					tangentList.add(new Vector4f(v[0].x, v[0].y, v[0].z, -1.0f));
				}
			}
			System.out.println("Type: "+type+" with "+vertList.size());
			Mesh m = new Mesh();
			m.setBuffer(Type.Position, 3,Format.Float, BufferUtils.createFloatBuffer(vertList.toArray(new Vector3f[0])));
	        m.setBuffer(Type.Normal, 3,Format.Float, BufferUtils.createFloatBuffer(normalList.toArray(new Vector3f[0])));
	        m.setBuffer(Type.Tangent, 4,Format.Float, BufferUtils.createFloatBuffer(tangentList.toArray(new Vector4f[0])));
	        meshesOut.put(type, m);
	        
	        indexList.clear();
	        vertList.clear();
	        normalList.clear();
	        tangentList.clear();
		}
		
//		System.out.println("Basic Mesher: "+meshesOut.size());
		return meshesOut;
	}
	
	
	
	///////////////////////////////////Helper/////////////////////////////////////////////////////////////
    /***
     * Outputs index. 
     * Adds contribution to vertex normal calculation.
     * @param v - vertex
     * @return 
     */
    private int insertVertex(Vector3f v, Vector3f normal, Vector3f tangents[]){
        Integer i = vertexToIndex.get(v);
        if(i==null){ //new vertex
             i = indexToVertex.size();
             indexToVertex.put(i,v);
             vertexToIndex.put(v,i);
             indexToNormal.put(i,normal);
             indexToTangents.put(i,tangents);
        }else{
        
        	//Normal of vert is normal of sum of normals:
        	indexToNormal.put(i,indexToNormal.get(i).add(normal));
        
        	//Sum our bi-tangents
        	Vector3f[] tans =  indexToTangents.get(i);
        	tans[0]=tans[0].add(tangents[0]);
        	tans[1]=tans[1].add(tangents[1]);
        }
        
        return i;
    }
	
	private Vector3f computeNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
		Vector3f e1 = new Vector3f(v2);
		Vector3f e2 = new Vector3f(v3);
		e1 = e1.subtract(v1);
		e2 = e2.subtract(v1);
		return e1.cross(e2).normalize();
	}

	private Vector3f[] generateTangents(Vector3f v0, Vector3f v1, Vector3f v2,Vector2f[] uvs) {
		// Edges of the triangle : postion delta
		Vector3f deltaPos1 = v1.subtract(v0);
		Vector3f deltaPos2 = v2.subtract(v0);
		
		// // UV delta
		Vector2f deltaUV1 = uvs[1].subtract(uvs[0]);
		Vector2f deltaUV2 = uvs[2].subtract(uvs[0]);

		float r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);

		// (deltaPos1 * deltaUV2.y - deltaPos2 * deltaUV1.y)*r;
		Vector3f tangent = scale(scale(deltaPos1, deltaUV2.y).subtract(scale(deltaPos2, deltaUV1.y)), r);
		// (deltaPos2 * deltaUV1.x - deltaPos1 * deltaUV2.x)*r
		Vector3f bitangent = scale(scale(deltaPos2, deltaUV1.x).subtract(scale(deltaPos1, deltaUV2.x)), r);
		return new Vector3f[] { tangent, bitangent };
	}

	private Vector3f scale(Vector3f v, float f) {
		return new Vector3f(v.x * f, v.y * f, v.z * f);
	}

	private Vector2f[] generateUV(Vector3f v1,Vector3f v2,Vector3f v3,Vector3f normal){
		Vector2f texCoord[] = new Vector2f[]{new Vector2f(),new Vector2f(),new Vector2f()};
		if(Math.abs(normal.y) > Math.abs(normal.z)){ 
			if(Math.abs(normal.y)>Math.abs(normal.x)){// y dominate so quoods z,x
				texCoord[0].set(v1.z,v1.x);
				texCoord[1].set(v2.z,v2.x);
				texCoord[2].set(v3.z,v3.x);
			}else{//x dominate, quoods y,z
				texCoord[0].set(v1.y,v1.z);
				texCoord[1].set(v2.y,v2.z);
				texCoord[2].set(v3.y,v3.z);
			}
		}else{ 
			if(Math.abs(normal.z)>Math.abs(normal.x)){ // z dominant, quoords y,x
				texCoord[0].set(v1.y,v1.x);
				texCoord[1].set(v2.y,v2.x);
				texCoord[2].set(v3.y,v3.x);
			}else{ //x dominant y,z
				texCoord[0].set(v1.y,v1.z);
				texCoord[1].set(v2.y,v2.z);
				texCoord[2].set(v3.y,v3.z);
			}
		}
		return texCoord;
    }
	


}
