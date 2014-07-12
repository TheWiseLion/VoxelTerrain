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



import idea.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import VoxelSystem.VoxelObject;
import VoxelSystem.DensityVolumes.TypeVolume;
import VoxelSystem.DensityVolumes.Shapes.BoxVolume;
import VoxelSystem.DensityVolumes.Shapes.SphereVolume;
import VoxelSystem.Hermite.HermiteDensityExtractor;
import VoxelSystem.Hermite.HermiteExtractor;
import VoxelSystem.MeshBuilding.DebugMesher;
import VoxelSystem.Operators.CSGOperators;
import VoxelSystem.SurfaceExtractors.DualContour;
import VoxelSystem.VoxelMaterials.MaterialBuilder;
import VoxelSystem.VoxelMaterials.VoxelType;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;


public class GSoC_DEMO extends SimpleApplication {

    public static void main(String[] args){
        GSoC_DEMO app = new GSoC_DEMO();
        app.start();
    }
    
    BitmapText position;//(print camera position)
    BitmapText direction; //(print camera direction)
    BitmapText selectedMaterialN; //print selected material
    Material shaded;
    Material rock;
    LinkedList<Geometry> ballSpheres;
    LinkedList<RigidBodyControl> ballBodies;
    Input input;
    
    private BulletAppState bulletAppState;
    int selectedMaterial = 0;
    private float counter = 0;
    private float radius = 1.0f;
    private static final int MAX_BALLS=200;
    private Sphere bullet;
    private SphereCollisionShape bulletCollisionShape;
    private Geometry cube;
    
    private boolean showingColor = true;
    private boolean showingTriangles = false;
    private boolean showingQuads = false;
    
    private int showMeshNum = -1;
    
    
    private Geometry[] colorMesh, wireMesh;
    private Geometry[] QuadData;
    
    
    int showMesh = -1;
    
