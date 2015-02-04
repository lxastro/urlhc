package xlong.util;

public class RunningTime {
	private static long totalMili = 0;
	private static long startMili = -1;
	
	public static void start() {
		totalMili = 0;
		startMili = System.currentTimeMillis();
	}
	public static void stop() {
		if (startMili != -1) {
			totalMili += System.currentTimeMillis() - startMili;
			startMili = -1;
		}
	}
	public static void resume() {
		if (startMili == -1) {
			startMili = System.currentTimeMillis();
		}		
	}
	public static double get() {
		if (startMili != -1) {
			return (totalMili + System.currentTimeMillis() - startMili)/1000.0;
		} else {
			return (totalMili)/1000.0;
		}
	}
	public static void main(String[] args) throws InterruptedException {
		RunningTime.start();
		Thread.sleep(1000);
		RunningTime.stop();
		System.out.println(RunningTime.get());
		
		RunningTime.start();
		Thread.sleep(1000);
		RunningTime.stop();
		System.out.println(RunningTime.get());
		RunningTime.resume();
		Thread.sleep(1000);
		RunningTime.stop();
		System.out.println(RunningTime.get());	
	}
	
}
