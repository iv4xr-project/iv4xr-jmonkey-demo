package cobaJMonkey;

import static nl.uu.cs.aplib.AplibEDSL.* ;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

import nl.uu.cs.aplib.mainConcepts.BasicAgent;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.SimpleState;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

/**
 * A simple demo of testing a JME game with aplib. The game is
 * RollingTheMonkey.
 *
 */
public class Test_RollingTheMonkey {
	
	static float EPSILON = 0.001f ;
	
	
	/**
	 * Creating an extension of the RollingTheMonkey game. We keep track here
	 * the executing test-agent and the test-goal given to it.
	 */
	static class SUT extends RollingTheMonkey {
		
		BasicAgent aplibTestAgent ;
		GoalStructure currentTestGoal ;
		
		/**
		 * Override the original game frame-update method, so that the game is stopped
		 * when the current test-goal is achieved. 
		 */
		@Override
	    public void simpleUpdate(float tpf) {
			super.simpleUpdate(tpf);
			if (currentTestGoal.getStatus().inProgress()) {
	        		aplibTestAgent.update();
	        }
	        else {
	        	// stop the game when the current goal is achieved.
	        	// When stopped, the game state won't change anymore.
	        	this.stop();
	        	System.out.println(">>>> goal status: " + currentTestGoal.getStatus()) ;
	        }
		}
		
	}
	
	/**
	 * Environment is an interface that connects the test-agent to the 
	 * SUT. As we have access to RollingTheMonkey's state and top-level 
	 * interaction actions, we don't actually need this interface. However,
	 * the agent needs one, and we should at least define its method observe().
	 * So below we just give a dummy implementation of Environment.
	 */
	static class MyDummyEnvironment extends Environment {
    	@Override
    	public Object observe(String agentId) {
    		return 1 ;
    	}
    }
	
	
   /**
    * Get the Spatial representing an in-game box object, with the specified
    * box-nr.
    */
   static Spatial getBox(SUT theGame, int boxNr) {
	   for (var B : theGame.pickUps.getChildren()) {
		   var id = B.getName() ;
		   if (id.endsWith("" + boxNr)) {
			   return B ;
		   }
	   }
	   return null ;
   }
   
   /**
    * At the game start, a bowling-ball is dropped on to the game's playing surface.
    * This goal is solved if the bowling-ball (the player) is on the ground.
    * No special action is needed, we only need to wait until the ball is on the 
    * ground.
    */
   static GoalStructure onGround(SUT theGame) {
	   	 return goal("monkey is on the ground")
	   	 .toSolve(dummy -> Math.abs(theGame.player.getPhysicsLocation().y - RollingTheMonkey.PLAYER_RADIUS) 
	   			           <= 
	   			           EPSILON )
	   	 .withTactic(action("do-nothing").do1(dummy -> 1).lift()) 
	   	 .lift() ;
	   }
		
	
   /**
    * A goal-structure for picking up an in-game box with nr k.
    */
   static GoalStructure pickUpBox(SUT theGame, int k) {
		 
		var collisionDistance = RollingTheMonkey.PLAYER_RADIUS + RollingTheMonkey.PICKUP_SIZE ;
		
		Vector3f directionToMove = new Vector3f(0,0,0) ;
		 
		return goal("Box-" + k + " is picked")
			.toSolve(dummy -> {
				var targetBox = getBox(theGame,k) ;
				var pos = targetBox.getLocalTranslation() ;
				var playerPos = theGame.player.getPhysicsLocation() ;
				
				System.out.println("## " + targetBox.getName() + " @" + pos) ;
				System.out.println("## monkey @" + playerPos) ;
				System.out.println("## dist = " + pos.distance(playerPos)) ;
				
				if (pos.distance(playerPos) <= collisionDistance) {
					theGame.player.setLinearVelocity(new Vector3f(0,0,0));
					System.out.println(">>> PICK UP " + targetBox.getName()) ;
					return true ;
				}
				return false ;
			})
			.withTactic(action("moveTo box-" + k)
					.do1(dummy -> {
						var targetBox = getBox(theGame,k) ;
						if (directionToMove.x == 0 && directionToMove.z == 0) {
							var pos = targetBox.getLocalTranslation() ;
							var playerPos = theGame.player.getPhysicsLocation() ;
							var dir = pos.add(playerPos.negate()) ;
							dir.y = 0 ;
							dir.normalize() ;
							dir.multLocal(0.3f);  
							directionToMove.x = dir.x ;
							directionToMove.y = dir.y ;
							directionToMove.z = dir.z ;
							theGame.player.setLinearVelocity(directionToMove);
						}
						return 1 ;
					})
					.lift()
					)
				 .lift() ;
	}
   
	/*
	public static void main(String[] args){
        var theGame = new SUT();
        theGame.aplibTestAgent = new BasicAgent("testHaha", "tester") ;
        theGame.aplibTestAgent.attachState(new SimpleState()) ;
        theGame.aplibTestAgent.attachEnvironment(new MyDummyEnvironment()) ;
        theGame.currentTestGoal = SEQ(onGround(theGame), pickUpBox(theGame,3)) ;
        theGame.aplibTestAgent.setGoal(theGame.currentTestGoal) ;
        theGame.start();
    }
    */
	
	AppSettings mkSettings() {
		AppSettings gameSettings = new AppSettings(false);
		gameSettings.setResolution(640,480);
		gameSettings.setFullscreen(false);
		gameSettings.setVSync(false);
		gameSettings.setTitle("Rolling the Monkey");
		gameSettings.setUseInput(true);
		gameSettings.setFrameRate(50);
		gameSettings.setSamples(0);
		gameSettings.setRenderer("LWJGL-OpenGL2");
		return gameSettings ;
	}
	
	/**
	 * This test will steer the bowling ball to hit and pick up box nr 3.
	 * It checks:
	 *    (1) initially there should be 16 boxes in the game, and the score is 0.
	 *    (2) when box-3 is hit, it is indeed picked (so, removed from the game)
	 *        and the score is increased to 1.
	 */
	@Test
	public void test_SUT_exampeleUsingAplib() throws InterruptedException{
        var theGame = new SUT();
        
        theGame.aplibTestAgent = new BasicAgent("testHaha", "tester") ;
        theGame.aplibTestAgent.attachState(new SimpleState()) ;
        theGame.aplibTestAgent.attachEnvironment(new MyDummyEnvironment()) ;
        
        theGame.currentTestGoal = SEQ(
        		lift("check starting state", 
        				action("checking starting state")
        				.do1(dummy -> { 
        					assertTrue(theGame.pickUps.getChildren().size() == 16) ;
        			   	    assertTrue(theGame.score == 0) ; 
        			   	    return 1 ; } )
        			),
        		
        		onGround(theGame), 
        		pickUpBox(theGame,3),
        		
        		lift("check end state", 
        				action("checking end state")
        				.do1(dummy -> { 
        					assertTrue(theGame.pickUps.getChildren().size() == 15) ;
        		    	    assertTrue(theGame.score == 1) ; 
        		    	    return 1 ; } )
        			)) ;
        
        theGame.aplibTestAgent.setGoal(theGame.currentTestGoal) ;
        theGame.setSettings(mkSettings());
        theGame.setShowSettings(false);
        theGame.start();
        Thread.sleep(8000);
        assertTrue(theGame.currentTestGoal.getStatus().success()) ;
    }
}
