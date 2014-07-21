package VoxelSystem.DensityVolumes.Shapes;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class BoxVolume extends VolumeShape{
	private Vector3f center;
	private Vector3f extents;
	private BoundingBox bb;
	
	public BoxVolume(Vector3f center, float width, float height, float length){
		Vector3f min = new Vector3f(center);
		Vector3f max = new Vector3f(center);
		min.subtractLocal(width/2.0f, height/2.0f, length/2.0f);
		max.subtractLocal(-width/2.0f, -height/2.0f, -length/2.0f);
		
		
		bb = new BoundingBox(min,max);
		extents = new Vector3f(width,height,length);
		this.center = center;
	}
	
	@Override
	public float getDensity(float x, float y, float z)	 {
		//Get closest surface point
		Vector3f min =bb.getMin(new Vector3f());
		Vector3f max =bb.getMax(new Vector3f());
		
		Vector3f v = new Vector3f(x,y,z);
		float val = -1;
		if(!VolumeShape.contains(bb, v)){//outside box
			//Get nearest point on box:
			if (v.x < min.x){ 
				v.x = min.x;
			}else if(v.x > max.x){ 
				v.x = max.x;
			}
			
			if (v.y < min.y){ 
				v.y = min.y;
			}else if(v.y>max.y){ 
				v.y = max.y;
			}
			
			if (v.z < min.z){ 
				v.z = min.z;
			}else if(v.z>max.z){
				v.z = max.z;
			}
			val = -v.subtract(x,y,z).length();
		}else{
			//Get distance to closest face
			float d = Math.min(x-min.x,max.x-x);
			 d = Math.min(d,Math.min(y-min.y,max.y-y));
			 d = Math.min(d,Math.min(z-min.z,max.z-z));
			 val = d;
		}
		
		
	   //The difference to the closest point is the density
		return val;
	}
	
	
	
	
	@Override
	public Vector3f getSurfaceNormal(float x, float y, float z) {
		Vector3f min =bb.getMin(new Vector3f());
		Vector3f max =bb.getMax(new Vector3f());
		
		Vector3f v = new Vector3f(x,y,z);
//		if(!bb.contains(v)){//outside box
//			//Get nearest point on box:
//			if (v.x < min.x){ 
//				v.x = min.x;
//			}else if(v.x>max.x){ 
//				v.x = max.x;
//			}
//			
//			if (v.y < min.y){ 
//				v.y = min.y;
//			}else if(v.y>max.y){ 
//				v.y = max.y;
//			}
//			
//			if (v.z < min.z){ 
//				v.z = min.z;
//			}else if(v.z>max.z){
//				v.z = max.z;
//			}
//			return v.subtract(x,y,z).normalize();
//		}else{
			//Get distance to closest face
			Vector3f normal = new Vector3f(-1,0,0);
			float d = x-min.x;
			if(max.x-x<d){
				d= max.x-x;
				normal.set(1,0,0);
			}
			
			if(max.y-y<d){
				d= max.y-y;
				normal.set(0,1,0);
			}
			
			if(y-min.y<d){
				d= y-min.y;
				normal.set(0,-1,0);
			}
			
			if(max.z-z<d){
				d= max.z-z;
				normal.set(0,0,1);
			}
			if(z-min.z<d){
				d= z-min.z;
				normal.set(0,0,-1);
			}
			
			
			return normal;
//		}
		
	}

	@Override
	public BoundingBox getEffectiveVolume() {
		return bb;
	}


}