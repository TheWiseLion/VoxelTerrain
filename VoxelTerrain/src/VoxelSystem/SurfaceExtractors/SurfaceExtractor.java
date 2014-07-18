package VoxelSystem.SurfaceExtractors;

import idea.VoxelGrid;

import java.util.List;

import VoxelSystem.Hermite.VoxelExtractor;
import VoxelSystem.MeshBuilding.SurfacePoint;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public interface SurfaceExtractor {
	/***
	 * Extracts the surface in the bounding volume, Described by the density and type volumes.
	 */
	public List<SurfacePoint> extractSurface(Vector3f point, VoxelGrid hg, float resolution);
	
	
}
