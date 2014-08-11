/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.Map;

import voxelsystem.densityvolumes.shapes.BoxVolume;
import voxelsystem.densityvolumes.shapes.NoiseShape;
import voxelsystem.densityvolumes.shapes.SphereVolume;
import voxelsystem.operators.CSGOperators;
import voxelsystem.voxeldata.VoxelDensityExtractor;
import voxelsystem.voxeldata.VoxelExtractor;
import voxelsystem.voxelmaterials.VoxelType;
import voxelsystem.voxelobjects.PagingVoxelObject;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;


public class GSoC_DEMO extends SimpleApplication {

    public static void main(String[] args){
        GSoC_DEMO app = new GSoC_DEMO();
        app.start();
    }
    
    BitmapText position;//(print camera position)
    BitmapText direction; //(print camera direction)
    BitmapText selectedMaterialN; //print selected material
    BitmapText editorMode;
    
    private float counter = 0;
   
    Input input;
    public VoxelEditor editor;
    private BulletAppState bulletAppState;
    int selectedMaterial = 0;
    private Geometry cube;
    PagingVoxelObject world;
    
    int showMesh = -1;
    
    @Override
    public void simpleInitApp() {
    	Map<Integer, VoxelType> materialTypes = Materials.getMaterials(assetManager);
    	
    	//Make a box:
    	Vector3f boxCenter = new Vector3f(0,-1,0);//1.24234f,2.2342f,-1.4234f
		BoxVolume box = new BoxVolume(boxCenter, 5, 5.25f, 5);
    	
		//Make a sphere
		SphereVolume ss = new SphereVolume(new Vector3f(0, 0, 0), 4f);
    	//Set type to rock
    	ss.setSimpleType(2);
    	//Set type to sand
		box.setSimpleType(2);
	
		
		//Boiler plate.....
    	SphereVolume sv2 = new SphereVolume(new Vector3f(2f, 0, 0), 4f);
    	sv2.setSimpleType(1);
    	
    	SphereVolume sv = new SphereVolume(new Vector3f(2f, 2f, 2), 5f);
    	sv.setSimpleType(1);
    	float res = .5f;
    	
    	
    	world = new PagingVoxelObject(res, materialTypes, new Vector3f(50,50,50));
    	VoxelExtractor ve =new VoxelDensityExtractor(new NoiseShape(res));
    	
//    	
    	world.preformOperation(CSGOperators.unionOverwrite, ve.getBoundingBox(), ve);
    	world.update(rootNode,2500);


    	editor = new VoxelEditor(world, rootNode, assetManager);
    	
    	flyCam.setZoomSpeed(0);
    	flyCam.setMoveSpeed(10);
        cam.setLocation(new Vector3f(14.81f,2.7f,14.4f));
        
        //PHYSICS SETUP//
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //Basic world bounds
        bulletAppState.setWorldMin(new Vector3f(1000,1000,1000));
        bulletAppState.setWorldMin(new Vector3f(-1000,-1000,-1000));
        
        initMaterial();
      
        //SkyBox
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/BrightSky.dds", false));
        
        //////CROSS HAIRS (aka + sign)///////
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
        settings.getWidth() / 2 - ch.getLineWidth()/2, 
        settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
        guiNode.attachChild(ch);
        //////////////////////////////
        
        ScreenshotAppState screenShotState = new ScreenshotAppState();
        this.stateManager.attach(screenShotState);
    }
    

    @Override
    public void simpleUpdate(float tpf) {
        //Print Camera location
        position.setText(""+cam.getLocation());
        direction.setText(""+cam.getDirection());
        editorMode.setText("Edit Mode: " + editor.getModeName());
        selectedMaterialN.setText("Selected Material: "+selectedMaterial);
        counter+=tpf;
        selectedMaterial = selectedMaterial%6;
        world.update(rootNode,10);
        
        int mod  = 0;
        if(input.addDown){
        	mod = 1;
        	
        }else if(input.removeDown){
        	mod = 2;
        	
        }
        
        if(editor.mode != 4){
        	input.addDown = false;
        	input.removeDown = false;
        }
        
        editor.update(cam.getLocation(), cam.getDirection(), mod,selectedMaterial);
     }
 

