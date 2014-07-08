package VoxelSystem.SurfaceExtractors;

import java.util.List;

import VoxelSystem.Hermite.HermiteExtractor;
import VoxelSystem.MeshBuilding.SurfacePoint;

import com.jme3.bounding.BoundingBox;

public interface SurfaceExtractor {
	/***
	 * Extracts the surface in the bounding volume, Described by the density and type volumes.
	 */
	public List<SurfacePoint> extractSurface(HermiteExtractor hermiteData,BoundingBox bb,float resolution); 
}
