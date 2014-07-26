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



import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import VoxelSystem.DensityVolumes.Shapes.BoxVolume;
import VoxelSystem.DensityVolumes.Shapes.NoiseShape;
import VoxelSystem.DensityVolumes.Shapes.SphereVolume;
import VoxelSystem.Operators.CSGOperators;
import VoxelSystem.VoxelData.VoxelDensityExtractor;
import VoxelSystem.VoxelData.VoxelExtractor;
import VoxelSystem.VoxelMaterials.VoxelType;
import VoxelSystem.VoxelObjects.PagingVoxelObject;

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
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
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
    private float operationWaitTime = 0;
    
    private boolean showingColor = true;
    private boolean showingTriangles = false;
    private boolean showingQuads = false;
    
    private int showMeshNum = -1;
    
    List<Geometry> worldGen;
    private Geometry[] colorMesh, wireMesh;
    private Geometry[] QuadData;
    PagingVoxelObject world;
    
    int showMesh = -1;
    
    @Override
    public void simpleInitApp() {
    	
    	
    	
    	Map<Integer, VoxelType> materialTypes = Materials.getMaterials(assetManager);
		
		//Set up voxel object bounds:
    	BoundingBox bb = new BoundingBox(new Vector3f(-20, -20, -20),new Vector3f(20, 20, 20));
    	
    	//Make a box:
    	Vector3f boxCenter = new Vector3f(0,-1,0);//1.24234f,2.2342f,-1.4234f
		BoxVolume box = new BoxVolume(boxCenter, 5, 5.25f, 5);
		BoxVolume box2 = new BoxVolume(new Vector3f(), 20, 20, 20);
    	
		//Make a sphere
		SphereVolume ss = new SphereVolume(new Vector3f(0, 0, 0), 4f);
    	//Set type to rock
    	ss.setSimpleType(2);
    	//Set type to sand
		box.setSimpleType(2);
	
		
		//Boiler plate.....
    	VoxelExtractor sphereExtractor = new VoxelDensityExtractor(ss);
    	VoxelExtractor boxExtractor = CSGOperators.translate(new VoxelDensityExtractor(box), new Vector3f(0,1,0));
    	VoxelExtractor boxExtractor2 = new VoxelDensityExtractor(box2);
    	SphereVolume sv2 = new SphereVolume(new Vector3f(2f, 0, 0), 4f);
    	sv2.setSimpleType(1);
    	VoxelExtractor sphereExtractor2 = new VoxelDensityExtractor(sv2);
    	
    	SphereVolume sv = new SphereVolume(new Vector3f(2f, 2f, 2), 5f);
    	sv.setSimpleType(1);
    	VoxelExtractor sphereExtractor3 = new VoxelDensityExtractor(sv);
    	//Add the box and sphere together
    	VoxelExtractor finalVolume = CSGOperators.union(false,CSGOperators.difference(CSGOperators.paint(sphereExtractor,boxExtractor),boxExtractor2),boxExtractor);
//    	paint =  CSGOperators.difference(paint, sphereExtractor2);
    	
    	float res = 1f;
    	
//    	Chunk c =new Chunk(new Vector3f(-4.0f,-4.0f,-4.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(-4.0f,-4.0f,0.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(-4.0f,0.0f,-4.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(-4.0f,0.0f,0.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(-4.0f,4.0f,-4.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(-4.0f,4.0f,0.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(0.0f,-4.0f,-4.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(0.0f,-4.0f,0.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(0.0f,0.0f,-4.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(0.0f,0.0f,0.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(0.0f,4.0f,-4.0f),20,20,20,0.25f);
//    	Chunk c =new Chunk(new Vector3f(0.0f,4.0f,0.0f),20,20,20,0.25f);
//    	c.extract(boxExtractor);
//    	Vector3f vn =  new Vector3f(-8.0f, -4.0f, -8.0f);
//    	VoxelGrid vg= new Chunk(vn,20,20,20, .25f);
//    	vg.extract(boxExtractor);
//    	DualContour dc = new DualContour();
//    	dc.extractSurface(vg);
    	

//    	vg.extract(CSGOperators.union(true, sphereExtractor2,vg));
    	//NOW: In place operations:
    	
    	Material green = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	green.setColor("Color", ColorRGBA.Green);
    	
    	world = new PagingVoxelObject(res, materialTypes, new Vector3f(50,50,50));
//    	VolumeShape vs = new NoiseShape();
//    	vs.setSimpleType(1);
    	
    	VoxelExtractor ve =new VoxelDensityExtractor(new NoiseShape(res));
    	
    	world.preformOperation(CSGOperators.unionOverwrite, ve.getBoundingBox(), ve);
//    	
//    	world.preformOperation(CSGOperators.unionOverwrite, sphereExtractor3.getBoundingBox(), sphereExtractor3);
    	world.update(rootNode,2500);
    	
//    	Vector3f borken = new Vector3f(15.116098f, 2.7305896f, 14.761848f);
//    	BoxVolume bv = new BoxVolume(borken,2f,2f,2f);
//    	bv.setSimpleType(1);
//    	world.add(new VoxelDensityExtractor(bv));
    	//Extract Mesh Data:
    	
//    	VoxelNode vn = new VoxelNode(new Vector3f(-20,-20,-20),(int)(40f/.25f),(int)(40f/.25f),(int)(40f/.25f),.25f);
//    	vn.extract(sphereExtractor);
//    	finalVolume = CSGOperators.union(false, vn,sphereExtractor);
    	
//    	VoxelGrid vg = new Chunk(new Vector3f(-20f,-20f,-20f), (int)(40f/res),(int)(40f/res),(int)(40f/res), res);
//    	vg.extract(boxExtractor);
    	
//    	VoxelObject vo = new VoxelObject(bb,vg, res);//finalVolume,.25f);
////    	init = System.currentTimeMillis();
//    	colorMesh = vo.extractGeometry(typeToMaterial);
////    	System.out.println((System.currentTimeMillis()-init));
//    	for(int i=0;i<colorMesh.length;i++){
//			  rootNode.attachChild(colorMesh[i]);
//		}
    	
    	
//    	DualContour dc = new DualContour();    	
//    	
//    	List<SurfacePoint> sps = dc.extractSurface(vg);
//    	BasicMesher bm = new BasicMesher();
//    	bm.addTriangles(sps);
//		Map<Integer, Mesh> meshes= bm.compileMeshes();
//		ArrayList<Geometry> generated = new ArrayList<Geometry>(meshes.size());
//		
//		for(Integer i : meshes.keySet()){
//			Mesh m = meshes.get(i);
//			m.updateBound();
//			m.updateCounts();
//			if(m.getTriangleCount() > 0){
//				Geometry g = new Geometry();
//				g.setMesh(m);
//				g.setMaterial(typeToMaterial.get(i));
//				generated.add(g);
//				rootNode.attachChild(g);
////				g.setLocalTranslation(this.getCorner());
//			}
//		}
    	

    	Material red = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	red.setColor("Color", ColorRGBA.Red);
    	
    	Material orange = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	orange.setColor("Color", ColorRGBA.Orange);
    	
    	Material blue = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    	blue.setColor("Color", ColorRGBA.Blue);
    	
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
  
    	
    	flyCam.setMoveSpeed(10);
        cam.setLocation(new Vector3f(14.81f,2.7f,14.4f));
        
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
    }
    

    @Override
    public void simpleUpdate(float tpf) {
        //Print Camera location
        position.setText(""+cam.getLocation());
        direction.setText(""+cam.getDirection());
        selectedMaterialN.setText("Selected Material: "+selectedMaterial);
        counter+=tpf;
        selectedMaterial = selectedMaterial%6;
        world.update(rootNode,10);
        
        if(input.addDown && operationWaitTime < counter){
//        	HitData hd = Physics.collideRay(world, cam.getLocation(), cam.getDirection(), 100, world.getVoxelSize());
//        	System.out.println(hd.impact);
        	Vector3f voxelBase = new Vector3f(cam.getLocation());
        	float v = world.getVoxelSize();
        	voxelBase.x = ((int)(voxelBase.x/v))*v;
        	voxelBase.y = ((int)(voxelBase.y/v))*v;
        	voxelBase.z = ((int)(voxelBase.z/v))*v;
        	
//        	SphereVolume bv = new SphereVolume(cam.getLocation(),10);
        	float f = FastMath.nextRandomFloat()*3f+1;
        	BoxVolume bv = new BoxVolume(voxelBase,2,2,2); 
        	bv.setSimpleType(selectedMaterial);
        	long init = System.currentTimeMillis();
        	world.preformOperation(CSGOperators.unionNoOverwrite,bv.getEffectiveVolume(),CSGOperators.makeCubed(new VoxelDensityExtractor(bv)));
//        	
//        	System.out.println((System.currentTimeMillis()-init));
        	input.addDown = false;
//        	operationWaitTime = counter + .15f;
//        	System.out.println(""+bv.count +" : "+bv.count2);
        }
//        
        if(input.removeDown){
        	Vector3f voxelBase = new Vector3f(cam.getLocation());
        	float v = world.getVoxelSize();
        	voxelBase.x = ((int)(voxelBase.x/v))*v;
        	voxelBase.y = ((int)(voxelBase.y/v))*v;
        	voxelBase.z = ((int)(voxelBase.z/v))*v;
        	
        	float f = FastMath.nextRandomFloat()*3f+1;
        	BoxVolume bv = new BoxVolume(voxelBase,2,2,2); 
        	bv.setSimpleType(selectedMaterial);
        	long init = System.currentTimeMillis();
//        	world.set();
        	world.preformOperation(CSGOperators.difference,bv.getEffectiveVolume(),new VoxelDensityExtractor(bv));
        	System.out.println((System.currentTimeMillis()-init));
//        	input.addDown = false;
        	operationWaitTime = counter + .15f;
        }
        
        updateBalls();
     }
 
    boolean wire = true;
  public void changeColorMesh(){
//	  if(showingColor){ //remove the meshes
//		//Remove old mesh
//		if(showMeshNum == -1){//remove all the meshes
//			  for(int i=0;i<colorMesh.length;i++){
//				  rootNode.detachChild(colorMesh[i]);
//			  }
//		 }else{ //remove a specific mesh
//			 rootNode.detachChild(colorMesh[showMeshNum]);
//		 }
//	  }
//	  
//	  showMeshNum++;
//	  if(showMeshNum >= colorMesh.length){
//		  showingColor = false;
//		  showMeshNum = -2;
//	  }else{
//		  showingColor = true;
//		  if(showMeshNum == -1){//remove all the meshes
//			  for(int i=0;i<colorMesh.length;i++){
//				  rootNode.attachChild(colorMesh[i]);
//			  }
//		 }else{
//			 rootNode.attachChild(colorMesh[showMeshNum]);
//		 }
//	  }
	  for(Geometry g : worldGen){
		  g.getMaterial().getAdditionalRenderState().setWireframe(wire);
		  rootNode.attachChild(g);
	  }
	  this.wire = !wire;
  }
  
  public void changeQuads(){
	  showMesh++;
	  for(Geometry g : worldGen){
		  rootNode.detachChild(g);
		  g.getMaterial().getAdditionalRenderState().setWireframe(false);
	  }
	  
	  showMesh = showMesh%worldGen.size();
	  rootNode.attachChild(worldGen.get(showMesh));
  }

  public void changeTriangles(){
//	  if(showingTriangles){
//		  for(Geometry g : wireMesh){
//			  rootNode.detachChild(g);
//		  }
//		  showingTriangles=false;
//	  }else{
//		  for(Geometry g : wireMesh){
//			  rootNode.attachChild(g);
//		  }
//		  showingTriangles=true;
//	  }
	  showMesh--;
	  for(Geometry g : worldGen){
		  rootNode.detachChild(g);
	  }
	  if(showMesh<0){
		  showMesh = worldGen.size();
	  }
	  showMesh = showMesh%worldGen.size();
	  rootNode.attachChild(worldGen.get(showMesh));
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
//      filter.setLightDensity(f);
//      LightScatteringUI ui = new LightScatteringUI(inputManager, filter);
      fpp.addFilter(filter);
      
   
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