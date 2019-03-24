// Kristina Elmgren

// [Do necessary modifications of this file.]
package paradis.assignment3;
// [You are welcome to add some import statements.]

// why you no work

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;

public class Program1 {
	final static int NUM_WEBPAGES = 40;
	private static WebPage[] webPages = new WebPage[NUM_WEBPAGES];

	// [You are welcome to add some variables.]

	// [You are welcome to modify this method, but it should NOT be parallelized.]
	private static void initialize() {
		for (int i = 0; i < NUM_WEBPAGES; i++) {
			webPages[i] = new WebPage("http://www.site.se/page" + i + ".html");
		}
	}
	
	// [You are welcome to modify this method, but it should NOT be parallelized.]
	private static void presentResult() {
		for (int i = 0; i < NUM_WEBPAGES; i++) {
			System.out.println(webPages[i]);
		}
	}

	void run(){
		// Initialize the list of webpages.
		initialize();


		// Start timing.
		long start = System.nanoTime();
		BlockingQueue<Task>  downloadQueue = new ArrayBlockingQueue<>(NUM_WEBPAGES);
		BlockingQueue<Task>  analyzeQueue = new ArrayBlockingQueue<>(NUM_WEBPAGES);
		BlockingQueue<Task>  categorizeQueue = new ArrayBlockingQueue<>(NUM_WEBPAGES);

		ForkJoinPool threads = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

		//execturor service

		for( WebPage page : webPages){
			threads.submit(()->{
				try{
					System.out.println("Putting " + page + " in queue");
					downloadQueue.put(new Task(page,0));

				}catch(InterruptedException e){
					e.printStackTrace();
				}
			});
		}

		try{
			Thread.sleep(500);
		}catch (InterruptedException e){
			e.printStackTrace();
		}
		while (downloadQueue.remainingCapacity() != 40){
			threads.submit(()->{
				try{
					System.out.println("Take download task");
					Task task = downloadQueue.take();
					task.execute();
					task.setSubTaskStep(1);
					analyzeQueue.put(task);
					System.out.println(task.getWebPage());
					System.out.println(downloadQueue.remainingCapacity());
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			});
			}


		while (analyzeQueue.remainingCapacity() != 40){
			//System.out.println("inne i analyse loop");
				threads.submit(()-> {
							try {
								System.out.println("Take analyse task");
								Task task = analyzeQueue.take();
								task.execute();
								task.setSubTaskStep(2);
								categorizeQueue.put(task);
								System.out.println(task.getWebPage());
								System.out.println(analyzeQueue.remainingCapacity());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						});
				//System.out.println("efter analyse loop");
		}
		while (categorizeQueue.remainingCapacity() != 40){
			//System.out.println("innne i cat loop");
			threads.submit(()-> {
				try {
					System.out.println("Take categorize task");
					Task task = categorizeQueue.take();
					task.execute();
					System.out.println(task.getWebPage());
					System.out.println(categorizeQueue.remainingCapacity());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});

		}



		/*
		// [Do modify this sequential part of the program.]
		for (int i = 0; i < NUM_WEBPAGES; i++) {
			//Task task = new Task(webPages[i], 0);
		}
		//webPages[i].download();
		for (int i = 0; i < NUM_WEBPAGES; i++)
			webPages[i].analyze();
		for (int i = 0; i < NUM_WEBPAGES; i++)
			webPages[i].categorize(); */

		// Stop timing.
		long stop = System.nanoTime();

		// Present the result.
		presentResult();

		// Present the execution time.
		System.out.println("Execution time (seconds): " + (stop - start) / 1.0E9);
	}

	
	public static void main(String[] args) {
		new Program1().run();
	}


	class Task{
		private WebPage webPage;
		private int subTaskStep;

		Task(WebPage webPage, int subTaskSetp){
			this.webPage = webPage;
			this.subTaskStep = subTaskStep;
		}
		void execute(){
			switch(subTaskStep){
				case 0:
					webPage.download();
				break;
				case 1:
					webPage.analyze();
				break;
				case 2:
					webPage.categorize();
				break;
			}
		}
		public void setSubTaskStep(int newNR){
			this.subTaskStep = newNR;
		}
		public WebPage getWebPage(){
			return webPage;
		}

	}

}
