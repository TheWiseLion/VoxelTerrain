package Operators;

import DensityVolumes.DensityVolume;
import DensityVolumes.DiscreteVolume;
import Operators.CSGHelpers.CSGCombinedVolume;

public class Intersection implements CSGOperator{

	private class DensityVolumeUnion extends CSGCombinedVolume{
		public DensityVolumeUnion(DensityVolume dv1, DensityVolume dv2){
			super(dv1,dv2,.001f);
		}
		
		@Override
		public float getDensity(float x, float y, float z) {
			return Math.min(dv1.getDensity(x, y, z), dv2.getDensity(x, y, z));
		}
		
	}
	

	@Override
	public DensityVolume opperate(DensityVolume dv1, DensityVolume dv2) {
		boolean discrete1 = dv1.isDiscrete();
		boolean discrete2 = dv2.isDiscrete();
		boolean hasVolume1 = dv1.getEffectiveVolume() != null;
		boolean hasVolume2 = dv2.getEffectiveVolume() != null;
		
		
		if(hasVolume1 && hasVolume2 && !dv1.getEffectiveVolume().intersects(dv2.getEffectiveVolume())){
			throw new IllegalArgumentException("Volumes must intersect");
		}
		
		DensityVolumeUnion union =new DensityVolumeUnion(dv1,dv2);
		
		if(discrete1 || discrete2){ 
			DiscreteVolume ddv;
			if(discrete1){
				ddv = (DiscreteVolume) dv1;
			}else{
				ddv = (DiscreteVolume) dv2;
			}
			
			ddv.extract(union);
			return ddv;	
		}
		
		return union;
	}
}
