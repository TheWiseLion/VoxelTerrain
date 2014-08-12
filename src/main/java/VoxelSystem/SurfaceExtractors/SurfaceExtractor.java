package voxelsystem.surfaceextractors;


import java.util.List;

import voxelsystem.meshbuilding.SurfacePoint;
import voxelsystem.voxeldata.VoxelGrid;

public interface SurfaceExtractor {
	/***
	 * Extracts the surface in the bounding volume, Described by the density and type volumes.
	 */
	public List<SurfacePoint> extractSurface(VoxelGrid hg);
	
	
}
