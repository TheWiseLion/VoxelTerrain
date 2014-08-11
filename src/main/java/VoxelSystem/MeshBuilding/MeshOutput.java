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
		vertexBiTangents = new ArrayList<Vector3f>();
	}
	
	
	public final Map<Integer,Vector3f> iToV;
	public final Map<Vector3f,Integer> vToI;
	public final List<Integer> triangles; //TODO: Int buffer
	public final List<Vector3f> verticies;//TODO: float buffer
	public final List<Vector3f> vertexNormals;  
	public final List<Vector3f> vertexTangents;
	public final List<Vector3f> vertexBiTangents;
	
	public Mesh[] compile(boolean compileDebugInfo){
		IntBuffer indicies = BufferUtils.createIntBuffer(this.triangles.size());
		FloatBuffer verticies = BufferUtils.createFloatBuffer(this.verticies.size()*3);
		FloatBuffer normals = BufferUtils.createFloatBuffer(this.vertexNormals.size()*3);
		FloatBuffer tangents = BufferUtils.createFloatBuffer(this.vertexTangents.size()*4);
		
		for(Vector3f v : vertexNormals){
			
			v.normalizeLocal();
			normals.put(v.x);
			normals.put(v.y);
			normals.put(v.z);
		}
		
		for(int i =0; i< vertexTangents.size(); i++){
			Vector3f n = this.vertexNormals.get(i);
			
			Vector3f t = vertexTangents.get(i);
			Vector3f b = vertexBiTangents.get(i);
			
			// Calculate handedness
			float handedness = (n.cross(t).dot(b) < 0.0F) ? -1.0F : 1.0F;
			
			// Gram-Schmidt orthogonalize
	        t.set((t.subtract(n.mult(n.dot(t)))).normalize());
	        
			
			tangents.put(t.x);
			tangents.put(t.y);
			tangents.put(t.z);
			tangents.put(handedness);
		
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
		
		if(compileDebugInfo){
			Mesh[] out = new Mesh[]{new Mesh(),new Mesh(),new Mesh(),new Mesh()};
			FloatBuffer extraNormals = BufferUtils.createFloatBuffer(this.vertexNormals.size()*3*2);
			FloatBuffer extraTangents = BufferUtils.createFloatBuffer(this.vertexTangents.size()*3*2);
			FloatBuffer extraBitangents = BufferUtils.createFloatBuffer(this.vertexTangents.size()*3*2);
			//Gen extra info
			for(int i = 0; i < this.vertexTangents.size(); i++){
				Vector3f vv = this.verticies.get(i);
				Vector3f n = this.vertexNormals.get(i);
				Vector3f vf = vertexTangents.get(i);
				Vector3f bi=n.cross(vf).mult(.25f).add(vv);
				Vector3f tan = vf.mult(.25f).add(vv);
				Vector3f norm = n.mult(.25f).add(vv);
				extraTangents.put(vv.x);
				extraTangents.put(vv.y);
				extraTangents.put(vv.z);
				extraNormals.put(vv.x);
				extraNormals.put(vv.y);
				extraNormals.put(vv.z);
				extraBitangents.put(vv.x);
				extraBitangents.put(vv.y);
				extraBitangents.put(vv.z);
				
				extraTangents.put(tan.x);
				extraTangents.put(tan.y);
				extraTangents.put(tan.z);
				
				extraNormals.put(norm.x);
				extraNormals.put(norm.y);
				extraNormals.put(norm.z);

				extraBitangents.put(bi.x);
				extraBitangents.put(bi.y);
				extraBitangents.put(bi.z);
			}
			
			out[0].setBuffer(Type.Index, 3, indicies);
			out[0].setBuffer(Type.Position, 3, verticies);
			out[0].setBuffer(Type.Normal, 3, normals);
			out[0].setBuffer(Type.Tangent, 4, tangents);
			
			out[1].setBuffer(Type.Position,  3, extraTangents);
			out[2].setBuffer(Type.Position,  3, extraNormals);
			out[3].setBuffer(Type.Position,  3, extraBitangents);
			
			return out;
		}else{
			Mesh m = new Mesh();
			m.setBuffer(Type.Index, 3, indicies);
			m.setBuffer(Type.Position, 3, verticies);
			m.setBuffer(Type.Normal, 3, normals);
			m.setBuffer(Type.Tangent, 4, tangents);
			return new Mesh[]{m};
		}
		
		
	}
	
}
