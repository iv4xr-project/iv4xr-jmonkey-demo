package cobaJMonkey;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

import nl.uu.cs.aplib.agents.State;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.BasicAgent;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.mainConcepts.Tactic;

import static nl.uu.cs.aplib.AplibEDSL. * ;

public class HelloGame extends SimpleApplication {

	Geometry player;
	Geometry target ;
	BulletAppState bulletAppState;
	BasicAgent aplibTestAgent ;
	GoalStructure currentTestGoal ;
	
	@Override
    public void simpleInitApp() {
		
		bulletAppState = new BulletAppState() ;
		stateManager.attach(bulletAppState);
		
        Box b = new Box(0.5f, 0.5f, 0.5f); // create cube shape
        var s = new Sphere(16,16,0.5f) ;
        
        player = new Geometry("player", b);  // create cube geometry from the shape
        target = new Geometry("target",s) ;
        Material mat = new Material(assetManager,
          "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        Material mat2 = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");  
        mat.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
        mat2.setColor("Color", ColorRGBA.Red) ;
        player.setMaterial(mat);                   // set the cube's material
        target.setMaterial(mat2) ;
        target.setLocalTranslation(new Vector3f(0,3,0)) ;
        player.setLocalTranslation(new Vector3f(0,0,0)) ;
        var player_phy = new RigidBodyControl(0f) ; // mass 0
        player.addControl(player_phy) ;
        var target_phy = new RigidBodyControl(0f) ; 
        
        target.addControl(target_phy) ;
        
        rootNode.attachChild(player);              // make the cube appear in the scene
        rootNode.attachChild(target) ;
        
        bulletAppState.getPhysicsSpace().add(player_phy) ;
        bulletAppState.getPhysicsSpace().add(target_phy) ;
        
        
        inputManager.addMapping("UP",   new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(upListener,"UP") ;
        
        
    }
	
	
	AnalogListener upListener = new AnalogListener() {
		
		@Override
	    public void onAnalog(String name, float value, float tpf) {
			
			if (name.equals("UP")) {
				//RigidBodyControl rbc = player.getControl(RigidBodyControl.class) ;
				player.move(new Vector3f(0,value,0)) ;
			}
			
		} 
		
	} ;
	
	/* Use the main event loop to trigger repeating actions. */
    @Override
    public void simpleUpdate(float tpf) {
        // make the player rotate:
        player.rotate(0, 2*tpf, 0); 
        if (aplibTestAgent != null && currentTestGoal != null) {
        	if (currentTestGoal.getStatus().inProgress()) {
        		aplibTestAgent.update();
        	}
        	else {
        		this.stop();
        		System.out.println(">>>> goal status: " + currentTestGoal.getStatus()) ;
        	}
        }
       
    }
    
    
    static class MyDummyEnvironment extends Environment {
    	@Override
    	public Object observe(String agentId) {
    		return 1 ;
    	}
    }
    

    
    static Tactic moveUp(HelloGame theGame) {
    	return action("moveUp").do1(S -> {
    		theGame.player.move(new Vector3f(0,0.1f,0)) ;
    		return 1 ;
    	}).lift() ;
    }
    
    static GoalStructure atTarget(HelloGame theGame) {
    	 return goal("atTarget")
    	 .toSolve(dummy -> {
    		 var p1 = theGame.player.getLocalTranslation() ;
    		 var p2 = theGame.target.getLocalTranslation() ;
    		 return p1.distance(p2) <= 0.1f ;
    	 })
    	 .withTactic(moveUp(theGame)) 
    	 .lift() ;
    }
      
    
	public static void main(String[] args){
		HelloGame app = new HelloGame();
		app.aplibTestAgent = new BasicAgent("testHaha", "tester") ;
		app.aplibTestAgent.attachState(new SimpleState()) ;
		app.aplibTestAgent.attachEnvironment(new MyDummyEnvironment()) ;
		app.currentTestGoal = atTarget(app) ;
		app.aplibTestAgent.setGoal(app.currentTestGoal) ;
        app.start(); // start the game        
    }
}
