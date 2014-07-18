package VoxelSystem;

import idea.VoxelGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import VoxelSystem.DensityVolumes.DensityVolume;
import VoxelSystem.Hermite.VoxelExtractor;
import VoxelSystem.MeshBuilding.BasicMesher;
import VoxelSystem.MeshBuilding.SurfacePoint;
import VoxelSystem.SurfaceExtractors.DualContour;
import VoxelSystem.SurfaceExtractors.SurfaceExtractor;

import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;

/***
 * This object represents the geometry of some given {@link DensityVolume}
 * in some given region of space. It takes level of detail and an optional {@link SurfaceExtractor}
 * as parameters.
 * @author wiselion
 *
 */
public class VoxelObject {
	private BoundingBox bb;
	private VoxelExtractor dv;
	private float resolution;
	private SurfaceExtractor se;

//	private static final Material errorMaterial;
	
//	{
//		errorMaterial = new Material(,"Common/MatDefs/Misc/Unshaded.j3md");
//		errorMaterial.setColor("Color", ColorRGBA.Gray); 
//	}
	
	public VoxelObject(BoundingBox bb,VoxelExtractor dv,float resolution,SurfaceExtractor s){
		//Do some error checking:
		if(bb==null || dv == null || resolution<=0 || s==null){
			throw new IllegalArgumentException("Voxel Object must have valid parameters");
		}
		this.bb = bb;
		this.dv = dv;
		this.resolution = resolution;
		this.se = s;
	}
	
	/***
	 * If no SurfaceExtractor specified DualContour is used.
	 */
	public VoxelObject(BoundingBox bb,VoxelExtractor dv,float resolution){
		this(bb,dv,resolution,new DualContour());
	}
	
	public Geometry[] extractGeometry(Map<Integer,Material> typeToMaterial){
		validateParameters();
		
		Vector3f ub = bb.getMax(new Vector3f());
		Vector3f lb = bb.getMin(new Vector3f());
		int stepX = (int) (Math.abs(ub.x-lb.x)/resolution);
		int stepY = (int) (Math.abs(ub.x-lb.x)/resolution);
		int stepZ = (int) (Math.abs(ub.x-lb.x)/resolution);
		
		VoxelGrid hg  = dv.extract(lb,stepX,stepY,stepZ,resolution);
		
		
		Material errorMaterial = typeToMaterial.get(-1);
		List<SurfacePoint> surfacePoints = se.extractSurface(lb,hg, resolution);
		BasicMesher bm = new BasicMesher();
		bm.addTriangles(surfacePoints);
		
		Map<Integer,Mesh> typeToMesh = bm.compileMeshes();
		
		if(typeToMesh.size()==0){
			return new Geometry[0];
		}
		
		List<Geometry> geometry = new ArrayList<Geometry>();
		
		for(Integer i : typeToMesh.keySet()){
			Mesh m = typeToMesh.get(i);
			m.updateBound();
			m.updateCounts();
			
			Geometry g = new Geometry("Type: "+i,m);
			if(typeToMaterial.containsKey(i)){
				g.setMaterial(typeToMaterial.get(i));
			}else{ //No material set. Use default material
				g.setMaterial(errorMaterial);
			}
//			g.setCullHint(CullHint.Never);
			g.getMaterial().getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
			
			geometry.add(g);
		}
		return geometry.toArray(new Geometry[0]);
		
	}
	
	private void validateParameters(){}
	
	//////////////////////////////////Getters-Setters///////////////////////////////////////////////
	public BoundingBox getBoundingBox() {
		return bb;
	}

	public void setBoundingBox(BoundingBox bb) {
		if(bb==null){
			throw new IllegalArgumentException("Voxel Object must have a bounding volume");
		}
		this.bb = bb;
	}

	public float getResolution() {
		return resolution;
	}

	public void setResolution(float lod) {
		if(lod<=0){
			throw new IllegalArgumentException("lod must be greater then zero");
		}
		this.resolution = lod;
	}

	public SurfaceExtractor getSurfaceExtractor() {
		return se;
	}

	public void setSurfaceExtractor(SurfaceExtractor se) {
		if(se==null){
			throw new IllegalArgumentException("lod must be greater then zero");
		}
		this.se = se;
	}

}
