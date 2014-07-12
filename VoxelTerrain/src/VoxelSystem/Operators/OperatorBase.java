package VoxelSystem.Operators;

import VoxelSystem.Hermite.HermiteEdge;
import VoxelSystem.Hermite.HermiteExtractor;
import VoxelSystem.Hermite.HermitePoint;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

/***
 * The range of DensityVolume and HermiteExtractors are often bounded and
 * thus aren't able to be queried outside their bounding boxes. To make the writing of operators simpler
 * I've constructed this class to deal with most of the issues with finding intersecting and union bounds.
 * 
 */
public abstract class OperatorBase extends HermiteBase{
	protected HermiteExtractor he1;
	protected HermiteExtractor he2;
	protected BoundingBox bb1;
	protected BoundingBox bb2;
	protected boolean intersect;
	protected BoundingBox intersection;
	protected boolean union;
	protected boolean only1;
	protected boolean only2;
	
	public OperatorBase(HermiteExtractor d1,HermiteExtractor d2, boolean union){
		bb1 = d1.getBoundingBox();
		bb2 = d2.getBoundingBox();
		
		this.he1 = d1;
		this.he2 = d2;
		this.union = union;
		
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
	
	
	public abstract HermitePoint getPointIntersection(Vector3f p);
	public abstract HermiteEdge getEdgeIntersection(Vector3f p1,Vector3f p2);
	
	@Override
	public HermiteEdge getEdge(Vector3f p1, Vector3f p2) {
		//This is the tricky case...
		
//		if(intersect){
//			boolean b1 = inVolume(p1,intersection);
//			boolean b2 = inVolume(p2,intersection);
//			//p1 and p2 are both in intersection
//			if(b1 && b2){
				return getEdgeIntersection(p1,p2);
//			}else if(b1 != b2){//[!!CASE UNDEFINED!!] p1 is in intersection and p2 isn't [!!CASE UNDEFINED!!]
//				HermitePoint hp1 = getPoint(p1);
//				HermitePoint hp2 = getPoint(p2);
//				
//				if(hp1.material != hp2.material){
//					throw new RuntimeException("Case undefined");
////					Vector3f e = VoxelSystemTables.getIntersection(p1, p2, hp1.density,hp2.density);
////					Vector3f n = new Vector3f();
////					HermiteEdge he = new HermiteEdge(e, n);
////					return he;
//				}else{
//					return null;
//				}
//				//				throw new RuntimeException("Case undefined");
//			}
//				
//		}
//		
//		if(union){
//			boolean b1 = inVolume(p1,bb1);
//			boolean b2 = inVolume(p2,bb1);
//			
//			boolean b3 = inVolume(p1,bb2);
//			boolean b4 = inVolume(p2,bb2);
//			
//			if(b1 && b2){ //p1 and p2 are both in one bounds
//				return he1.getEdge(p1,p2);
//			}else if(b3 && b4){//p1 and p2 are both in one bounds
//				return he2.getEdge(p1,p2);
//			}else if(b1 && b4 || b2 && b3){//[!!CASE UNDEFINED!!] p1 is in one bound and p2 is the other [!!CASE UNDEFINED!!]
//				throw new RuntimeException("Case undefined");
//			}else{ //p1 and p2 are both in no bounds
//				return null;
//			}
//		}else if(only1){
//			boolean b1 = inVolume(p1,bb1);
//			boolean b2 = inVolume(p2,bb1);
//			if(b1 && b2){ //p1 and p2 are both in one bounds
//				return he1.getEdge(p1,p2);
//			}
//		}else if(only2){
//			boolean b3 = inVolume(p1,bb2);
//			boolean b4 = inVolume(p2,bb2);
//			if(b3 && b4){//p1 and p2 are both in one bounds
//				return he2.getEdge(p1,p2);
//			}
//		}
//		
//		return null;
	}

	@Override
	public HermitePoint getPoint(Vector3f p) {
		//if in intersection:
//		if(intersect && inVolume(p,intersection)){
			return getPointIntersection(p);
//		}
		
//		if(union){
//			if(inVolume(p,bb1)){//else if in either d1 or d2
//				return he1.getPoint(p);
//			}else if (inVolume(p, bb2)){//else if in either d1 or d2
//				return he2.getPoint(p);
//			}
//		}else if(only1){
//			if(inVolume(p,bb1)){//else if in either d1 or d2
//				return he1.getPoint(p);
//			}
//		}else if(only2){
//			if (inVolume(p, bb2)){//else if in either d1 or d2
//				return he2.getPoint(p);
//			}
//		}
		
		//else set to air
//		return new HermitePoint(HermitePoint.AIR);
	}

	protected boolean inVolume(Vector3f p,BoundingBox bb){
		if(bb == null){
			return true;
		}else if(bb.contains(p)){
			return true;
		}else{
			return false;
		}
	}
	
}
