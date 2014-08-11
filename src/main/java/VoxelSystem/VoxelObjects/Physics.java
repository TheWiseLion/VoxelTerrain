package voxelsystem.voxelobjects;




import com.jme3.math.Vector3f;

/**
 * Bunch of static classes to do DDA on voxel field.
 * 
 * 
 * @author wiselion
 */
public class Physics {
    public static class HitData{
		public Vector3f impact;
		public float length;
	}
	
	public static HitData collideRay(PagingVoxelObject world, Vector3f source, Vector3f ray, float maxDist,float step){
		HitData hd = new HitData();
		
		Vector3f rayNormal = new Vector3f(ray);
		rayNormal.normalize();
		
		Vector3f map = new Vector3f((float)Math.floor(source.x),
                        (float) Math.floor(source.y),(float) Math.floor(source.z));
		
		float sideX, sideY, sideZ;
		float stepX, stepY, stepZ;
		
		float deltaX = Math.abs((step / rayNormal.x));
		float deltaY = Math.abs((step / rayNormal.y));
		float deltaZ = Math.abs((step / rayNormal.z));
		if(rayNormal.x < 0){
			stepX = -step; 
			sideX = (source.x - map.x) * deltaX;
		}else{
			stepX = step; 
			sideX = (map.x + step - source.x) * deltaX;
		}
		
		if(rayNormal.y < 0){
			stepY = -step;
			sideY = (source.y - map.y) * deltaY;
		}else{
			stepY = step; 
			sideY = (map.y + step - source.y) * deltaY;
		}
		
		if(rayNormal.z < 0){
			stepZ = -step; 
			sideZ = (source.z - map.z) * deltaZ;
		}else{
			stepZ = step; 
			sideZ = (map.z + step - source.z) * deltaZ;
		}
		
		float dist = 0;
		do{
			int t = world.getType(map.x, map.y, map.z);
//			System.out.println("Q: "+map+" "+t + " "+dist);
			if(t != -1){
				hd.impact = line(source, rayNormal, dist);
				hd.length = dist;
				return hd;
            }
			if(sideX < sideY){
				if(sideX < sideZ){//do X
					dist = sideX;
					sideX += deltaX;
					map.x += stepX;
					
				}else{//do Z
					dist = sideZ;
					sideZ += deltaZ;
					map.z += stepZ;
					
				}
			}else{
				if(sideY < sideZ){//do Y
					dist = sideY;
					sideY += deltaY;
					map.y += stepY;
					
				}else{//do Z
					dist = sideZ;
					sideZ += deltaZ;
					map.z += stepZ;
					
				}
			}
//                        System.out.println("Map: "+map);
			
		} while(dist <= maxDist);
		
		hd.impact = line(source, rayNormal, maxDist);
		hd.length = maxDist;
		
		return hd;
	}
        
        
        public static float intersect(Vector3f rayOrigin, Vector3f rayVector, Vector3f normal, Vector3f origin){
                float eq = -(normal.x*origin.x + normal.y*origin.y + normal.z*origin.z);
		float numer = rayOrigin.dot(normal) + eq;
		float denom = rayVector.dot(normal);
		return -(numer / denom);
	}
        
        private static Vector3f line(Vector3f p, Vector3f v, float t){
		Vector3f d = new Vector3f(v);
		d= d.scaleAdd(t,p);
		return d;
	}
        
        
      	public static float signedDistanceTo(Vector3f p,Vector3f origin,Vector3f normal){
            float eq = -(normal.x*origin.x + normal.y*origin.y + normal.z*origin.z);
		return (normal.x * p.x) + (normal.y * p.y) + (normal.z * p.z) + eq;
	}
}
