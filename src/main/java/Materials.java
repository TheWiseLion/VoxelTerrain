import java.util.HashMap;
import java.util.Map;

import voxelsystem.voxelmaterials.VoxelType;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;


public class Materials {
	public static Map<Integer,VoxelType> getMaterials(AssetManager assetManager){
		//Load Textures:
    	Texture [] textures = new Texture[]{
    		assetManager.loadTexture("Textures/TextureArray/BlackRockD512.png"),
    		assetManager.loadTexture("Textures/TextureArray/BlackRockH512.png"),
    		assetManager.loadTexture("Textures/TextureArray/BlackRockN512.png"),
    		
    		assetManager.loadTexture("Textures/TextureArray/SandD512.png"),
    		assetManager.loadTexture("Textures/TextureArray/SandS512.png"),
    		assetManager.loadTexture("Textures/TextureArray/SandN512.png"),
    		
    		assetManager.loadTexture("Textures/TextureArray/lavaD512.png"),
    		assetManager.loadTexture("Textures/TextureArray/lavaS512.png"),
    		assetManager.loadTexture("Textures/TextureArray/lavaN512.png"),
    		
    		
    		assetManager.loadTexture("Textures/TextureArray/CrackedMudD512.png"),
    		assetManager.loadTexture("Textures/TextureArray/CrackedMudS512.png"),
    		assetManager.loadTexture("Textures/TextureArray/CrackedMudN512.png"),
    		
    		assetManager.loadTexture("Textures/TextureArray/StonePlatesD512.png"),
    		assetManager.loadTexture("Textures/TextureArray/StonePlatesS512.png"),
    		assetManager.loadTexture("Textures/TextureArray/StonePlatesN512.png"),
    		
    		
    		assetManager.loadTexture("Textures/TextureArray/BlackPebbelsD512.png"),
    		assetManager.loadTexture("Textures/TextureArray/BlackPebbelsS512.png"),
    		assetManager.loadTexture("Textures/TextureArray/BlackPebbelsN512.png")
    		
    		
    	};
    	
    	for(Texture t : textures){
    		t.setWrap(WrapMode.Repeat);
    		t.setMinFilter(MinFilter.Trilinear);
    		t.setAnisotropicFilter(4);
    	}
    	
    	//Initialize Materials:
    	Material rockMaterial = new Material(assetManager, "Shaders/VoxelMaterial.j3md"); 
		rockMaterial.setTexture("DiffuseMap", textures[0]);
		rockMaterial.setTexture("NormalMap", textures[2]);
		rockMaterial.setTexture("SpecularMap", textures[1]);
		
		
		
		
		Material sandMaterial = new Material(assetManager, "Shaders/VoxelMaterial.j3md"); 
		sandMaterial.setTexture("DiffuseMap", textures[3]);
		sandMaterial.setTexture("SpecularMap", textures[4]);
		sandMaterial.setTexture("NormalMap", textures[5]);
		
		
		Material lavaMaterial = new Material(assetManager, "Shaders/VoxelMaterial.j3md"); 
		lavaMaterial.setTexture("DiffuseMap", textures[6]);
		lavaMaterial.setTexture("SpecularMap", textures[7]);
		lavaMaterial.setTexture("NormalMap", textures[8]);
		lavaMaterial.setFloat("Shininess", 30);
		
		Material mudMaterial = new Material(assetManager, "Shaders/VoxelMaterial.j3md"); 
		mudMaterial.setTexture("DiffuseMap", textures[9]);
		mudMaterial.setTexture("SpecularMap", textures[10]);
		mudMaterial.setTexture("NormalMap", textures[11]);
		mudMaterial.setFloat("Shininess", 30);
		
		
		Material stonePlateMaterial = new Material(assetManager, "Shaders/VoxelMaterial.j3md"); 
		stonePlateMaterial.setTexture("DiffuseMap", textures[12]);
		stonePlateMaterial.setTexture("SpecularMap", textures[13]);
		stonePlateMaterial.setTexture("NormalMap", textures[14]);
		stonePlateMaterial.setFloat("Shininess", 30);
		
		
		Material blackPebbleMaterial = new Material(assetManager, "Shaders/VoxelMaterial.j3md"); 
		blackPebbleMaterial.setTexture("DiffuseMap", textures[15]);
		blackPebbleMaterial.setTexture("SpecularMap", textures[16]);
		blackPebbleMaterial.setTexture("NormalMap", textures[17]);
		blackPebbleMaterial.setFloat("Shininess", 30);
		
		
		// Initialize Voxel Materials:
		Map<Integer,VoxelType> types = new HashMap<Integer,VoxelType>();
		VoxelType rock = new VoxelType();
		rock.name = "rock";
		rock.material = rockMaterial;
		
		VoxelType sand = new VoxelType();
		sand.name = "sand";
		sand.material = sandMaterial;
		
		VoxelType lava = new VoxelType();
		lava.name = "lava";
		lava.material = lavaMaterial;
		
		VoxelType mud = new VoxelType();
		mud.name = "mud";
		mud.material = mudMaterial;
		
		VoxelType plates = new VoxelType();
		plates.name = "plates";
		plates.material = stonePlateMaterial;
		
		VoxelType pebbles = new VoxelType();
		pebbles.name = "pebbles";
		pebbles.material = blackPebbleMaterial;
		
		types.put(0,rock);
		types.put(1,sand);
		types.put(2,lava);
		types.put(3,plates);
		types.put(4,pebbles);
		types.put(5,mud);
		
		return types;
	}
}
