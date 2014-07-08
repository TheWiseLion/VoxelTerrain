import VoxelSystem.DensityVolumes.TypeVolume;


public class TrollType implements TypeVolume{
	private double r1= Math.random()*.99+.01;
	private double r2= Math.random()*.99+.01;
	private double r3= Math.random()*.99+.01;
	
	@Override
	public int getType(float x, float y, float z) {
//		x = Math.abs(x);
//		y = Math.abs(y);
//		z = Math.abs(z);
//		
//		return ((int)(100*x*r1+100*y*r2+100*z*r3))%2;
		if(x>=0){
			return 0;
		}else{
			return 1;
		}
	}

}
