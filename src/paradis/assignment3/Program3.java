package paradis.assignment3;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;


// Kristina Elmgren

// [Do necessary modifications of this file.]
// [You are welcome to add some import statements.]

// why you no work

        import java.util.concurrent.ArrayBlockingQueue;
        import java.util.concurrent.BlockingQueue;
        import java.util.concurrent.ForkJoinPool;
public class Program3 {

    final static int NUM_WEBPAGES = 40;
    private static WebPage[] webPages = new WebPage[NUM_WEBPAGES];
    private static BlockingQueue<Task> queue = new ArrayBlockingQueue<>(120);


    private static ForkJoinPool threads = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

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


        //execturor service

        for( WebPage page : webPages){
            threads.submit(()->{
                try{
                    queue.put(new Task(page,0));

                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            });
        }

     while(true){
            threads.submit(()->{
                try{

                    Task task = queue.take();
                    task.execute();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            });
            if(!true){
                break;
            }

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
        new Program3().run();
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
                    subTaskStep++;
                    try{
                        queue.put(this);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    webPage.analyze();
                    subTaskStep++;
                    try{
                        queue.put(this);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
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
