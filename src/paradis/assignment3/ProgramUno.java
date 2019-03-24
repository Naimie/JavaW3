package paradis.assignment3;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/*
 * SubTask1:
 * Create a program Program1 (in package paradis.assignment3), where
 * you parallelize the execution of Program by using the producer-consumer
 * pattern and the common ForkJoinPool (thread pool). Each stage in the working
 * process should be separated by using the producer-consumer pattern. Let the
 * number of threads of the thread pool automatically be set to the number of
 * available cores on the current computer.
 */

//[You are welcome to add some import statements.]

public class ProgramUno {
    final static int NUM_WEBPAGES = 40;
    private static WebPage[] webPages = new WebPage[NUM_WEBPAGES];
    // [You are welcome to add some variables.]

    // The blocking queue
    private static BlockingQueue<Task> firstQueue = new ArrayBlockingQueue<>(40);
    private static BlockingQueue<Task> secondQueue = new ArrayBlockingQueue<>(40);
    private static BlockingQueue<Task> thirdQueue = new ArrayBlockingQueue<>(40);

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

    public static void main(String[] args) {
        // Initialize the list of webpages.
        initialize();
        ProgramUno p1 = new ProgramUno();

        // Start timing.
        long start = System.nanoTime();

        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = ForkJoinPool.commonPool();

        // Do the stuff
        for (WebPage wp : webPages) {
            executor.submit(p1.new ProdCons(null, firstQueue, p1.new Task(wp)));
        }

        for (WebPage wp : webPages) {
            executor.submit(p1.new ProdCons(firstQueue, secondQueue, null));
        }

        for (WebPage wp : webPages) {
            executor.submit(p1.new ProdCons(secondQueue, thirdQueue, null));
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        for (WebPage wp : webPages) {
            executor.submit(p1.new ProdCons(thirdQueue, null, null));
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        // Stop timing.
        long stop = System.nanoTime();

        // Present the result.

        executor.shutdown();

        presentResult();

        // Present the execution time.
        System.out.println("Execution time (seconds): " + (stop - start) / 1.0E9);
        System.out.println("Number of cores: " + cores);
    }

    class ProdCons implements Runnable {

        private BlockingQueue<Task> takeQueue;
        private BlockingQueue<Task> putQueue;
        private Task task;

        public ProdCons(BlockingQueue<Task> takeQueue, BlockingQueue<Task> putQueue, Task task) {
            this.takeQueue = takeQueue;
            this.putQueue = putQueue;
            this.task = task;
        }

        public void run() {
            if (takeQueue != null && putQueue != null) { // Prod and Cons
                try {
                    task = takeQueue.take();
                    task.execute();
                    putQueue.put(task);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (takeQueue == null) { // Prod
                try {
                    putQueue.put(task);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (putQueue == null) { // Cons
                try {
                    task = takeQueue.take();
                    task.execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Task {
        private WebPage page;
        private int taskToDo; // 0 = download, 1 = analyze, 2 = categorize

        public int getTaskToDo() {
            return taskToDo;
        }

        public Task(WebPage page) {
            this.page = page;
            this.taskToDo = 0;
        }

        public void execute() {
            switch (taskToDo) {
                case 0:
                    page.download();
                    increaseTask();
                    // System.out.println(page + " did download");
                    break;
                case 1:
                    page.analyze();
                    increaseTask();
                    // System.out.println(page + " did analyze");
                    break;
                case 2:
                    page.categorize();
                    // System.out.println(page + " did categorize");
                    break;
            }

        }

        private void downloadPage() {
            page.download();
        }

        private void analyzePage() {
            page.analyze();
        }

        private void categorizePage() {
            page.categorize();
        }

        private void increaseTask() {
            taskToDo++;
            // System.out.println("taskToDo is now: " + taskToDo);

        }

        public WebPage getPage() {
            return page;
        }

    }

}
