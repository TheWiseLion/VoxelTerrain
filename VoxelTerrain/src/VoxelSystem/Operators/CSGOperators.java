package VoxelSystem.Operators;

import VoxelSystem.Hermite.HermiteExtractor;
import VoxelSystem.Operators.CSGHelpers.DifferenceExtactor;
import VoxelSystem.Operators.CSGHelpers.UnionExtactor;



/***
 * Something that does an operation on a set 
 * @author wiselion
 *
 */
public class CSGOperators {
	
	
	/**
	 * Takes n operators. As defined by the specific implementation.
	 * 
	 * In general the following rules are to be strictly followed:
	 * - finite + non-finite = non-finite 
	 * - finite + finite = finite (volume of first)
	 * - non-finite + non-finite = non-finite
	 */

	

//	public DensityVolume intersection(DensityVolume ... volumes) {
//		if(volumes.length < 2){
//			throw new IllegalArgumentException("must be greater than or equal to 2 arguements");
//		}
//		
//		DensityVolumeIntersection intersection =new DensityVolumeIntersection(volumes[0],volumes[1]);
//		for(int i = 2; i<volumes.length;i++){
//			intersection = new DensityVolumeIntersection(intersection,volumes[i]);
//		}
//		
//		return intersection;
//	}
	
	
	public static HermiteExtractor union(boolean overwrite,HermiteExtractor ... extractors){
		if(extractors.length<2){
			throw new IllegalArgumentException("must be greater than or equal to 2 arguements");
		}
		
		UnionExtactor union =new UnionExtactor(extractors[0],extractors[1],overwrite);
		for(int i = 2; i<extractors.length;i++){
			union = new UnionExtactor(union,extractors[i],overwrite);
		}
		
		return union;
	}
	
	public static HermiteExtractor difference(HermiteExtractor ... extractors){
		if(extractors.length<2){
			throw new IllegalArgumentException("must be greater than or equal to 2 arguements");
		}
		
		DifferenceExtactor diff =new DifferenceExtactor(extractors[0],extractors[1]);
		for(int i = 2; i<extractors.length;i++){
			diff = new DifferenceExtactor(diff,extractors[i]);
		}
		
		return diff;
	}
}
