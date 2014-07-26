package VoxelSystem.VoxelData;

import VoxelSystem.VoxelSystemTables;
import VoxelSystem.DensityVolumes.DensityVolume;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class VoxelDensityExtractor extends ExtractorBase{
	DensityVolume dv;
	public VoxelDensityExtractor(DensityVolume dv){
		this.dv = dv;
	}
	
	@Override
	public BoundingBox getBoundingBox() {
		return dv.getEffectiveVolume();
	}
	
	/**
	 * returns percent from v1 to v2
	 */
	private static float getLerp(Vector3f v1, Vector3f v2, Vector3f mp){
		Vector3f l = v1.subtract(v2);
		Vector3f n = v2.subtract(mp);
		return 1.0f - (Math.abs(n.x+n.y+n.z)/Math.abs(l.x+l.y+l.z));
	}
	
	@Override
	public HermiteEdge getEdge(Vector3f p1, Vector3f p2) {
		
		HermiteEdge he = new HermiteEdge();
		
		he.t1 = dv.getType(p1.x,p1.y,p1.z);
		he.t2 = dv.getType(p2.x,p2.y,p2.z);
		
		
		if(he.t1 != he.t2){ //edge must exist
			float d0 = dv.getDensity(p1.x, p1.y, p1.z);
			float d1 = dv.getDensity(p2.x, p2.y, p2.z);
			Vector3f v = VoxelSystemTables.getIntersection(p1, p2, d0, d1);
			he.normal = dv.getSurfaceNormal(v.x, v.y, v.z);
			he.intersection = getLerp(p1,p2,v); //TODO: do it less stupid please...
		}
		
		return he;
	}
	
}
