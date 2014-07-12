package VoxelSystem.SurfaceExtractors;

import idea.HermiteGrid;

import java.util.List;

import VoxelSystem.Hermite.HermiteExtractor;
import VoxelSystem.MeshBuilding.SurfacePoint;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public interface SurfaceExtractor {
	/***
	 * Extracts the surface in the bounding volume, Described by the density and type volumes.
	 */
	public List<SurfacePoint> extractSurface(HermiteExtractor hermiteData,BoundingBox bb,float resolution);
	
	
	public List<SurfacePoint> extractSurface(HermiteGrid hg, float resolution);//, Vector3f offset
}
