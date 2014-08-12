package voxelsystem;

import voxelsystem.densityvolumes.shapes.BoxVolume;
import voxelsystem.densityvolumes.shapes.SphereVolume;
import voxelsystem.voxeldata.VoxelDensityExtractor;
import voxelsystem.voxeldata.VoxelExtractor;

import com.jme3.math.Vector3f;

public class TestCases {
	public static void main(String [] args){
		
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
    	VoxelExtractor sphereExtractor = new VoxelDensityExtractor(ss);
    	VoxelExtractor boxExtractor = new VoxelDensityExtractor(box);
    	VoxelExtractor boxExtractor2 = new VoxelDensityExtractor(box2);
    	VoxelExtractor sphereExtractor2 = new VoxelDensityExtractor(new SphereVolume(new Vector3f(2f, 0, 0), 4f));
    	
    	VoxelExtractor paint = sphereExtractor;
//		VoxelExtractor paint = CSGOperators.union(false,CSGOperators.difference(CSGOperators.paint(sphereExtractor,boxExtractor),boxExtractor2),boxExtractor2);
//    	paint =  CSGOperators.difference(paint, sphereExtractor2);
    	
    	VoxelExtractor ve = paint.extract(new Vector3f(0,0,0), 10, 10, 10 , 1f);
	}
}
