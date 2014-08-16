import voxelsystem.densityvolumes.shapes.BoxVolume;
import voxelsystem.densityvolumes.shapes.NoiseSphere;
import voxelsystem.densityvolumes.shapes.SphereVolume;
import voxelsystem.operators.CSGOperators;
import voxelsystem.voxeldata.VoxelDensityExtractor;
import voxelsystem.voxelobjects.PagingVoxelObject;
import voxelsystem.voxelobjects.Physics;
import voxelsystem.voxelobjects.Physics.HitData;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

/***
 * Utility class for editing voxels.
 * Shows resulting geometry before actually clicking.
 * @author 0xFFFF
 *
 */
public class VoxelEditor {
	int mode = 0;
	float size = 0;
	PagingVoxelObject pvo;
	
	Material material;
	Geometry cube;
	Geometry sphere;
	boolean show = true;
	Vector3f lockPoint;
	Vector3f lockPoint2;
	Vector3f lockOther;
	int lastClick = 0;
	
	public VoxelEditor(PagingVoxelObject pvo, Node root,AssetManager assetManager) {
		this.size = pvo.getVoxelSize();
		this.pvo = pvo;
		initializeMaterials(assetManager);
		
		 Box cube1Mesh = new Box(1f,1f,1f);
	     cube = new Geometry("Cube Display", cube1Mesh);
//	     cube.setShadowMode(ShadowMode.Receive);
	     cube.setMaterial(material);
	     cube.setQueueBucket(Bucket.Transparent);
	     
	     Sphere sMesh = new Sphere(30,30,1f);
	     sphere = new Geometry("Sphere Display", sMesh);
//	     sphere.setShadowMode(ShadowMode.Receive);
	     sphere.setMaterial(material);
	     sphere.setQueueBucket(Bucket.Transparent);
	     
	     root.attachChild(sphere);
	     root.attachChild(cube);
	     
	     
	     setSize(this.size);
	}
	
	
	public void initializeMaterials(AssetManager assetManager){
		 material = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
	     material.setColor("Color", new ColorRGBA(.25f, .25f, .25f,.5f));
	     material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
	}
	
	
	/****
	 * Mode 0 -> Sphere
	 * Mode 1 -> Cube
	 * Mode 2 -> Sphere + Paint
	 * Mode 3 -> Sphere + Cube-ularize
	 * Mode 4 -> Rectangle lock axis
	 * @param mode
	 */
	public void setMode(int mode){
		if(mode < 0){
			this.mode = getNumModes()-1;
		}else if(mode > getNumModes()-1){
			this.mode = 0;
		}else{
			this.mode = mode;
		}
	}
	
	
	public void setSize(float size){
		if(size < pvo.getVoxelSize()){
			this.size = pvo.getVoxelSize();
		}else if(size > 30){
			this.size = 30;
		}else{
			this.size = size;
		}
		
//		System.out.println(size);
//		root.detachChild(sphere);
//	    root.detachChild(cube);
//		cube.setLocalScale(this.size,this.size,this.size);
		cube.setMesh(new Box(this.size/2f,this.size/2f,this.size/2f));
		cube.updateModelBound();
		cube.updateGeometricState();
//		sphere.setLocalScale(this.size,this.size,this.size);
		sphere.setMesh( new Sphere(30,30,this.size));
		sphere.updateModelBound();
		sphere.updateGeometricState();
//		 
//	     root.attachChild(sphere);
//	     root.attachChild(cube);
	     
		
	}
	
	public float getSize(){
		return size;
	}
	
	public void display(boolean v){
		this.show = v;
	}
	
