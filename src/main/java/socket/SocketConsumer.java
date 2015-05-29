package socket;

/**
 * Created by Adem on 13/4/2015.
 */
public class SocketConsumer implements Runnable {
    Thread t;
    boolean isPaused;
    boolean isStopped;
    SocketHelper helper;
    final String threadName = "QueueConsumer";

    public SocketConsumer(SocketHelper helper) {
        this.helper=helper;
        t = new Thread(this, threadName);
    }

    public void start() {
        isStopped=false;
        isPaused=false;
        t.start();
    }

    synchronized public void pause() {
        isPaused=true;
        notify();
    }

    synchronized public void resume() {
        isPaused=false;
        notify();
    }

    synchronized public void stop() {
        isStopped=true;
        notify();
    }

    @Override
    public void run() {

        try {

            while(true) {

                synchronized (this) {
                    if(isPaused)
                        wait();

                    if(isStopped)
                        return;
                }

                while(!helper.incoming.isEmpty()) {
                    Job job = helper.incoming.pop();
                    helper.socket.emit(job.key, job.obj);
                }
                t.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
