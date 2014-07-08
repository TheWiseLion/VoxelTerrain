package VoxelSystem.Hermite;

import com.jme3.math.Vector3f;

public class HermiteUtils {
	public static Vector3f VertexInterp(Vector3f p1, Vector3f p2, float valp1,float valp2) {
//		float epsilon=0;
//		if (Math.abs(valp1) <= epsilon) //Very near zero
//			return (p1);
//		if (Math.abs(valp2) <= epsilon)
//			return (p2);
//		if (Math.abs(valp1 - valp2) <= epsilon)//
//			return (p1);
		
		float v1 = Math.abs(valp1);
	    float v2 = Math.abs(valp2);
		float v = v1 + v2;
		// mu is always between 1 and 0
		Vector3f p = new Vector3f();
		p.x = (v2 * p1.x + v1 * p2.x) / v;
		p.y = (v2 * p1.y + v1 * p2.y) / v;
		p.z = (v2 * p1.z + v1 * p2.z) / v;
		
		return (p);
	}
}
