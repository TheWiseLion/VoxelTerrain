package VoxelSystem.Hermite;

public class HermitePoint {
	public static final int AIR = -1;
	public float density;//hmmm skeptical.
	public int material;
	public HermitePoint(int material){
		this.material = material;
		density = 0f;
	}

	public HermitePoint(int material, float density){
		this.material = material;
		this.density = density;
	}

}
