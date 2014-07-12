package VoxelSystem.Operators;

import VoxelSystem.Hermite.HermiteEdge;
import VoxelSystem.Hermite.HermiteExtractor;
import VoxelSystem.Hermite.HermitePoint;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class CSGHelpers {
	public final static float CLAMPVAL=1.0f;
	
	/***
	 * Can be infinite -> returns null
	 * Can be finite -> returns true ['output' will be modified]
	 * May not intersect -> returns false
	 */
	public static final Boolean getIntersection(BoundingBox bv1,BoundingBox bv2,BoundingBox output){
		Vector3f min = new Vector3f();
		Vector3f max = new Vector3f();
		boolean hasVolume1 = bv1 != null;
		boolean hasVolume2 = bv2 != null;
		
		if(hasVolume1 && hasVolume2){
			if(!bv1.intersects(bv2)){
				return false;
			}
			min = bv1.getMin(min);
	    	max = bv1.getMax(max);
	    	min.maxLocal(bv2.getMin(new Vector3f()));
	    	max.minLocal(bv2.getMax(new Vector3f()));
	    	output.setMinMax(min, max);
	    	return true;
		}else if(hasVolume1){
			output.setMinMax(bv1.getMin(min), bv1.getMax(max));
			return true;
		}else if(hasVolume2){
			output.setMinMax(bv2.getMin(min), bv2.getMax(max));
			return true;
		}else{
			return null;
		}
	}
	
	/***
	 * Returns the minimal union between 2 bounding volumes.
	 */
	public static final BoundingBox getUnion(BoundingBox bv1,BoundingBox bv2){
		if(bv1 == null || bv2 == null){
			return null;
		}
		Vector3f min = bv1.getMin(new Vector3f());
    	Vector3f max = bv1.getMax(new Vector3f());
    	min.minLocal(bv2.getMin(new Vector3f()));
    	max.maxLocal(bv2.getMax(new Vector3f()));
    	return new BoundingBox(min,max);
	}
	
	public static float clamp(float val) {
	    return Math.max(-CLAMPVAL, Math.min(CLAMPVAL, val));
	}
	
	public static float clamp(float val,float clampV) {
	    return Math.max(-clampV, Math.min(clampV, val));
	}
	
	public static class UnionExtactor extends OperatorBase{
		boolean overWrite;
		public UnionExtactor(HermiteExtractor d1, HermiteExtractor d2,boolean overWrite) {
			super(d1, d2, true);
			this.overWrite = overWrite;
		}

		@Override
		public BoundingBox getBoundingBox() {
			return CSGHelpers.getUnion(this.bb1,this.bb2);
		}

		@Override
		public HermitePoint getPointIntersection(Vector3f p) {
			HermitePoint hp1 = this.he1.getPoint(p);
			HermitePoint hp2 = this.he2.getPoint(p);
			
			if(overWrite){
				if(hp2.material != -1){
					return hp2;
				}else{
					return hp1;
				}
			}else{
				if(hp1.material != -1){
					return hp1;
				}else{
					return hp2;
				}
			}
		}

		@Override
		public HermiteEdge getEdgeIntersection(Vector3f p1, Vector3f p2) {
			HermiteEdge e1 = this.he1.getEdge(p1, p2);
			HermiteEdge e2 = this.he2.getEdge(p1, p2);
			
			//If both have an active edge pick the one that MAXIMIZES
			//the volume
			if(e1 != null && e2 != null){
				float f = getLerp(p1,p2,e1.intersection);
				float f1 = getLerp(p1,p2,e2.intersection);
				if(f > f1){
					return e1;
				}else{
					return e2;
				}
			}else{
				if(overWrite){
					if(e2 != null){
						return e2;
					}else{
						return e1;
					}
				}else{
					if(e1 != null){
						return e1;
					}else{
						return e2;
					}
				}
			}
		}
		
		/**
		 * returns percent from v1 to v2
		 */
		private static float getLerp(Vector3f v1, Vector3f v2, Vector3f mp){
			Vector3f l = v1.subtract(v2);
			Vector3f n = v2.subtract(mp);
			return 1.0f - (Math.abs(n.x+n.y+n.z)/Math.abs(l.x+l.y+l.z));
		}
		
		
		
	}
	
	public static class DifferenceExtactor extends OperatorBase{

		public DifferenceExtactor(HermiteExtractor d1, HermiteExtractor d2) {
			super(d1, d2,false);
			this.only1 = true;
		}

		@Override
		public BoundingBox getBoundingBox() {
			return this.bb1;
		}

		@Override
		public HermitePoint getPointIntersection(Vector3f p) {
			HermitePoint hp1 = this.he1.getPoint(p);
			HermitePoint hp2 = this.he2.getPoint(p);
			
			if(hp2.material != -1){
				return new HermitePoint(-1);
			}else{
				return hp1;
			}
		}
		
		HermiteEdge getClosest(Vector3f point,HermiteEdge E1, HermiteEdge E2){
			Vector3f worker = new Vector3f(E1.intersection);
			float f = worker.subtractLocal(point).lengthSquared();
			if(worker.set(E2.intersection).subtractLocal(point).lengthSquared() < f){
				return E2;
			}else{
				return E1;
			}
		}
		
		
		@Override
		public HermiteEdge getEdgeIntersection(Vector3f p1, Vector3f p2) { //TODO: reduce complexity.
			HermiteEdge e1 = this.he1.getEdge(p1, p2);
			HermiteEdge e2 = this.he2.getEdge(p1, p2);
			
			if(e2 != null){
				if(e1 == null){
					return e2;
				}
				
				HermitePoint actual1 = this.he1.getPoint(p1);
				HermitePoint actual2 = this.he1.getPoint(p2);
				Vector3f p;
				if(actual1.material == -1){
					p = p2;
				}else{
					p = p1;
				}
				return getClosest(p,e1,e2);
//				return e2;
			}else{
				return e1;
			}
			
			//We only care about THE SURFACE of he2
			//So check if its a surface point.
//			HermitePoint hp1 = this.he2.getPoint(p1);
//			HermitePoint hp2 = this.he2.getPoint(p2);
//			
//			
//			HermitePoint hp3 = this.he1.getPoint(p1);
//			HermitePoint hp4 = this.he1.getPoint(p2);
//			
//			//Check early out. If he1 is empty so dont bother subtracting.
//			if(hp3.material == -1 && hp4.material == -1){ 
//				return null;
//			}else if(hp1.material ==  hp2.material){ 
//				if(hp1.material != -1){//is he2 surface? We only need to consider cases where its the surface.
//					return null;
//				}else{ //he2 is empty so nothing to subtract
//					return e1;
//				}
//			}
//			
//			HermitePoint actual1 = getPointIntersection(p1);
//			HermitePoint actual2 = getPointIntersection(p2);
//			
//			if(actual1.material == actual2.material){
//				return null;
//			}else if(actual1.material == -1 || actual2.material == -1){
//				
//				
//				/**
//				 * The interesting case:
//				 * "intersection normals in B  may only override the data in A  if the resulting volume data represents a smaller non-air volume"
//				 * "This is the case if the intersection in B  lies closer to the non-air material in A  than the corresponding intersection in A"
//				 */
//				//How do we know which is closer to the surface?
//				//look at where "inside" and "outside" are.
//				if(e1 == null){
//					return e2;
//				}
//				
//				if(actual1.material == -1){
//					//Get point closer to p2 (less volume)
//					return getClosest(p1,e1,e2);
//				}else if(actual2.material == -1){
//					//Get point closer to p2 (less volume)
//					return getClosest(p2,e1,e2);
//				}else{
//					throw new RuntimeException("??");
//				}
//				
//			}else{
//				return e1;
//			}
			
		}
		
	}
	
	public static class PaintOperator extends OperatorBase{
		boolean overWrite;
		public PaintOperator(HermiteExtractor d1, HermiteExtractor d2) {
			super(d1, d2, true);
		}

		@Override
		public BoundingBox getBoundingBox() {
			return this.bb1;
		}

		@Override
		public HermitePoint getPointIntersection(Vector3f p) {
			HermitePoint hp1 = this.he1.getPoint(p);
			HermitePoint hp2 = this.he2.getPoint(p);
			
			if(hp1.material != -1 && hp2.material != -1){
					return hp2;
				
			}else{
				return hp1;
			}
			
		}

		@Override
		public HermiteEdge getEdgeIntersection(Vector3f p1, Vector3f p2) {
			HermiteEdge e1 = this.he1.getEdge(p1, p2);
			HermiteEdge e2 = this.he2.getEdge(p1, p2);
			
			//If there is a material change:
			int new1 = getPointIntersection(p1).material;
			int new2 = getPointIntersection(p2).material;
			
			int original1 = this.he1.getPoint(p1).material;
			int original2 = this.he1.getPoint(p2).material;
			
			//if its a material-material boundary:
			if(new1 != -1 && new2 != -1 && new2!=new1){
				if(new1 != original1 || new2 != original2){ // there is a new material change
					return e2;
				}
			}
			
			return e1;
		}
		
	}
}
