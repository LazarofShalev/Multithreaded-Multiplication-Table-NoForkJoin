import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultiplicationTableNoForkJoin {

	final static int NUM = 10;

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		int np = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of Available Processors: " + np);

		int[] numArray1 = new int[NUM];
		for (int i = 0; i < numArray1.length; i++) {
			numArray1[i] = i + 1;
		}

		int[] numArray2 = new int[NUM];
		for (int i = 0; i < numArray2.length; i++) {
			numArray2[i] = i + 1;
		}

		for (int numOfThreads = 1; numOfThreads <= np; numOfThreads++) {

			long startTime = System.currentTimeMillis();
			int[][] resultMatrix = parallel(numArray1, numArray2, numOfThreads);
			long endTime = System.currentTimeMillis();

			System.out.println("\nTime with " + numOfThreads + " threads: " + (endTime - startTime) + "  ms.");

			if (NUM <= 10) {
				System.out.println("\nTable multiplication is ");
				printResult(resultMatrix);
			}
		}
	}

	public static void printResult(int[][] resultMatrix) {
		for (int[] row : resultMatrix) {
			for (int i : row) {
				System.out.print(i);
				System.out.print("\t");
			}
			System.out.println();
		}
	}

	public static int[][] parallel(int[] numArray1, int[] numArray2, int numOfThreads) {
		int[][] resultMatrix = new int[numArray1.length][numArray2.length];

		ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);

		int numberOfElementsInEachThread = numArray1.length / numOfThreads;

		int i = 0;
		for (; i < numOfThreads - 1; i++) {
			executor.execute(new Task(i * numberOfElementsInEachThread, (i + 1) * numberOfElementsInEachThread,
					numArray1, numArray2, resultMatrix));
		}
		executor.execute(
				new Task(i * numberOfElementsInEachThread, numArray1.length, numArray1, numArray2, resultMatrix));

		executor.shutdown();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		return resultMatrix;
	}

}

class Task implements Runnable {
	private int start;
	private int end;

	private int[] numArray1;
	private int[] numArray2;
	private int[][] resultMatrix;

	private static Lock lock = new ReentrantLock();

	Task(int start, int end, int[] numArray1, int[] numArray2, int[][] resultMatrix) {
		this.start = start;
		this.end = end;

		this.numArray1 = numArray1;
		this.numArray2 = numArray2;
		this.resultMatrix = resultMatrix;
	}

	public void run() {
		lock.lock();
		try {

			for (int i = start; i < end; i++) {
				for (int j = 0; j < resultMatrix.length; j++) {
					for (int k = 0; k < resultMatrix[0].length; k++) {
						resultMatrix[i][j] = numArray1[i] * numArray2[j];
					}
				}
			}

		} finally {
			lock.unlock();
		}
	}
}
