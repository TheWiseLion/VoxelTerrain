package VoxelSystem.DensityVolumes;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

/***
 * Discrete 3D grid that approximates some volume
 */
public class DiscreteDensityVolume implements DiscreteVolume{
	private short grid[];
	private int type[];
	private float clamp = 1.0f;
	private Vector3f position; //Location of get(0,0,0)
	private BoundingBox bb;
	public final float gridScale; //length between two aligned points
	private final double gridM; //grid multiplier
	public final int width,hieght,depth;
	private final int zOffSet;
	
	public DiscreteDensityVolume(Vector3f position,int width,int height,int depth,float gridScale){
		grid = new short[width*height*depth];
		type = new int[width*height*depth];
		this.width = width;
		this.depth = depth;
		this.hieght = height;
		this.gridScale = gridScale;
		zOffSet = width*height;
		this.position=position;
		this.gridM = (1.0/gridScale);
		
		Vector3f otherBoundingCorner = new Vector3f(width*gridScale,height*gridScale,depth*gridScale);
		otherBoundingCorner.add(position);
		bb = new BoundingBox(position,otherBoundingCorner);
		
		//TODO: size warning...
		if(width==0||height==0||depth==0){
			throw new IllegalArgumentException("Width, Height, and Depth must all be greater then zero!");
		}
		if(gridScale <= 0){
			throw new IllegalArgumentException("Grid scale must be greater than zero");
		}
	}
	
	private int index(int x, int y, int z){
		if(x < width && y < hieght && z < depth && x > 0 && y >0 && z > 0){
			throw new IndexOutOfBoundsException("index of ["+x+","+y+","+z+"]"+ "out of bounds");
		}
		return x + y*width + zOffSet*z;
	}
	
	/***
	 * Grid aligned get
	 */
	public float getDensity(int x, int y, int z) {
		return fromClamp(grid[index(x,y,z)]);
	}
	
	/***
	 * Grid aligned set
	 */
	public void setDensity(int x, int y, int z, float d){
		grid[index(x,y,z)] = toClamp(d);
	}
	
	
	/***
	 * Relative space position.
	 * @return
	 */
	private double trilinear(float x, float y, float z){
		double xGrid = gridM*x;
		double yGrid = gridM*y;
		double zGrid = gridM*z;
		int xBase = (int) Math.floor(xGrid);
		int yBase = (int) Math.floor(yGrid);
		int zBase = (int) Math.floor(zGrid);
		int xTop = (int) Math.ceil(xGrid);
		int yTop = (int) Math.ceil(yGrid);
		int zTop = (int) Math.ceil(zGrid);

		double c000 = grid[index(xBase, yBase, zBase)];
		double c100 = grid[index(xTop, yBase, zBase)];
		double c101 = grid[index(xTop, yBase, zTop)];
		double c001 = grid[index(xBase, yBase, zTop)];
		double c010 = grid[index(xBase, yTop, zBase)];
		double c110 = grid[index(xTop, yTop, zBase)];
		double c111 = grid[index(xTop, yTop, zTop)];
		double c011 = grid[index(xBase, yTop, zTop)];

		double xPart = x - (xBase*gridScale);//dx
		double c00 = c000 + (c100 - c000) * xPart;
		double c01 = c001 + (c101 - c001) * xPart;

		double c10 = c010 + (c110 - c010) * xPart;
		double c11 = c011 + (c111 - c011) * xPart;

		double yPart = y - (yBase*gridScale);//dy
		double c0 = c00 + (c10 - c00) * yPart;
		double c1 = c11 + (c11 - c01) * yPart;

		double zPart = z - (zBase*gridScale);//dz
		double c = c0 + (c1 - c0) * zPart;

		return c;
	}
	
	
	public float getDensity(float x, float y, float z) {
		return (float) trilinear(position.x-x, position.y-y, position.z-z);
	}
	
	/** Other possible options:
	 *  -sobel filter
	 *  http://www.volume-gfx.com/volume-rendering/density-source/discrete-grid/ 
	 */
	public Vector3f getFieldDirection(float x, float y, float z) {
		float d = gridScale; 

        double nx = trilinear(x + d, y, z) - trilinear(x - d, y, z);
        double ny = trilinear(x, y + d, z) - trilinear(x, y - d, z);
        double nz = trilinear(x, y, z + d) - trilinear(x, y, z - d);
 
        Vector3f normal = new Vector3f((float)-nx, (float)-ny, (float)-nz);
        normal.normalizeLocal();
        return normal;
	}
	
	
	public void extract(DensityVolume source) {
        BoundingBox bb = source.getEffectiveVolume();
        
        int beginX = 0;
        int beginY = 0;
        int beginZ = 0;
        int boundX = width;
        int boundY = hieght;
        int boundZ = depth;
        
        if(bb !=null && !bb.intersects(this.bb)){//early out
        	return;
        }
        
        if(bb !=null && bb.intersects(this.bb)){
        	Vector3f min = bb.getMin(new Vector3f());
        	Vector3f max = bb.getMax(new Vector3f());
        	min.maxLocal(this.bb.getMin(new Vector3f()));
        	max.minLocal(this.bb.getMax(new Vector3f()));
        	//now roll it into reletive space
        	min.subtractLocal(position);
        	max.subtractLocal(position);
        	
        	//now index space:
        	min.multLocal((float)gridM);
        	max.multLocal((float)gridM);
        	
        	beginX = (int)Math.ceil(min.x);
        	beginY = (int)Math.ceil(min.y);
        	beginZ = (int)Math.ceil(min.z);
        	
        	boundX = (int)Math.floor(max.x);
        	boundY = (int)Math.floor(max.y);
        	boundZ = (int)Math.floor(max.z);
        }
        
        for( int x = beginX; x < boundX; x++ ) {
            for( int y = beginY; y < boundY; y++ ) {
                for( int z = beginZ; z < boundZ; z++ ) {
                	float sx = (float)(position.x+x*gridScale);
                	float sy = (float)(position.y+y*gridScale);
                	float sz = (float)(position.z+z*gridScale);
                    grid[index(x,y,z)]=toClamp(source.getDensity(sx,sy,sz));
                    type[index(x,y,z)]= source.getType(sx, sy, sz);
                }
            }
        }
    }
	
	
	@Override
	public BoundingBox getEffectiveVolume() {
		return bb;
	}
	
	public Vector3f getPosition(){
		return position;
	}
	
	public boolean isDiscrete(){
		return true;
	}
	
	private float fromClamp(short s){
		return ((float)s/Short.MAX_VALUE)*clamp;
	}
	
	private short toClamp(float d){
		float toClamp = Math.min(Math.max(d, -clamp),clamp);//[-clamp,clamp]
		//now convert to [-1, 1] space
		float normal = toClamp/clamp;
		return (short) (normal*Short.MAX_VALUE);
	}
	
	
	
	private int getGridClampedCoords(float x, float y, float z){
		Vector3f  pos = new Vector3f(x,y,z);
		if(bb.intersects(pos)){
			return 0;
		}else{
			throw new RuntimeException("Out Of Bounds "+ bb);
		}
		
	}

	@Override
	public int getType(float x, float y, float z) {
		return type[getGridClampedCoords(x, y, z)];
	}


	@Override
	public void setType(float x, float y, float z,int type) {
		this.type[getGridClampedCoords(x, y, z)] = type;
	}

	@Override
	public void setDensity(float x, float y, float z, float d) {
		this.grid[getGridClampedCoords(x, y, z)] = toClamp(d);
	}
	
}
