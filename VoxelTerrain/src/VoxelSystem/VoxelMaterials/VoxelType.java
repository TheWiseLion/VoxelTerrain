package VoxelSystem.VoxelMaterials;

import com.jme3.material.Material;


public class VoxelType {
	public String name;
	public Material material;
	private boolean renderSubsurface = false;

	
	/***
	 * This is true if setRenderSubsurface is called with argument true.
	 * If this is true then FaceCullMode will be disable and blend mode will be set to
	 * BlendMode.Alpha and queue bracket set to Bucket.Transparent
	 * 
	 * TODO: Ouput Type:List<Mesh> instead....
	 * @return
	 */
	public boolean willRenderSubsurface(){
		return renderSubsurface;
	}
	
	public void setRenderSubsurface(boolean b){
		renderSubsurface = b;
	}
	
}
