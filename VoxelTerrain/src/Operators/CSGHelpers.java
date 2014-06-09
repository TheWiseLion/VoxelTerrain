package Operators;

import DensityVolumes.DensityVolume;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class CSGHelpers {
	
	public abstract static class CSGCombinedVolume implements DensityVolume{
		public final DensityVolume dv1;
		public final DensityVolume dv2;
		private BoundingBox bb;
		private float delta;
		public CSGCombinedVolume(DensityVolume dv1, DensityVolume dv2,float delta){
			this.dv1=dv1;
			this.dv2=dv2;
			boolean hasVolume1 = dv1.getEffectiveVolume() != null;
			boolean hasVolume2 = dv2.getEffectiveVolume() != null;
			
			if(hasVolume1 && hasVolume2){
				bb = CSGHelpers.getIntersection(dv1.getEffectiveVolume(),dv2.getEffectiveVolume());
			}else if(hasVolume1){
				bb = dv1.getEffectiveVolume();
			}else if(hasVolume2){
				bb = dv2.getEffectiveVolume();
			}else{
				bb = null;
			}
			this.delta = delta;
		}
		
		@Override
		public abstract float getDensity(float x, float y, float z);
		
		
		@Override
		public final Vector3f getFieldDirection(float x, float y, float z) {
			float d = delta;
			double nx = getDensity(x + d, y, z) - getDensity(x - d, y, z);
	        double ny = getDensity(x, y + d, z) - getDensity(x, y - d, z);
	        double nz = getDensity(x, y, z + d) - getDensity(x, y, z - d);
	 
	        Vector3f normal = new Vector3f((float)-nx, (float)-ny, (float)-nz);
	        normal.normalizeLocal();
	        return normal;
		}

		@Override
		public final BoundingBox getEffectiveVolume() {
			return bb;
		
		}

		@Override
		public boolean isDiscrete() {
			return false;
		}
	}
	
	public static final BoundingBox getIntersection(BoundingBox bv1,BoundingBox bv2){
		if(!bv1.intersects(bv2)){
			throw new RuntimeException("Volumes don't interscet");
		}
		Vector3f min = bv1.getMin(new Vector3f());
    	Vector3f max = bv1.getMax(new Vector3f());
    	min.maxLocal(bv2.getMin(new Vector3f()));
    	max.minLocal(bv2.getMax(new Vector3f()));
    	return new BoundingBox(min,max);
	}
}
