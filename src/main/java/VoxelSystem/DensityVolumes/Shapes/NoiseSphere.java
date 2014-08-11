package VoxelSystem.DensityVolumes.Shapes;

import VoxelSystem.VoxelNoise.SimplexNoise;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

public class NoiseSphere extends VolumeShape{
	private Vector3f center;
	private float radius;
	private BoundingBox bb;
	
	public NoiseSphere(Vector3f center, float radius){
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
		
		float ox = (center.x-x)*(center.x-x);
		float oy = (center.y-y)*(center.y-y);
		float oz = (center.z-z)*(center.z-z);
		if(radius*radius<(ox+oy+oz)){
			return -1;
		}
		
		int octaves = 1;
		float density = -.25f;
		float amplitude = 1.70f;
		
		
		float baseFreq = 12f;
		float FreqMultiplier = 4f;
		
		//"Mountain"  Low Frequency high octave
		for(int i = 0; i< octaves;i++){
			float localFreq = baseFreq + FreqMultiplier*i;
			density += SimplexNoise.noise(d.x/localFreq,d.y/localFreq,d.z/localFreq) * amplitude * (i+1);
		}
		
		
		return density;
	}

	@Override
	public Vector3f getSurfaceNormal(float x, float y, float z) {
		return new Vector3f(0,0,0);
	}

	@Override
	public BoundingBox getEffectiveVolume() {
		return bb;
	}

	@Override
	public boolean isOutside(float x, float y, float z) {
		return getDensity(x,y,z)<0;
	}
}
