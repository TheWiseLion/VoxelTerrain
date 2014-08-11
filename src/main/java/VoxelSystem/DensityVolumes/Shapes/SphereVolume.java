package VoxelSystem.DensityVolumes.Shapes;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

public class SphereVolume extends VolumeShape{
	private Vector3f center;
	private float radius;
	private BoundingBox bb;
	
	public SphereVolume(Vector3f center, float radius){
		Vector3f min = new Vector3f(center);
		Vector3f max = new Vector3f(center);
		radius = Math.abs(radius);
		min.subtractLocal(radius, radius, radius);
		max.subtractLocal(-radius, -radius, -radius);
		bb = new BoundingBox(min,max);
		this.radius = radius;
		this.center = center;
	}

	@Override
	public float getDensity(float x, float y, float z) {
		Vector3f d = center.subtract(x, y, z);
		return radius-d.length();
	}

	@Override
	public Vector3f getSurfaceNormal(float x, float y, float z) {
		return center.subtract(x,y, z).normalizeLocal();
	}

	@Override
	public BoundingBox getEffectiveVolume() {
		return bb;
	}

	@Override
	public boolean isOutside(float x, float y, float z) {
		float ox = (center.x-x)*(center.x-x);
		float oy = (center.y-y)*(center.y-y);
		float oz = (center.z-z)*(center.z-z);
		return radius*radius<(ox+oy+oz);
	}


}
