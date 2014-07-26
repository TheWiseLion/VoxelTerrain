package VoxelSystem.Operators;

import VoxelSystem.Operators.CSGHelpers.DifferenceExtactor;
import VoxelSystem.Operators.CSGHelpers.PaintOperator;
import VoxelSystem.Operators.CSGHelpers.UnionExtactor;
import VoxelSystem.VoxelData.ExtractorBase;
import VoxelSystem.VoxelData.HermiteEdge;
import VoxelSystem.VoxelData.VoxelExtractor;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;



/***
 * Something that does an operation on a set 
 * @author wiselion
 *
 */
public class CSGOperators {
	
	
	/**
	 * Takes n operators. As defined by the specific implementation.
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
	
	public static VoxelExtractor paint(VoxelExtractor ... extractors){
		if(extractors.length<2){
			throw new IllegalArgumentException("must be greater than or equal to 2 arguements");
		}
		
		PaintOperator painted =new PaintOperator(extractors[0],extractors[1]);
		for(int i = 2; i<extractors.length;i++){
			painted = new PaintOperator(painted,extractors[i]);
		}
		
		return painted;
	}
	
	
	public static VoxelExtractor union(boolean overwrite,VoxelExtractor ... extractors){
		if(extractors.length<2){
			throw new IllegalArgumentException("must be greater than or equal to 2 arguements");
		}
		
		UnionExtactor union =new UnionExtactor(extractors[0],extractors[1],overwrite);
		for(int i = 2; i<extractors.length;i++){
			union = new UnionExtactor(union,extractors[i],overwrite);
		}
		
		return union;
	}
	
	public static VoxelExtractor difference(VoxelExtractor ... extractors){
		if(extractors.length<2){
			throw new IllegalArgumentException("must be greater than or equal to 2 arguements");
		}
		
		DifferenceExtactor diff =new DifferenceExtactor(extractors[0],extractors[1]);
		for(int i = 2; i<extractors.length;i++){
			diff = new DifferenceExtactor(diff,extractors[i]);
		}
		
		return diff;
	}
	
	public static VoxelExtractor makeCubed(final VoxelExtractor ve){
		return new ExtractorBase() {

			@Override
			public HermiteEdge getEdge(Vector3f p1, Vector3f p2) {
				HermiteEdge he = ve.getEdge(p1, p2);
				if(he != null && he.intersection != null){
//					he.normal = new Vector3f(0,0,0);
					if(p1.x != p2.x){
						he.normal = new Vector3f(1,0,0);
					}else if(p1.y != p2.y){
						he.normal = new Vector3f(0,1,0);
					}else{
						he.normal = new Vector3f(0,0,1);
					}
				}
				return he;
			}
			
			@Override
			public BoundingBox getBoundingBox() {
				return ve.getBoundingBox();
			}
		};
	}
	
	public static VoxelExtractor translate(final VoxelExtractor ve,final Vector3f t){
		final Vector3f r = new Vector3f(t);
		r.negateLocal();
		return new ExtractorBase() {

			@Override
			public HermiteEdge getEdge(Vector3f p1, Vector3f p2) {
				Vector3f t0 = new Vector3f(p1);t0.addLocal(r);
				Vector3f t1 = new Vector3f(p2);t1.addLocal(r);
				HermiteEdge he = ve.getEdge(t0,t1);
				return he;
			}
			
			@Override
			public BoundingBox getBoundingBox() {
				BoundingBox bb = ve.getBoundingBox();
				Vector3f min = bb.getMin(new Vector3f());
				Vector3f max = bb.getMax(new Vector3f());
				min.addLocal(t);
				max.addLocal(t);
				return new BoundingBox(min,max);
			}
		};
	}
	
	
	//////////////////////////////////////////////////////////
	////////////Perhaps my derpiest idea yet//////////////////
	//////////////////////////////////////////////////////////
	
	public static CSGOperator paint = new CSGOperator() {
		@Override
		public VoxelExtractor operate(VoxelExtractor... arguements) {
			return CSGOperators.paint(arguements);
		}
	};
	
	public static CSGOperator unionOverwrite = new CSGOperator() {
		@Override
		public VoxelExtractor operate(VoxelExtractor... arguements) {
			return CSGOperators.union(true,arguements);
		}
	};
	
	
	public static CSGOperator unionNoOverwrite = new CSGOperator() {
		@Override
		public VoxelExtractor operate(VoxelExtractor... arguements) {
			return CSGOperators.union(false,arguements);
		}
	};
	
	
	public static CSGOperator difference = new CSGOperator() {
		@Override
		public VoxelExtractor operate(VoxelExtractor... arguements) {
			return CSGOperators.difference(arguements);
		}
	};
	
	public static CSGOperator makeCubed = new CSGOperator() {
		@Override
		public VoxelExtractor operate(VoxelExtractor... arguements) {
			return CSGOperators.difference(arguements);
		}
	};
	
}
