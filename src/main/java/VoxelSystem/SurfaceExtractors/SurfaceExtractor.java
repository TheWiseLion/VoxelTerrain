package VoxelSystem.SurfaceExtractors;


import java.util.List;

import VoxelSystem.MeshBuilding.SurfacePoint;
import VoxelSystem.VoxelData.VoxelGrid;

public interface SurfaceExtractor {
	/***
	 * Extracts the surface in the bounding volume, Described by the density and type volumes.
	 */
	public List<SurfacePoint> extractSurface(VoxelGrid hg);
	
	
}
