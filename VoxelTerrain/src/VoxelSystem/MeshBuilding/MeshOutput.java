package VoxelSystem.MeshBuilding;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class MeshOutput {
	public MeshOutput(){
		iToV = new HashMap<Integer,Vector3f>();
		vToI = new HashMap<Vector3f, Integer>();
		triangles = new ArrayList<Integer>();
		verticies = new ArrayList<Vector3f>();
		vertexNormals = new ArrayList<Vector3f>();
		vertexTangents = new ArrayList<Vector3f>();
	}
	
	
	public final Map<Integer,Vector3f> iToV;
	public final Map<Vector3f,Integer> vToI;
	public final List<Integer> triangles; //TODO: Int buffer
	public final List<Vector3f> verticies;//TODO: float buffer
	public final List<Vector3f> vertexNormals;  
	public final List<Vector3f> vertexTangents;
	
	public Mesh compile(){
		IntBuffer indicies = BufferUtils.createIntBuffer(this.triangles.size());
		FloatBuffer verticies = BufferUtils.createFloatBuffer(this.verticies.size()*3);
		FloatBuffer normals = BufferUtils.createFloatBuffer(this.vertexNormals.size()*3);
		FloatBuffer tangents = BufferUtils.createFloatBuffer(this.vertexTangents.size()*4);
		
		Mesh m = new Mesh();
		normals.rewind();
		for(Vector3f v : vertexNormals){
			
			v.normalizeLocal();
			
//			if(!v.equals(new Vector3f(0f,1.0f,0))){
//				System.out.println(v);
//			}
			normals.put(v.x);
			normals.put(v.y);
			normals.put(v.z);
		}
		
		tangents.rewind();
		for(int i =0; i< vertexTangents.size(); i++){
			Vector3f v = vertexTangents.get(i);
			
			v.normalizeLocal();
//			System.out.println(v +" cross "+v.dot(vertexNormals.get(i)));
			tangents.put(v.x);
			tangents.put(v.y);
			tangents.put(v.z);
			tangents.put(1f);
		}
		
		indicies.rewind();
		for(int v : triangles){
			indicies.put(v);
		}
		
		verticies.rewind();
		for(int i =0; i < this.iToV.size();i++){
			Vector3f v = this.iToV.get(i);
			verticies.put(v.x);
			verticies.put(v.y);
			verticies.put(v.z);
		}
		verticies.flip();
		indicies.flip();
		normals.flip();
		
//		System.out.println(this.triangles.size());
//		System.out.println(verticies.limit());
//		System.out.println(normals.limit());
//		System.out.println(this.vertexNormals.size());
//		
		
		m.setBuffer(Type.Index, 3, indicies);
		m.setBuffer(Type.Position, 3, verticies);
		m.setBuffer(Type.Normal,3,normals);
//		m.setBuffer(Type.Tangent,4,tangents);	
		return m;
	}
	
}
