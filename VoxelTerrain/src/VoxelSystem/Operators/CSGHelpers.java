package VoxelSystem.Operators;

import VoxelSystem.VoxelData.HermiteEdge;
import VoxelSystem.VoxelData.VoxelExtractor;

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
		public UnionExtactor(VoxelExtractor d1, VoxelExtractor d2,boolean overWrite) {
			super(d1, d2);
			this.overWrite = overWrite;
		}

		@Override
		public BoundingBox getBoundingBox() {
			return CSGHelpers.getUnion(this.bb1,this.bb2);
		}

		private int getType(int t1, int t2, boolean overwrite){
			if(overwrite){
				if(t2 != -1 ){
					return t2;
				}else{
					return t1;
				}
			}else{
				if(t1 != -1){
					return t1;
				}else{
					return t2;
				}
			}
			
		}

		private HermiteEdge commonCase(HermiteEdge e1 , HermiteEdge e2){
			//If both have an active edge pick the one that MAXIMIZES
			//the volume
			int t1 = getType(e1.t1,e2.t1,overWrite);
			int t2 = getType(e1.t2,e2.t2,overWrite);
			HermiteEdge f = e1;
			if (overWrite) {
				if(e2.intersection != null){
					f = e2;
				}
			}else{
				if(e1.intersection == null){
					f = e2;
				}
			}
			f.t1 = t1;
			f.t2 = t2;
			return f;
		}
		
		@Override
		public HermiteEdge getEdgeIntersection(Vector3f p1, Vector3f p2) {
			HermiteEdge e1 = super.validate(this.v1.getEdge(p1, p2));
			HermiteEdge e2 = super.validate(this.v2.getEdge(p1, p2));
			return commonCase(e1,e2);
		}

		@Override
		public HermiteEdge getEdgeV1(Vector3f p1, Vector3f p2) {
			return v1.getEdge(p1, p2);
		}

		@Override
		public HermiteEdge getEdgeV2(Vector3f p1, Vector3f p2) {
			return v2.getEdge(p1, p2);
		}

		@Override
		public HermiteEdge getEdgeIntersectionInconsistant(HermiteEdge e1, HermiteEdge e2, Vector3f p1, Vector3f p2) {
			//p1 in intersection, p2 isnt. e1 is volume 1.
			return commonCase(e1,e2);
		}

		@Override
		public HermiteEdge getEdgeInconsistant(HermiteEdge e1, HermiteEdge e2,Vector3f p1, Vector3f p2) {
			return commonCase(e1,e2);
		}

		@Override
		public HermiteEdge getInconsistantComplete(HermiteEdge e1,HermiteEdge e2, Vector3f p1, Vector3f p2) {
			return commonCase(e1,e2);
		}		
	}
	
	public static class DifferenceExtactor extends OperatorBase{

		public DifferenceExtactor(VoxelExtractor d1, VoxelExtractor d2) {
			super(d1, d2);
		}

		@Override
		public BoundingBox getBoundingBox() {
			return this.bb1; //Not necessarily correct... 
		}
		
		private int getType(int t1, int t2){
			if(t2 != -1){
				return -1;
			}else{
				return t1;
			}
		}
		
		private HermiteEdge commonCase(HermiteEdge e1, HermiteEdge e2){
			int t1 = getType(e1.t1, e2.t1);
			int t2 = getType(e1.t2, e2.t2);
			HermiteEdge f = e1;
			
			if(t1 == t2 && t1 == -1){
				return new HermiteEdge(t1,t2);
			}else if(e2.intersection != null){
				
				if(e1.intersection != null){ //The interesting case:
					f = e1;
					boolean p1Inside = (t1!=-1);//is the point p1 inside?
					if(p1Inside){ //p1 inside pick smallest 
						if(e2.intersection < e1.intersection){
							f = e2;
						}
					}else{ //p1 outside pick largest
						if(e2.intersection > e1.intersection){
							f = e1;
						}
					}	
				}else{
					f = e2; 
				}
			}
			
			f.t1 = t1;
			f.t2 = t2;
			return f;
		}
		
		@Override
		public HermiteEdge getEdgeIntersection(Vector3f p1, Vector3f p2) { 
			HermiteEdge e1 = super.validate(this.v1.getEdge(p1, p2));
			HermiteEdge e2 = super.validate(this.v2.getEdge(p1, p2));
			return commonCase(e1, e2);
		}

		@Override
		public HermiteEdge getEdgeV1(Vector3f p1, Vector3f p2) {
			// TODO Auto-generated method stub
			return v1.getEdge(p1, p2);
		}

		@Override
		public HermiteEdge getEdgeV2(Vector3f p1, Vector3f p2) {
			return new HermiteEdge(-1,-1);
		}

		@Override
		public HermiteEdge getEdgeIntersectionInconsistant(HermiteEdge e1,HermiteEdge e2, Vector3f p1, Vector3f p2) {
			return commonCase(e1, e2);
		}

		@Override
		public HermiteEdge getEdgeInconsistant(HermiteEdge e1, HermiteEdge e2, Vector3f p1, Vector3f p2) {
			return commonCase(e1, e2);
		}

		@Override
		public HermiteEdge getInconsistantComplete(HermiteEdge e1,HermiteEdge e2, Vector3f p1, Vector3f p2) {
			return commonCase(e1, e2);
		}
		
	}
	
	public static class PaintOperator extends OperatorBase{
		boolean overWrite;
		public PaintOperator(VoxelExtractor d1, VoxelExtractor d2) {
			super(d1, d2);
		}

		@Override
		public BoundingBox getBoundingBox() {
			return this.bb1;
		}

		private int getMaterial(int p1, int p2){
			if(p2 != -1 && p1 != -1){
				return p2;
			}else{
				return p1;
			}
		}
		
		private HermiteEdge commonCase(HermiteEdge e1, HermiteEdge e2){
			int new1 = getMaterial(e1.t1,e2.t1);
			int new2 = getMaterial(e1.t2,e2.t2);
			HermiteEdge f = e1;
			
			if(new1 != -1 && new2 != -1 && new2!=new1){
				 // there is a new material change and e2 must be the reason:
				if(new1 != e1.t1 || new2 != e1.t2){
					f = e2; 
				}
			}
			
			f.t1=new1;
			f.t2=new2;
			return f;
		}
		
		@Override
		public HermiteEdge getEdgeIntersection(Vector3f p1, Vector3f p2){
			HermiteEdge e1 = this.v1.getEdge(p1, p2);
			HermiteEdge e2 = this.v2.getEdge(p1, p2);
			return commonCase(e1,e2);
		}

		@Override
		public HermiteEdge getEdgeV1(Vector3f p1, Vector3f p2) {
			return v1.getEdge(p1, p2);
		}

		@Override
		public HermiteEdge getEdgeV2(Vector3f p1, Vector3f p2) {
			return new HermiteEdge(-1,-1);
		}

		@Override
		public HermiteEdge getEdgeIntersectionInconsistant(HermiteEdge e1, HermiteEdge e2, Vector3f p1, Vector3f p2) {
			
			return commonCase(e1,e2);
		}

		@Override
		public HermiteEdge getEdgeInconsistant(HermiteEdge e1, HermiteEdge e2, Vector3f p1, Vector3f p2) {
			
			return commonCase(e1,e2);
		}

		@Override
		public HermiteEdge getInconsistantComplete(HermiteEdge e1,HermiteEdge e2, Vector3f p1, Vector3f p2) {
			return commonCase(e1,e2);
		}

		
		
	}

	

}