    @Override
    public void simpleInitApp() {

		// Initialize Voxel Materials:
		List<VoxelType> types = new ArrayList<VoxelType>();
		VoxelType rock = new VoxelType();
		rock.name = "rock";
		rock.typeID = 0;
		rock.colorMap = "Textures/TextureArray/BlackRockD512.png";

		
		VoxelType sand = new VoxelType();
		sand.name = "sand";
		sand.typeID = 1;
		sand.colorMap = "Textures/TextureArray/SandD512.png";
		
		VoxelType lava = new VoxelType();
		lava.name = "lava";
		lava.typeID = 2;
		lava.colorMap = "Textures/TextureArray/lavaD512.png";

		types.add(rock);
		types.add(sand);
		types.add(lava);
		
		MaterialBuilder mb = new MaterialBuilder(512, 4, MinFilter.Trilinear);
		Map<Integer, Material> typeToMaterial = mb.getMaterials(types,assetManager);
    	
    	
    	//Set up voxel object bounds:
    	BoundingBox bb = new BoundingBox(new Vector3f(-20, -20, -20),new Vector3f(20, 20, 20));
		
    	//Make a sphere:
//    	SphereVolume ss = new SphereVolume(new Vector3f(0, 0, 0), 4f);
    	//Set type to rock
//    	ss.setSimpleType(0);
//    	ss.setTypeVolume(new TypeVolume() {
//			@Override
//			public int getType(float x, float y, float z) {
//				if(x > 2*y){
//					return 0;
//				}else{
//					return 1;
//				}
//			}
//		});
    	
    	//Make a box:
    	Vector3f boxCenter = new Vector3f(0,2.5f,0);
		BoxVolume box = new BoxVolume(boxCenter, 5, 5, 5);
		BoxVolume box2 = new BoxVolume(new Vector3f(-2.5f,0,-2.5f), 5, 20, 5);
    	
		//Make a sphere
		SphereVolume ss = new SphereVolume(new Vector3f(0, 0, 0), 4f);
    	//Set type to rock
    	ss.setSimpleType(0);
    	//Set type to sand
		box.setSimpleType(2);
	
		
		//Boiler plate.....
    	HermiteExtractor sphereExtractor = new HermiteDensityExtractor(ss);
    	HermiteExtractor boxExtractor = new HermiteDensityExtractor(box);
    	HermiteExtractor boxExtractor2 = new HermiteDensityExtractor(box2);
    	
    	//Add the box and sphere together
    	HermiteExtractor paint = CSGOperators.difference((CSGOperators.union(true,sphereExtractor,boxExtractor)),boxExtractor2);
		
    	
    	
    	float res = .25f;
    	
    	
//    	Subtract new box from sphere:
    	HermiteExtractor finalVolume = paint;//CSGOperators.difference(union,box2);
    	
    	//Set LOD:
    	VoxelObject vo = new VoxelObject(bb,finalVolume, res);//finalVolume,.25f);
        
      
    	//Extract Mesh Data:
    	colorMesh = vo.extractGeometry(typeToMaterial);
    	
    	for(int i=0;i<colorMesh.length;i++){
			  rootNode.attachChild(colorMesh[i]);
		}
//    	
    	//Prepare debug data:
    	DualContour dc = new DualContour();
    	Material red = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	red.setColor("Color", ColorRGBA.Red);
    	
    	Material orange = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	orange.setColor("Color", ColorRGBA.Orange);
    	
    	Material blue = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	blue.setColor("Color", ColorRGBA.Blue);
    	Material green = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	green.setColor("Color", ColorRGBA.Green);
    	
    	Material teal = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	teal.setColor("Color", ColorRGBA.Cyan);
    	
    	Material magenta = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	magenta.setColor("Color", ColorRGBA.Magenta);
    	
    	Material pink = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	pink.setColor("Color", ColorRGBA.Pink);
    	
    	Material white = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	white.setColor("Color", ColorRGBA.White);
    	
    	Map<Integer,Material> iToM= new HashMap<Integer,Material>();
    	iToM.put(0, orange);
    	iToM.put(1, blue);
    	iToM.put(2, red);
    	
//    	wireMesh = vo.extractGeometry(typeToMaterial);
//    	for (Geometry g : wireMesh){
//    		g.setMaterial(green);
//    		g.getMaterial().getAdditionalRenderState().setWireframe(true);
//    		g.getMaterial().getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
//    	}
    	
    	
//    	List<Vector3f> xp = new ArrayList<Vector3f>(), yp = new ArrayList<Vector3f>(), zp = new ArrayList<Vector3f>();
    	
    	
    	//Extract Triangle Data (for debugging):
//    	wireMesh = DebugMesher.getTriangleMesh(dc.extractSurface(finalVolume, bb, res, false),iToM, green);
    	wireMesh = DebugMesher.getQuadMesh(dc.extractSurface(finalVolume, bb, res, true),iToM, green);
    	
    	//Extract Quad Data (for debugging):
    	QuadData = DebugMesher.getQuadMesh(dc.extractSurface(sphereExtractor, bb, res, true),iToM, green);
    	
//    	Chunk c = new Chunk(64,64,64);
//    	c.extract(new Vector3f(-5f,-5f,-5f), .25f, he1);
//    	c = c.downSample(); //has some edge cases...
//    	
//    	idea.DualContour idc = new idea.DualContour();
//    	wireMesh = DebugMesher.getTriangleMesh(idc.extractSurface(c,new Vector3f(.25f,.5f,.25f)),iToM, green);
        
    	flyCam.setMoveSpeed(10);
        cam.setLocation(new Vector3f(0,0,0));
        
        //PHYSICS SETUP//
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //Basic world bounds
        bulletAppState.setWorldMin(new Vector3f(1000,1000,1000));
        bulletAppState.setWorldMin(new Vector3f(-1000,-1000,-1000));
        
        //Init our bullet//
        bullet = new Sphere(32, 32, 0.4f, true, false);
        bullet.setTextureMode(TextureMode.Projected);
        bulletCollisionShape = new SphereCollisionShape(0.4f);
        
        ballSpheres = new LinkedList<Geometry>();
        ballBodies = new LinkedList<RigidBodyControl>();
        
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
//        initKeys();
    }
    

    
    @Override
    public void simpleUpdate(float tpf) {
        //Print Camera location
        position.setText(""+cam.getLocation());
        direction.setText(""+cam.getDirection());
        selectedMaterialN.setText("Selected Material: "+selectedMaterial);
        counter+=tpf;
        updateBalls();
     }
 
