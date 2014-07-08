package VoxelSystem.VoxelMaterials;

import java.awt.Color;

import com.jme3.texture.Texture;

public class VoxelType {
	public String name;
	public int typeID;
	
	//Textures:
	public String colorMap;
	public String bumpMap;
	public String specularMap;
	public String parallaxMap;
	
	//Solid Color
	public Color c;
	public float tint;
	
	//Illumination:
	public IlluminationType illumination;
	public Object illuminationOptions;
	
	//Blending Hints for mesh maker
	public boolean shouldBlend;
	
	//Note if opacity < 1 
	public float reflectivity; //Value must be between [0.0-1.0]
	public float opacity; //Value must be between [0.0-1.0]
	
	//TODO:
	//Vegitation Options
	
}
