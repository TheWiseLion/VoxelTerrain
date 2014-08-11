
import java.util.ArrayList;
import java.util.List;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class Cube extends Mesh {

    public Cube(int num, float lineDist){
    	float lineLength = 5;
//      FloatBuffer fpb = BufferUtils.createFloatBuffer(6 * lineCount);
//      ShortBuffer sib = BufferUtils.createShortBuffer(2 * lineCount);
    	List<Vector3f> lines = new ArrayList<Vector3f>();
    	float r = 5;
    	for(float z = -r; z<=r ; z+=lineDist){
//	    	float zOff = z*lineDist;
	    	
	    	
	    	for(float y = -r; y <= r; y+= lineDist){
	    		
	    		for(float x = -r; x <= r; x+= lineDist){
	    			
		    		lines.add(new Vector3f(x,-lineLength,z));
		    		lines.add(new Vector3f(x,lineLength,z));
		    		
		    		lines.add(new Vector3f(x,-y,-lineLength));
		    		lines.add(new Vector3f(x,-y,lineLength));
		    	}
    		
	    		
    			
    			lines.add(new Vector3f(-lineLength,y,z));
	    		lines.add(new Vector3f(lineLength,y,z));
    		}
    		
    		
    		
    	}

       

//        fpb.flip();
//        sib.flip();
//
        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(lines.toArray(new Vector3f[0])));
//        setBuffer(Type.Index, 2, sib);
//        
        setMode(Mode.Lines);

        updateBound();
        updateCounts();
    }
}