	/***
	 * Updates display and modifies voxel object if necessary
	 * 
	 * click -> 0 -> just update display 
	 * click -> 1 -> Left Click
	 * click -> 2 -> Right Click
	 * 
	 * @param pos
	 * @param direction
	 */
	public void update(Vector3f pos, Vector3f direction, int click, int material){
		
		//TODO: Collide against triangles...
		HitData hd = Physics.collideRay(pvo, new Vector3f(pos),new Vector3f(direction), 100, pvo.getVoxelSize());
		float x = round(hd.impact.x/pvo.getVoxelSize())*pvo.getVoxelSize();
		float y = round(hd.impact.y/pvo.getVoxelSize())*pvo.getVoxelSize();
		float z = round(hd.impact.z/pvo.getVoxelSize())*pvo.getVoxelSize();
		Vector3f center = new Vector3f(x,y,z);
		
		sphere.setLocalTranslation(center);
		cube.setLocalTranslation(center);
		sphere.setCullHint(CullHint.Always);
		cube.setCullHint(CullHint.Always);
		
		if(mode == 0 || mode == 4){
			cube.setCullHint(CullHint.Never);
		}else if(mode == 1 || mode == 2 || mode == 5 || mode ==3){
			sphere.setCullHint(CullHint.Never);
		}

		
		
		if(click == 1){
			if(mode == 0){
				BoxVolume bv  = new BoxVolume(center, this.size+.01f,this.size+.01f,this.size+.01f);
				bv.setSimpleType(material);
				this.pvo.preformOperation(CSGOperators.unionOverwrite, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
				
			}else if(mode == 1){
			
				SphereVolume sv  = new SphereVolume(center, this.size+.01f);
				sv.setSimpleType(material);
				this.pvo.preformOperation(CSGOperators.unionOverwrite, sv.getEffectiveVolume(), new VoxelDensityExtractor(sv));
			
			}else if(mode == 2){
				
				SphereVolume bv  = new SphereVolume(center, this.size+.01f);
				bv.setSimpleType(material);
				this.pvo.preformOperation(CSGOperators.paint, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
			
			}else if(mode == 4){
				
				modeLockRectangle(center);
				
			}else if(mode == 3){
				
				SphereVolume bv  = new SphereVolume(center, this.size+.01f);
				bv.setSimpleType(material);
				this.pvo.preformOperation(CSGOperators.makeCubed, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
			
			}else if(mode == 5){
				NoiseSphere bv  = new NoiseSphere(center, this.size+.01f);
				bv.setSimpleType(material);
				this.pvo.preformOperation(CSGOperators.unionOverwrite, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
			}
			
			
		}else if(click == 2){
		
			if(mode == 0){
				BoxVolume bv  = new BoxVolume(center, this.size+.01f,this.size+.01f,this.size+.01f);
				this.pvo.preformOperation(CSGOperators.difference, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
			}else if(mode == 1){
				SphereVolume bv  = new SphereVolume(center, this.size+.01f);
				this.pvo.preformOperation(CSGOperators.difference, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
			}else if(mode == 2){
			
			SphereVolume bv  = new SphereVolume(center, this.size+.01f);
			bv.setSimpleType(material);
			this.pvo.preformOperation(CSGOperators.paint, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
			
			}else if(mode == 4){
				
				modeLockRectangle(center);
				
			}else if(mode == 3){
				
				SphereVolume bv  = new SphereVolume(center, this.size+.01f);
				bv.setSimpleType(material);
				this.pvo.preformOperation(CSGOperators.makeCubed, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
			
			}else if(mode == 5){
				NoiseSphere bv  = new NoiseSphere(center, this.size+.01f);
				bv.setSimpleType(material);
				this.pvo.preformOperation(CSGOperators.difference, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
			}
		}
		
		if(click != lastClick && mode == 4){
			
			if(lockPoint != null && lockOther!=null){
				
				if(lastClick == 1){
					BoxVolume bv  = new BoxVolume(lockPoint2, lockOther.x+.1f,lockOther.y+.1f,lockOther.z+.1f);
					bv.setSimpleType(material);
					this.pvo.preformOperation(CSGOperators.unionOverwrite, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
				}else if(lastClick == 2){
					BoxVolume bv  = new BoxVolume(lockPoint2, lockOther.x+.01f,lockOther.y+.01f,lockOther.z+.01f);
					this.pvo.preformOperation(CSGOperators.difference, bv.getEffectiveVolume(), new VoxelDensityExtractor(bv));
				}
				
				lockPoint = null;
				lockOther = null;
			}
			setSize(this.size);
		}
		
		lastClick = click;
	}
	
	private void modeLockRectangle(Vector3f center){
		if(lockPoint == null){
			lockPoint = new Vector3f(center);
		}else{
			Vector3f diff = new Vector3f(lockPoint).subtract(center);
			float d[] = new float[3];
			d[0] = round(Math.abs(diff.x)/pvo.getVoxelSize())*pvo.getVoxelSize();
			d[1] = round(Math.abs(diff.y)/pvo.getVoxelSize())*pvo.getVoxelSize();
			d[2] = round(Math.abs(diff.z)/pvo.getVoxelSize())*pvo.getVoxelSize();
			
			if(d[0]<= d[1] && d[0]<d[2]){
				d[0] = this.size;
				d[1] = diff.y;
				d[2] = diff.z;
			}else if(d[1] <= d[0] && d[1]<=d[2]){
				d[0] = diff.x;
				d[1] = this.size;
				d[2] = diff.z;
			}else{
				d[0] = diff.x;
				d[1] = diff.y;
				d[2] = this.size;
			}
			lockPoint2 = new Vector3f((lockPoint.x-d[0]/2f),(lockPoint.y-d[1]/2f),(lockPoint.z-d[2]/2f));
			
			lockPoint2.x = round(lockPoint2.x/pvo.getVoxelSize())*pvo.getVoxelSize();
			lockPoint2.y = round(lockPoint2.y/pvo.getVoxelSize())*pvo.getVoxelSize();
			lockPoint2.z = round(lockPoint2.z/pvo.getVoxelSize())*pvo.getVoxelSize();
			
			
			//Limit size so game doesn't crash....
			lockOther = new Vector3f(d[0],d[1],d[2]);
			lockOther.x = Math.max(Math.min(lockOther.x, 100),-100);
			lockOther.y = Math.max(Math.min(lockOther.y, 100),-100);
			lockOther.z = Math.max(Math.min(lockOther.z, 100),-100);
			
			cube.setLocalTranslation(lockPoint2);
			cube.setMesh(new Box(lockOther.x/2f,lockOther.y/2f,lockOther.z/2.0f));
			cube.updateModelBound();
			cube.updateGeometricState();
		}
	}
	
	private int round(float x){
		int r;
		float f = Math.abs(x);
		if( f - (int)f >= .5){
			r = (int)f + 1;
		}else{
			r = (int)f;
		}
		
		if(x < 0){ //add back the sign
			r = -r;
		}
		
		return r;
	}
	
	public String getModeName(){
		if(mode == 0){
			return "Mode Cube";
		}
		
		if(mode == 1){
			return "Mode Sphere";
		}
		
		if(mode == 2){
			return "Mode Sphere Paint ";
		}
		
		if(mode == 3){
			return "Mode Sphere Pait Normals xD";
		}
		
		if(mode == 4){
			return "Mode Lock Rectangle";
		}
		
		if(mode == 5){
			return "Mode Mod Sphere Noise";
		}

		return "Unknown";
	}
	
	public int getNumModes(){
		return 6;
	}
	
	public int getMode(){
		return this.mode;
	}
	
	public void execute(int mode, int click, Vector3f position){
		
	}
	
}
