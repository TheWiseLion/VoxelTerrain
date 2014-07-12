package VoxelSystem.Operators;

import VoxelSystem.DensityVolumes.DensityVolume;
import VoxelSystem.Hermite.HermiteExtractor;
import VoxelSystem.Operators.CSGHelpers.DifferenceExtactor;
import VoxelSystem.Operators.CSGHelpers.PaintOperator;
import VoxelSystem.Operators.CSGHelpers.UnionExtactor;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;



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
	public static HermiteExtractor paint(HermiteExtractor ... extractors){
		if(extractors.length<2){
			throw new IllegalArgumentException("must be greater than or equal to 2 arguements");
		}
		
		PaintOperator painted =new PaintOperator(extractors[0],extractors[1]);
		for(int i = 2; i<extractors.length;i++){
			painted = new PaintOperator(painted,extractors[i]);
		}
		
		return painted;
	}
	
	
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
	
	public static DensityVolume rotate(DensityVolume df, Vector3f center, Quaternion q){
		final Vector3f c = center;
		final Matrix3f r = q.toRotationMatrix();
		final DensityVolume d = df;
		
		return new DensityVolume() {
			
			private Vector3f rotatePoint(float x, float y, float z){
				Vector3f rotatedPoint = c.subtract(x,y,z);
				return r.mult(rotatedPoint);
			}
			
			@Override
			public int getType(float x, float y, float z) {
				Vector3f r = rotatePoint(x,y,z);
				return d.getType(r.x,r.y,r.z);
			}
			
			@Override
			public boolean isDiscrete() {
				return d.isDiscrete();
			}
			
			@Override
			public Vector3f getFieldDirection(float x, float y, float z) {
				Vector3f p = rotatePoint(x,y,z);
				System.out.println(r.mult(d.getFieldDirection(p.x,p.y,p.z)) + " vs "+r.mult(d.getFieldDirection(x,y,z))+" vs "+d.getFieldDirection(x,y,z));
				
//				if(d.getFieldDirection(x,y,z).equals(new Vector3f(0,0,0))){
//					System.out.print("entry");
//				}
				
				
				return new Vector3f(0,0,0);//;
			}
			
			@Override
			public BoundingBox getEffectiveVolume() {
				return d.getEffectiveVolume(); //Wrong: ?needs to be expanded?
			}
			
			@Override
			public float getDensity(float x, float y, float z) {
				Vector3f r = rotatePoint(x,y,z);
				return d.getDensity(r.x,r.y,r.z);
			}
		};
	}
	
	
	
}
