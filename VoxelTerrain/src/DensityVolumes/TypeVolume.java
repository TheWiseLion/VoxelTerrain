package DensityVolumes;

public interface TypeVolume {
	/**
	 * Retrieves a type at a specific integer grid corner. May be used for
	 * optimization reasons.
	 * TODO: link discrete volume
	 */
	public float getDensity(int x, int y, int z);

	/**
	 * Retrieves a type value from the specified point in this volume.
	 */
	public float getDensity(float x, float y, float z);
}