  //Initialize Materials// 
  public void initMaterial() {
	  //Intialize Lights
	  DirectionalLight sun2 = new DirectionalLight();
      sun2.setColor(ColorRGBA.White);
      sun2.setDirection(new Vector3f(0.408248f, 0.408248f, 0.816497f).normalizeLocal().negate());
      rootNode.addLight(sun2);
      
      /* Drop shadows */
      final int SHADOWMAP_SIZE=1024;
      DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
      dlsr.setLight(sun2);
      dlsr.setShadowIntensity(.25f);
      viewPort.addProcessor(dlsr);
      dlsr.setEnabledStabilization(true);
      dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
      dlsr.setEdgesThickness(2);
      dlsr.setLambda(1.03f);
      
      
      FilterPostProcessor fpp;
      fpp = new FilterPostProcessor(assetManager);
      
      LightScatteringFilter filter = new LightScatteringFilter(sun2.getDirection().mult(-3000));
      filter.setEnabled(true);
      filter.setLightDensity(.25f);
//    filter.setLightDensity(f);
//    LightScatteringUI ui = new LightScatteringUI(inputManager, filter);
      fpp.addFilter(filter);
//      
   
      WaterFilter water;
      float initialWaterHeight = -2f;
      water = new WaterFilter(rootNode, sun2.getDirection());
      water.setWaterHeight(initialWaterHeight);
      fpp.addFilter(water);
      viewPort.addProcessor(fpp);
      
      
//      FogFilter fog=new FogFilter();
//      fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
//      fog.setFogDistance(20);
//      fog.setFogDensity(1f);
//      fpp.addFilter(fog);
//      viewPort.addProcessor(fpp);
   
      
      AmbientLight al = new AmbientLight();
      al.setColor(ColorRGBA.White.mult(1.55f));
      rootNode.addLight(al);

      //Initialize Textures.
      Texture rt = assetManager.loadTexture("Textures/Rock.PNG");
      rt.setWrap(WrapMode.Repeat);
      Texture bt = assetManager.loadTexture("Textures/BrickWall.jpg");
      bt.setWrap(WrapMode.Repeat);
    
   
        
      Box cube1Mesh = new Box( 1f,1f,1f);
      cube = new Geometry("My Textured Box", cube1Mesh);
      Material cube1Mat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
      cube1Mat.setColor("Color", new ColorRGBA(.25f, .25f, .25f,.5f));
      cube1Mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
      cube.setShadowMode(ShadowMode.Receive);
      
      cube.setMaterial(cube1Mat);
      cube.setQueueBucket(Bucket.Transparent);    
//      rootNode.attachChild(cube); 
        
      guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
      position = new BitmapText(guiFont, false);
      position.setLocalTranslation(0,settings.getHeight(), 0);
      

      
      
      
      selectedMaterialN = new BitmapText(guiFont, false);
      selectedMaterialN.setLocalTranslation((int)(settings.getWidth()-settings.getWidth()*.4f),selectedMaterialN.getLineHeight(), 0);
      
      editorMode = new BitmapText(guiFont, false);
      editorMode.setLocalTranslation((int)(settings.getWidth()-settings.getWidth()*.4f),selectedMaterialN.getLineHeight() + editorMode.getLineHeight() + 10, 0);
      
      direction = new BitmapText(guiFont, false);
      direction.setLocalTranslation(0,settings.getHeight()-position.getLineHeight(), 0);
      
      guiNode.attachChild(editorMode);
      guiNode.attachChild(selectedMaterialN);
      guiNode.attachChild(direction);
      guiNode.attachChild(position);
      
      input = new Input(this);
  }
  

  
   @Override
   public void start() {
		// set some default settings in-case
		// settings dialog is not shown
		settings = new AppSettings(true);
		settings.setTitle("Dual Contouring Demo");
//		settings.put
//		settings.set
		settings.setResolution(800, 600);
		showSettings = false;
//		 re-setting settings they can have been merged from the registry.
		setSettings(settings);
		super.start();
	}
	
	public Image load( String name){
		return assetManager.loadTexture(name).getImage();
	}
	
	
	
}