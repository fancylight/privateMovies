package com.light.privateMovies.reptile.core.newCore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.BlockingQueue;

/**
 * 使用阻塞队列,此类表示创建连接的线程
 */
public class ConnectionBlock implements Runnable {
    //连接线程消耗,由任务处理线程创建
    private BlockingQueue<ConnectionTarget> connectionTargets;
    //任务目标,由连接线程创建
    private BlockingQueue<TaskTarget> taskTargets;
    private boolean isTer;
    private Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080));
    private long intervalTime = 1000;
    Logger logger = LogManager.getLogger(ConnectionBlock.class);

    public ConnectionBlock(BlockingQueue<ConnectionTarget> connectionTargets, BlockingQueue<TaskTarget> taskTargets) {
        this.connectionTargets = connectionTargets;
        this.taskTargets = taskTargets;
    }

    @Override
    public void run() {
        //通过线程中断阻塞来退出线程
        try {
            while (!isTer) {
                System.out.println("connect线程");
                var target = connectionTargets.take();
                if (target.isPoison())
                {
                    System.out.println("毒药退出线程"+Thread.currentThread().getName());
                    break;
                }

                //此处有可能出现连接失败,设置一个重新连接次数
                Connection.Response response=null;
                try {
                    var connection = Jsoup.connect(target.getUrl()).proxy(proxy);
                    response = connection.method(Connection.Method.GET).ignoreContentType(true).ignoreHttpErrors(true).execute();

                } catch (IOException e) {
                    if (target.getReConnectionTimes()>0){
                        logger.warn(target.getUrl()+"连接失败,重新加入队列");
                        connectionTargets.offer(target);
                    }
                    else {
                        logger.warn(target.getUrl()+"连接次数过多,被抛弃");
                    }
                }
                if (response != null)

                    taskTargets.put(new TaskTarget(response, target.getTaskType(), target.getUrl(),target.getDeep()));
            }
        }catch (InterruptedException e ){
            logger.warn(Thread.currentThread().getName()+"中断");
        }

    }
}
