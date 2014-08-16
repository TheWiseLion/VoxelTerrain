

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;

/***
 * For keeping tack of input states.
 * 
 * @author wiselion
 */
public class Input {
	  InputManager inputManager;
	  GSoC_DEMO gd;
	  boolean addDown = false;
	  boolean recentlyAdd =false;
	  boolean removeDown = false;
	  boolean recentlyRemove =false;
	  boolean up = false;
	  boolean down=false;
	  boolean light = false;
	  boolean toggleNormal = true;
	  
	  public Input(GSoC_DEMO gd){
		  this.inputManager = gd.getInputManager();
		  this.gd = gd;
		  initKeys();
	  }
	
	
	  /** Custom Keybinding: Map named actions to inputs. */
	  private void initKeys() {
	    //Game Controls
	    inputManager.addMapping("ShootBalls",  new KeyTrigger(KeyInput.KEY_T));
	    inputManager.addMapping("Up",  new KeyTrigger(KeyInput.KEY_UP));
	    inputManager.addMapping("Down",  new KeyTrigger(KeyInput.KEY_DOWN));
	    inputManager.addMapping("LeftMouse",  new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
	    inputManager.addMapping("RightMouse",  new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
	    //
	    
	    //Movement Stuff:
	    inputManager.addMapping("Run",  new KeyTrigger(KeyInput.KEY_LSHIFT));
	    inputManager.addMapping("space",  new KeyTrigger(KeyInput.KEY_SPACE));
	    
	    inputManager.addMapping("showMaterial", new KeyTrigger(KeyInput.KEY_P));
	    inputManager.addMapping("showQuads", new KeyTrigger(KeyInput.KEY_O));
	    inputManager.addMapping("showTriangles", new KeyTrigger(KeyInput.KEY_I));
	    
	    inputManager.addMapping("LArrow", new KeyTrigger(KeyInput.KEY_LEFT));
	    inputManager.addMapping("RArrow", new KeyTrigger(KeyInput.KEY_RIGHT));
	    
	    inputManager.addMapping("scrollUp", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));//new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true)
	    inputManager.addMapping("scrollDown",new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));//new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true)
	    
	    
	    // Add the names to the action listener.
	    inputManager.addListener(actionListener,"Up","lighting","Down","lambdaUp","lambdaDown",
	    		"edgeUp","edgeDown","ShootBalls","LeftMouse","RightMouse","Run", "scrollUp", "scrollDown", "LArrow","RArrow");
	   
	  }
	   
	   private ActionListener actionListener = new ActionListener() {
	    public void onAction(String name, boolean keyPressed, float tpf) {
	      //Jmonkeyengine time key has been pressed
	      
	      if (name.equals("ShootBalls") && !keyPressed) {
//	        gd. shootBall();
	      }
	      
	      if (name.equals("Run")) {
	    	  if( keyPressed){
	    		  gd.getFlyByCamera().setMoveSpeed(50);
	    	  }else{
	    		  gd.getFlyByCamera().setMoveSpeed(10);
	    	  }
	      }
	      
	      if (name.equals("LArrow")) {
	    	  if( keyPressed){
	    		 gd.editor.setMode(gd.editor.getMode()+1);
	    	  }
	      }
	      
	      if (name.equals("RArrow")) {
	    	  if( keyPressed){
	    		  gd.editor.setMode(gd.editor.getMode()-1);
	    	  }
	      }
	      
//	      if (name.equals("showTriangles")) {
//	    	  if( keyPressed){
//	    		 gd.changeTriangles();
//	    	  }
//	      }
	      
	      if (name.equals("scrollUp")) {
	    	  if( keyPressed){
	    		 gd.editor.setSize(gd.editor.getSize()+.25f);
	    	  }
	      }
	      
	      if (name.equals("scrollDown")) {
	    	  if( keyPressed){
	    		  gd.editor.setSize(gd.editor.getSize()-.25f);
	    	  }
	      }
	      
	      if (name.equals("LeftMouse")) {
	          
//	    	  if(keyPressed!=addDown && !addDown){
//	        	  recentlyAdd=true;
//	          }
	    	  addDown = keyPressed;
	      }
//	      
	      if (name.equals("RightMouse")) {
//	    	  if(keyPressed!=removeDown && !removeDown){
//	        	  recentlyRemove=true;
//	          }
	          removeDown = keyPressed;
	      }
	     
	      if (name.equals("Up")) { 
	    	  if(down!=keyPressed & keyPressed){
	    		  gd.selectedMaterial++;
	    		  if(gd.selectedMaterial>10){
	    			  gd.selectedMaterial = 0;
	    		  }
	    	  }
	    	  up = keyPressed;
	      }
	      
	      if (name.equals("down")) {
	    	  
	    	  if(down!=keyPressed & keyPressed){
	    		  gd.selectedMaterial--;
	    		  if(gd.selectedMaterial<0){
	    			  gd.selectedMaterial = 10;
	    		  }
	    	  }    	  
	    	  down = keyPressed;

	      }

	      
	    }
	  };
}
