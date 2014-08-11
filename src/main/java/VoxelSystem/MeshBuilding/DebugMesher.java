package VoxelSystem.MeshBuilding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class DebugMesher {
	
	
	/**
	 * Returns a Map of meshes. 1st is lines. all others are points with set material
	 * @return
	 */
	public static Geometry[] getQuadMesh(List<SurfacePoint> surfacePoints, Map<Integer, Material> tToMat,Material lineMaterial){
		Map<Integer,List<Vector3f>> typeToPoints = new HashMap<Integer,List<Vector3f>>();
		List<Vector3f> lines = new ArrayList<Vector3f>();
		
		List<Vector3f> pointX = new ArrayList<Vector3f>();
		List<Vector3f> pointY = new ArrayList<Vector3f>();
		List<Vector3f> pointZ = new ArrayList<Vector3f>();
		
		for(int i=0; i<surfacePoints.size(); i+=4){
			//0 1
			lines.add(surfacePoints.get(i).point);
			lines.add(surfacePoints.get(i+1).point);
			//1 2
			lines.add(surfacePoints.get(i+1).point);
			lines.add(surfacePoints.get(i+2).point);
			//2 3
			lines.add(surfacePoints.get(i+2).point);
			lines.add(surfacePoints.get(i+3).point);
			//4 1
			lines.add(surfacePoints.get(i+3).point);
			lines.add(surfacePoints.get(i).point);
			insert(surfacePoints.get(i),typeToPoints);
			insert(surfacePoints.get(i+1),typeToPoints);
			insert(surfacePoints.get(i+2),typeToPoints);
			insert(surfacePoints.get(i+3),typeToPoints);
		}
		
		Mesh lineMesh = new Mesh();
		lineMesh.setMode(Mode.Lines);
		lineMesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(lines.toArray(new Vector3f[0])));
		
		Geometry g[] = new Geometry[1+typeToPoints.size()];
		Geometry geo = new Geometry("lines", lineMesh);
		lineMesh.updateBound();
		lineMesh.updateCounts();
		geo.setMaterial(lineMaterial);
		g[0]=geo;
		
		int count = 1;
		for(Integer i : typeToPoints.keySet()){
			List<Vector3f> points = typeToPoints.get(i);
			Mesh m = new Mesh();
			m.setMode(Mode.Points);
			m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(points.toArray(new Vector3f[0])));
			m.updateBound();
			m.updateCounts();
			m.setPointSize(5f);
			
			geo = new Geometry("Points "+i, m);
			geo.setMaterial(tToMat.get(i));
			geo.setCullHint(CullHint.Never);
			
			g[count] = geo;
			count++;
		}
		return g;
	}
	
	
	/**
	 * Returns a Map of meshes. 1st is lines. all others are points with set material
	 * @return
	 */
	public static Geometry[] getTriangleMesh(List<SurfacePoint> surfacePoints, Map<Integer, Material> tToMat,Material lineMaterial){
		Map<Integer,List<Vector3f>> typeToPoints = new HashMap<Integer,List<Vector3f>>();
		List<Vector3f> lines = new ArrayList<Vector3f>();
		for(int i=0;i<surfacePoints.size();i+=3){
			//0 1
			lines.add(surfacePoints.get(i).point);
			lines.add(surfacePoints.get(i+1).point);
			//1 2
			lines.add(surfacePoints.get(i+1).point);
			lines.add(surfacePoints.get(i+2).point);
			//2 3
			lines.add(surfacePoints.get(i+2).point);
			lines.add(surfacePoints.get(i).point);
			insert(surfacePoints.get(i),typeToPoints);
			insert(surfacePoints.get(i+1),typeToPoints);
			insert(surfacePoints.get(i+2),typeToPoints);
		}
		
		Mesh lineMesh = new Mesh();
		lineMesh.setMode(Mode.Lines);
		lineMesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(lines.toArray(new Vector3f[0])));
		lineMesh.updateBound();
		lineMesh.updateCounts();
		
		Geometry g[] = new Geometry[1+typeToPoints.size()];
		Geometry geo = new Geometry("lines", lineMesh);
		geo.setMaterial(lineMaterial);
		g[0]=geo;
		
		
		int count = 1;
		for(Integer i : typeToPoints.keySet()){
			List<Vector3f> points = typeToPoints.get(i);
			Mesh m = new Mesh();
			m.setMode(Mode.Points);
			m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(points.toArray(new Vector3f[0])));
			m.updateBound();
			m.updateCounts();
			m.setPointSize(5f);
			
			geo = new Geometry("Points "+i, m);
			geo.setMaterial(tToMat.get(i));
			geo.setCullHint(CullHint.Never);
			g[count] = geo;
			count++;
		}
		return g;
	}
	
	
	private static void insert(SurfacePoint v, Map<Integer,List<Vector3f>> tToP){
		List<Vector3f> list = tToP.get(v.type);
		if(list == null){
			list= new ArrayList<Vector3f>();
			tToP.put(v.type, list);
		}
		list.add(v.point);
	}
	
}
