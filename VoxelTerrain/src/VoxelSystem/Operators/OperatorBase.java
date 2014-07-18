package VoxelSystem.Operators;

import VoxelSystem.Hermite.ExtractorBase;
import VoxelSystem.Hermite.HermiteEdge;
import VoxelSystem.Hermite.VoxelExtractor;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/***
 * The range of DensityVolume and HermiteExtractors are often bounded and
 * thus aren't able to be queried outside their bounding boxes. To make the writing of operators simpler
 * I've constructed this class to deal with the breaking down of the different edge cases.
 * 
 * It breaks down all 5 basic cases for any operation between 2 volumes.
 */
public abstract class OperatorBase extends ExtractorBase{
	protected VoxelExtractor v1;
	protected VoxelExtractor v2;
	protected BoundingBox bb1;
	protected BoundingBox bb2;
	protected boolean intersect;
	protected BoundingBox intersection;
	protected boolean only1;
	protected boolean only2;
	
	public OperatorBase(VoxelExtractor d1,VoxelExtractor d2){
		bb1 = d1.getBoundingBox();
		bb2 = d2.getBoundingBox();
		
		this.v1 = d1;
		this.v2 = d2;
		
		intersection = new BoundingBox();
		Boolean b = CSGHelpers.getIntersection(bb1, bb2, intersection);
		if(b == null){
			intersect = true;
			intersection = null;
		}else if(b == false){
			intersect = false;
		}else{
			intersect = true;
		}
	}
	
	
	/**
	 * This is called when both points are in the intersection of both volumes.
	 */
	public abstract HermiteEdge getEdgeIntersection(Vector3f p1,Vector3f p2);
	
	/**
	 * Strictly inside volume 1
	 */
	public abstract HermiteEdge getEdgeV1(Vector3f p1,Vector3f p2);
	
	/**
	 * Strictly inside volume 2
	 */
	public abstract HermiteEdge getEdgeV2(Vector3f p1,Vector3f p2);
	
	
	/***
	 * This is called when only p1 falls inside the intersection of the volumes but
	 * p2 does not.
	 * e1 is the hermite edge of volume 1 (between p1 and p2)
	 * e2 is the hermite edge of volume 2 (between p1 and p2)
	 * @return
	 */
	public abstract HermiteEdge getEdgeIntersectionInconsistant(HermiteEdge e1,HermiteEdge e2, Vector3f p1, Vector3f p2);
	
	/***
	 * The final edge case: 
	 * p1 is in volume 1
	 * p2 is in volume 2
	 * e1 is the hermite edge of volume 1 (between p1 and p2)
	 * e2 is the hermite edge of volume 2 (between p1 and p2)
	 */
	public abstract HermiteEdge getEdgeInconsistant(HermiteEdge e1,HermiteEdge e2, Vector3f p1, Vector3f p2);
	
	@Override
	public HermiteEdge getEdge(Vector3f p1, Vector3f p2) {
		//If at least one point is in volume (ELSE ERROR)
		//TODO: At some point move this to validator.
		boolean i1 = inVolume(p1, this.getBoundingBox());
		boolean i2 = inVolume(p2,this.getBoundingBox());
		
		if(!i1 && !i2){
			throw new RuntimeException("");
		}
		
		//TODO: check early out cases (only1/only2 etc)
		
		
		boolean b1 = inVolume(p1,intersection);
		boolean b2 = inVolume(p2,intersection);
		
		//If p1 and p2 are in intersection:
		if(b1 && b2){
			return getEdgeIntersection(p1,p2);
		
		}else if(b1 != b2){ //one point is in intersection
			//Grab both edges let operator decide. Be sure to be consistent (pt1 in side as first param)
			if(b1 == true){
				HermiteEdge e1 = validate(v1.getEdge(p1, p2));
				HermiteEdge e2 = validate(v2.getEdge(p1, p2));
				return getEdgeIntersectionInconsistant(e1, e2, p1, p2);
			}else{
				HermiteEdge e1 = validate(v1.getEdge(p2, p1));
				HermiteEdge e2 = validate(v2.getEdge(p2, p1));
				return swap(getEdgeIntersectionInconsistant(e1, e2, p2, p1)); //swapped to maintain consistency
			}
			
		}else{ //The other 3 cases:
			boolean bv11 = inVolume(p1, bb1);
			boolean bv21 = inVolume(p2, bb1);
			
			boolean bv12 = inVolume(p1, bb2);
			boolean bv22 = inVolume(p2, bb2);
			
			if(bv11 && bv22 || bv12 && bv21){ //if one point in each
				if(bv11 && bv22){ //p1 in v1, p2 in v2
					HermiteEdge e1 = validate(v1.getEdge(p1, p2));
					HermiteEdge e2 = validate(v2.getEdge(p1, p2));
					return getEdgeInconsistant(e1, e2, p1, p2);
				}else{ //p1 in v2, p2 in v1
					HermiteEdge e1 = validate(v1.getEdge(p2, p1));
					HermiteEdge e2 = validate(v2.getEdge(p2, p2));
					return swap(getEdgeInconsistant(e1, e2, p2, p1));
				}
			}else if(bv11 || bv21){// if volume 1 only
					return getEdgeV1(p1,p2);
			}else if(bv12 || bv22){ //if volume 2 only
					return getEdgeV2(p1,p2);
			}else{
				return null;
//				throw new RuntimeException("I dont understand.");
			}
		}
		
	}

	/***
	 * flips the edge data
	 * (t1 = t2, t1= t2)
	 * @return
	 */
	protected HermiteEdge swap(HermiteEdge he){
		if(he == null){
			return null;
		}
		int tmp = he.t1;
		he.t1 = he.t2;
		he.t2 = tmp;
		if(he.intersection !=null){
			he.intersection = (1f - he.intersection);
		}
		
		return he;
	}
	
	protected HermiteEdge validate(HermiteEdge he){
		if(he ==null){
			return new HermiteEdge();
		}
		return he;
	}
	
	protected boolean inVolume(Vector3f p,BoundingBox bb){
		if(bb == null){
			return true;
		}else if(contains(bb,p)){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean contains(BoundingBox bb, Vector3f point) {
		Vector3f center = bb.getCenter();
		return FastMath.abs(center.x - point.x) <= bb.getXExtent()
				&& FastMath.abs(center.y - point.y) <= bb.getYExtent()
				&& FastMath.abs(center.z - point.z) <= bb.getZExtent();
	}

}