  public void changeColorMesh(){
	  if(showingColor){ //remove the meshes
		//Remove old mesh
		if(showMeshNum == -1){//remove all the meshes
			  for(int i=0;i<colorMesh.length;i++){
				  rootNode.detachChild(colorMesh[i]);
			  }
		 }else{ //remove a specific mesh
			 rootNode.detachChild(colorMesh[showMeshNum]);
		 }
	  }
	  
	  showMeshNum++;
	  if(showMeshNum >= colorMesh.length){
		  showingColor = false;
		  showMeshNum = -2;
	  }else{
		  showingColor = true;
		  if(showMeshNum == -1){//remove all the meshes
			  for(int i=0;i<colorMesh.length;i++){
				  rootNode.attachChild(colorMesh[i]);
			  }
		 }else{
			 rootNode.attachChild(colorMesh[showMeshNum]);
		 }
	  }
  }
  
  public void changeQuads(){
	  if(showingQuads){
		  for(int i=0;i<QuadData.length;i++){
			  rootNode.detachChild(QuadData[i]);
		  }
		  showingQuads=false;
	  }else{
		  for(int i=0;i<QuadData.length;i++){
			  rootNode.attachChild(QuadData[i]);
		  }
		  showingQuads=true;
	  }
  }

  public void changeTriangles(){
	  if(showingTriangles){
		  for(Geometry g : wireMesh){
			  rootNode.detachChild(g);
		  }
		  showingTriangles=false;
	  }else{
		  for(Geometry g : wireMesh){
			  rootNode.attachChild(g);
		  }
		  showingTriangles=true;
	  }
  }
  
  //Initialize Materials// 
  public void initMaterial() {
	  //Intialize Lights
	  DirectionalLight sun2 = new DirectionalLight();
      sun2.setColor(ColorRGBA.White);
      sun2.setDirection(new Vector3f(0.408248f, 0.408248f, 0.816497f).normalizeLocal().negate());
      rootNode.addLight(sun2);
      
      AmbientLight al = new AmbientLight();
      al.setColor(ColorRGBA.White.mult(1.3f));
      rootNode.addLight(al);

      //Initialize Textures.
      Texture rt = assetManager.loadTexture("Textures/Rock.PNG");
      rt.setWrap(WrapMode.Repeat);
      Texture bt = assetManager.loadTexture("Textures/BrickWall.jpg");
      bt.setWrap(WrapMode.Repeat);
    
      rock = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
      rock.setTexture("ColorMap",rt);
        
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
      
      direction = new BitmapText(guiFont, false);
      direction.setLocalTranslation(0,settings.getHeight()-position.getLineHeight(), 0);
      
      guiNode.attachChild(selectedMaterialN);
      guiNode.attachChild(direction);
      guiNode.attachChild(position);
      
      input = new Input(this);
  }
  
  public void shootBall(){
       Geometry bulletg = new Geometry("bullet", bullet);
       bulletg.setMaterial(rock);
       bulletg.setShadowMode(ShadowMode.CastAndReceive);
       bulletg.setLocalTranslation(cam.getLocation());
       RigidBodyControl bulletNode = new RigidBodyControl(new SphereCollisionShape(0.4f), 10.0f);
       bulletNode.setLinearVelocity(cam.getDirection().mult(25));
       bulletg.addControl(bulletNode);
       bulletNode.setSleepingThresholds(0f, 0f);
       rootNode.attachChild(bulletg);
       bulletAppState.getPhysicsSpace().add(bulletNode);
       
       //Do i really need to keep two lists?
       if(ballSpheres.size()>= MAX_BALLS){
           rootNode.detachChild(ballSpheres.pollFirst());
           bulletAppState.getPhysicsSpace().remove(ballBodies.pollFirst());
       }
       ballSpheres.addLast(bulletg); 
       ballBodies.addLast(bulletNode);
  }
  
  private void updateBalls(){
      Iterator<RigidBodyControl> iter = ballBodies.iterator();
      while(iter.hasNext()){
          RigidBodyControl rbc = iter.next();
          //Is this body out of bounds?
          if(rbc.getPhysicsLocation().lengthSquared() > 1000*1000){
              //remove
              int i = ballBodies.indexOf(rbc);
              ballSpheres.remove(i); //Yeah kinda dumb.
              iter.remove();
          }
      }
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