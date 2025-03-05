package cobaJMonkey;

public class RollingTheMonkeyMain {

	/**
	 * Just providing an example of creating and launching an instance of
	 * RollingTheMonkey game.
	 * 
	 * Use w-a-s-d to control the bowling ball.
	 * Esc will exit the game.
	 */
	public static void main(String[] args) {
		var theGame = new RollingTheMonkey() ;
		theGame.start();
	}

}
