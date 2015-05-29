package crawler;

import crawler.sites.AaCrawler;
import crawler.sites.HurriyetCrawler;
import crawler.sites.MilliyetCrawler;
import data.CrawlerObject;
import lucene.Lucene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import socket.Job;

import java.util.LinkedList;
import java.util.concurrent.*;

/**
 * Created by Adem on 09/4/2015.
 */
public class CrawlerManager implements INewListener {

    private static final Logger logger = LogManager.getLogger(CrawlerManager.class);


    ScheduledExecutorService executor;
    ScheduledFuture consumerService;
    Runnable consumer;
    protected LinkedBlockingDeque<CrawlerObject> incoming = new LinkedBlockingDeque<CrawlerObject>();
    protected LinkedList<INewListener> listeners = new LinkedList<INewListener>();


    public CrawlerManager() {

        CrawlerBase.addListener(this);
        executor = Executors.newScheduledThreadPool(4);
        consumer = new Runnable() {
            @Override
            public void run() {

                logger.trace("consumer running");

                while(!incoming.isEmpty()) {
                    CrawlerObject obj = incoming.pop();

                    for (INewListener listener : listeners)
                        listener.onNew(obj);
                }




            }
        };

    }


    public void addListener(INewListener listener) {
        listeners.add(listener);
    }


    public void startSync() throws Exception {

        Thread t = new Thread(consumer);

        t.run();
        t.join();

        try {
            logger.trace("AaCrawler running...");
            AaCrawler aa = new AaCrawler(3);
            aa.execute();
            logger.trace("AaCrawler end");
        } catch (Exception e) {
            e.printStackTrace();
        }



        t.run();
        t.join();

        try {
            logger.trace("MillorreiyetCrawler running...");
            MilliyetCrawler milliyet = new MilliyetCrawler(1);
            milliyet.execute();
            logger.trace("MilliyetCrawler end");
        } catch (Exception e) {
            e.printStackTrace();
        }

        t.run();
        t.join();

        try {
            logger.trace("HurriyetCrawler running...");
            HurriyetCrawler hurriyet= new HurriyetCrawler(2);
            hurriyet.execute();
            logger.trace("HurriyetCrawler end");
        } catch (Exception e) {
            e.printStackTrace();
        }

        t.run();
        t.join();

    }


    public void start() {



        Runnable aaTask = new Runnable() {
            public void run() {
                logger.trace("AaCrawler running...");
                AaCrawler aa = new AaCrawler(3);
                aa.execute();
                logger.trace("AaCrawler end");
            }
        };

        Runnable milliyetTask = new Runnable() {
            public void run() {
                logger.trace("MillorreiyetCrawler running...");
                MilliyetCrawler milliyet = new MilliyetCrawler(1);
                milliyet.execute();
                logger.trace("MilliyetCrawler end");
            }
        };

        Runnable hurriyetTask = new Runnable() {
            public void run() {
                logger.trace("HurriyetCrawler running...");
                HurriyetCrawler hurriyet= new HurriyetCrawler(2);
                hurriyet.execute();
                logger.trace("HurriyetCrawler end");

            }
        };


        executor.scheduleAtFixedRate(aaTask, 0, 2, TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(milliyetTask, 0, 2, TimeUnit.MINUTES);
        executor.scheduleAtFixedRate(hurriyetTask, 0, 2, TimeUnit.MINUTES);

        consumerService = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(consumer,0,2,TimeUnit.SECONDS);

    }


    public void shutdown() {

        executor.shutdown();

        if(consumerService != null)
            consumerService.cancel(true);
    }


    @Override
    public void onNew(CrawlerObject obj) {
        incoming.add(obj);

    }
}
