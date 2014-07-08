package VoxelSystem.VoxelMaterials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.TextureArray;
import com.jme3.texture.plugins.AWTLoader;

/***
 * Takes a list of VoxelType's and builds the appropriate material for each 
 * @author 0xFFFF
 *
 */
public class MaterialBuilder {
	public final static int MAX_MATERIALS = 255;
	
	private Map<String,Integer> stringToIndexDiffuse;
	private Map<Integer,String> indexToStringDiffuse;
	
	private Map<String,Image> textures;
	private MinFilter mf;
	private int anisotropy;
	//TODO: other image resolutions: 'detail map,noise map,???' 
	private AWTLoader awt;
	private final int tileDim;
	private Format diffuseFormat = Format.ABGR8;
//	private Format normalFormat = Format.BGR8;
//	private Format specularFormat = Format.BGR8;
	
	public MaterialBuilder(int tileDimension, int anisotropicLevel,MinFilter mf){
		stringToIndexDiffuse = new HashMap<String,Integer>();
		indexToStringDiffuse = new HashMap<Integer,String>();
		tileDim = tileDimension;
		this.anisotropy = anisotropicLevel;
		this.mf = mf;
		awt= new AWTLoader();
		textures= new HashMap<String,Image>();
	}
	
	
	/***
	 * Takes a list of voxelTypes that are constructed into VoxelMaterials.
	 * An immutable mapping of type to material is returned.
	 */
	public Map<Integer,Material> getMaterials(List<VoxelType> voxelTypes, AssetManager loader){
		
		
		
		HashMap<Integer,Material> materialMapping = new HashMap<Integer,Material>();
		for(VoxelType vt : voxelTypes){
			//load all images
			if(vt.colorMap!=null){
				insertMap(vt.colorMap,stringToIndexDiffuse,indexToStringDiffuse,loader,tileDim,diffuseFormat);
			}
		}
		
		//Create Texture Arrays
		TextureArray diffuseMap;
		List<Image> diffuseImages = new ArrayList<Image>(indexToStringDiffuse.size());
		for(int i=0;i<indexToStringDiffuse.size();i++){
			String image = indexToStringDiffuse.get(i);
			diffuseImages.add(textures.get(image));
		}
		diffuseMap = new TextureArray(diffuseImages);
		diffuseMap.setMinFilter(mf);
		diffuseMap.setAnisotropicFilter(anisotropy);
		diffuseMap.setWrap(WrapMode.Repeat);
		
		//Assign Create Materials
		for(VoxelType vt : voxelTypes){
			Material m =  new Material(loader,"Shaders/VoxelMaterial.j3md");
			m.setTexture("colorMaps", diffuseMap);
			int i;
			if(vt.colorMap!=null){
				i = stringToIndexDiffuse.get(vt.colorMap);
			}else{
				i = -1;
			}
			m.setInt("colorIndex", i);
			materialMapping.put(vt.typeID, m);
		}
		
		//TODO: immutable
		
		//Special Values:
		//-1 -> "error" texture
		Material error = new Material(loader,"Common/MatDefs/Misc/Unshaded.j3md");
		error.setColor("Color", ColorRGBA.Gray);
		//-2 -> The mighty mighty multimaterial shader
		materialMapping.put(-1, error);
		
		return materialMapping;
	}
	
	
	/***
	 * Insert into map and load any needed images.
	 * @param s
	 * @param map
	 */
	private void insertMap(String s,Map<String,Integer> sToI,Map<Integer,String> iToS,AssetManager loader,int res,Format imageFormat){
		if(!sToI.containsKey(s)){
			int index = sToI.size();
			sToI.put(s,index);
			iToS.put(index,s);
			load(s,loader,res,imageFormat);
		}
	}
	
	private void load(String name,AssetManager loader,int res,Format imageFormat){
		if(!textures.containsKey(name)){
			Texture tx = loader.loadTexture(name);
			Image i = tx.getImage();
			i = MaterialTools.scaleImage(i, res, awt);
			i = MaterialTools.setImageFormat(i, imageFormat);
			textures.put(name, i);
			//Set texture attributes:
//			tx.setMinFilter(MinFilter.Trilinear);
//			tx.setAnisotropicFilter(anisotropy);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
