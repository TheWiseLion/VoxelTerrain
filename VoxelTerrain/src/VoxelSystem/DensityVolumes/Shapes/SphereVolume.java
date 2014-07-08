package VoxelSystem.DensityVolumes.Shapes;

import com.jme3.bounding.BoundingBox;
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
		bb = null;//new BoundingBox(min,max);
		this.radius = radius;
		this.center = center;
	}
	
	@Override
	public float getDensity(float x, float y, float z) {
		Vector3f d = center.subtract(x, y, z);
		return radius-d.length();
	}

	@Override
	public Vector3f getFieldDirection(float x, float y, float z) {
		return center.subtract(x,y, z).normalizeLocal();
	}

	@Override
	public BoundingBox getEffectiveVolume() {
		return bb;
	}

	@Override
	public boolean isDiscrete() {
		return false;
	}

}
