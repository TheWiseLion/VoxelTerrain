package VoxelSystem.Hermite;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

import VoxelSystem.VoxelSystemTables;
import VoxelSystem.DensityVolumes.DensityVolume;

public class HermiteDensityExtractor implements HermiteExtractor{
	DensityVolume dv;
	public HermiteDensityExtractor(DensityVolume dv){
		this.dv = dv;
	}
	@Override
	public BoundingBox getBoundingBox() {
		return dv.getEffectiveVolume();
	}
	
	@Override
	public HermiteEdge getEdge(Vector3f p1, Vector3f p2) {
		float d1 = dv.getDensity(p1.x, p1.y, p1.z);
		float d2 = dv.getDensity(p2.x, p2.y, p2.z);
		int material1 = dv.getType(p1.x,p1.y,p1.z);
		int material2 = dv.getType(p2.x,p2.y,p2.z);
		
		if(d1 < 0){ // is it air?
			material1 = -1;
		}
		
		if(d2 < 0){
			material2 = -1;
		}
		
		if(material1 == material2){ // edge only exists when materials are different
			return null;
		}else{
			//if (material1 == -1) d1 = 0;
			//if (material2 == -1) d2 = 0;
			
			
			Vector3f v = HermiteUtils.VertexInterp(p1, p2, d1,d2);
			Vector3f n = dv.getFieldDirection(v.x, v.y, v.z);
			//if (material1 != -1 && material2 != -1) n.set(0, 0, 1);
			return new HermiteEdge(v, n);
		}
	}
	

	private int[] getMaterial(Vector3f [] p, float [] v){
		int materials[] = new int[8];
		for(int i=0;i<8;i++){
			if(v[i] < 0){
				materials[i] = -1;
			}else{
				materials[i] =  dv.getType(p[i].x, p[i].y, p[i].z);
			}
		}
		
		return materials;
	}
	
	private float getD(Vector3f p){
		float d = dv.getDensity(p.x, p.y, p.z);
		if(d==0){
			d=.003f;
		}
		return d;
	}
	
	@Override
	public HermitePoint getPoint(Vector3f p) {
		float d1 = dv.getDensity(p.x, p.y, p.z);
		int material;
		if(d1<0){
			material =  -1;
		}else{
			material = dv.getType(p.x, p.y, p.z);
		}
		
				
		return new HermitePoint(material,d1);
	}
	
	@Override
	public HermiteCube getCube(Vector3f[] cubeCorners) {
		float cubeVals[] = new float[]{
			getD(cubeCorners[0]),getD(cubeCorners[1]),
			getD(cubeCorners[2]),getD(cubeCorners[3]),
			getD(cubeCorners[4]),getD(cubeCorners[5]),
			getD(cubeCorners[6]),getD(cubeCorners[7])
		}; 
		
		int materials[] = getMaterial(cubeCorners,cubeVals);
		int edgeInfo = VoxelSystemTables.getEdgeFromMaterials(materials); // "Active Edges"
		int numIntersections = Integer.bitCount(edgeInfo);//gets number of active edges
		
		
		HermiteCube hc = new HermiteCube();
		hc.materials = materials;
		hc.edgeInfo = edgeInfo;
		
		if(edgeInfo == 0){ //No active edges
			return hc;
		}
		
		int count = 0;
		Vector3f intersections[] = new Vector3f[numIntersections];//TODO?
		Vector3f normals[] = new Vector3f[numIntersections];//TODO?
		
		// For each pair of corners:
		for (int i = 0; i < 12; i++){ // 12 edges
			if ((edgeInfo & (1 << i)) == 0){
				continue;
			}
			
			int v1 = VoxelSystemTables.iTable[i*2];
			int v2 = VoxelSystemTables.iTable[i*2+1];
			// The first corner is set according to the intersection table.
			Vector3f corner1 = cubeCorners[v1];

			// Same with the second corner
			Vector3f corner2 = cubeCorners[v2];

			// Interpolate the intersection point with the surface.
			
			Vector3f intersection = VoxelSystemTables.getIntersection(corner1, corner2, cubeVals[v1], cubeVals[v2]);
			intersections[count] = intersection;
			normals[count] = dv.getFieldDirection(intersection.x, intersection.y, intersection.z);
			if (materials[v1] != -1 && materials[v2] != -1) normals[count].set(0, 0, 1);
			
			count++;
		}
		
		hc.intersections = intersections;
		hc.normals = normals;
		return hc;
	}
	
	
}
