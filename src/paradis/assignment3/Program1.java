// Kristina Elmgren

// [Do necessary modifications of this file.]
package paradis.assignment3;
// [You are welcome to add some import statements.]

// why you no work

import java.util.concurrent.*;

public class Program1 {
    final static int NUM_WEBPAGES = 40;
    private static WebPage[] webPages = new WebPage[NUM_WEBPAGES];
    private static BlockingQueue<Task> downloadQueue = new ArrayBlockingQueue<>(NUM_WEBPAGES);
    private static BlockingQueue<Task> analyzeQueue = new ArrayBlockingQueue<>(NUM_WEBPAGES);
    private static BlockingQueue<Task> categorizeQueue = new ArrayBlockingQueue<>(NUM_WEBPAGES);
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

    void run() {
        // Initialize the list of webpages.
        initialize();

        // Start timing.
        long start = System.nanoTime();

        //ExecutorService using a ForkJoinPool using threads equal to the number of cores
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


        for (WebPage page : webPages) {
            //submits tasks to the thread pool and creates the tasks needed to dealt with
            executor.submit(new ProducerConsumer(new Task(page)));
        }

        executor.shutdown();

        //waits for att the threads to finish
        try {
            executor.awaitTermination(4, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    //Runnable class that produces or consumed depending on the task and what has previously been done
    class ProducerConsumer implements Runnable {
        private Task task;


        public ProducerConsumer(Task task) {
            this.task = task;
        }

        //this run-method will first put all the tasks in the downloadQueue then consume them all
        //when the download queue is done the analyzeQueue gets processed and then the catagorizeQueue
        //this could be done in a nested fashion where 4 threads at a time complete all 4 steps before they get reused and start on the first step again
        @Override
        public void run() {

            //sends tasks to be produced
            if (task.getSubTaskStep() == 0) {
                try {
                    BlockingQueue<Task> nextQueue = downloadQueue;
                    //System.out.println("New task" + task);
                    produce(task, nextQueue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //sends tasks from the downloadQueue to be consumed
            while (downloadQueue.remainingCapacity() != 40) {
                try {
                    BlockingQueue<Task> nextQueue = analyzeQueue;
                    // System.out.println("Taking from download");
                    Task task = downloadQueue.take();
                    consume(task, nextQueue);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //sends tasks from the analyzeQueue to be consumed
            while (analyzeQueue.remainingCapacity() != 40) {
                try {
                    BlockingQueue<Task> nextQueue = categorizeQueue;
                    // System.out.println("Taking from analyze");
                    Task task = analyzeQueue.take();
                    consume(task, nextQueue);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }//sends tasks from the catagorizeQueue to be consumed
            while (categorizeQueue.remainingCapacity() != 40) {
                try {
                    BlockingQueue<Task> nextQueue = null;
                    //  System.out.println("Taking from catagorize");
                    Task task = categorizeQueue.take();
                    consume(task, nextQueue);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //Puts tasks in the BlockingQueue
        void produce(Task task, BlockingQueue<Task> nextQueue) {
            if (nextQueue == downloadQueue) {
                try {
                    // System.out.println("Putting in download" + task);
                    downloadQueue.put(task);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (nextQueue == analyzeQueue) {
                try {
                    // System.out.println("Producing " + task);
                    analyzeQueue.put(task);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    // System.out.println("Producing " + task);
                    categorizeQueue.put(task);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //Takes tasks from the Blocking Queue and if appropriate calls on the produce method
        void consume(Task task, BlockingQueue<Task> nextQueue) {
            if (nextQueue != null) {
                try {
                    // System.out.println("Consuming " + task);
                    task.execute();
                    produce(task, nextQueue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    //System.out.println("Consuming " + task);
                    task.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //tasks to be put and taken out of BlockingQueues
    class Task {
        private WebPage webPage;
        private int subTaskStep;

        Task(WebPage webPage) {
            this.webPage = webPage;
            this.subTaskStep = 0;
        }

        void execute() {
            switch (subTaskStep) {
                case 0:
                    webPage.download();
                    incrementSubTaskStep();
                    break;
                case 1:
                    webPage.analyze();
                    incrementSubTaskStep();
                    break;
                case 2:
                    webPage.categorize();
                    break;
            }
        }

        public int getSubTaskStep() {
            return subTaskStep;
        }

        public void incrementSubTaskStep() {
            subTaskStep++;
        }

        public WebPage getWebPage() {
            return webPage;
        }

    }

}
