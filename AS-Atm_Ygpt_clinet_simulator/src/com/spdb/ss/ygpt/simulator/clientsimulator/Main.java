/**   
* @Title: Main.java 
* @Package com.spdb.ss.ygpt.simulator.clientsimulator 
* @Description: TODO(用一句话描述该文件做什么) 
* @author Hao Wei  
* @date 2016年5月19日 下午1:20:23 
* @version V1.0   
*/

package com.spdb.ss.ygpt.simulator.clientsimulator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: Main
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author Hao Wei
 * @date 2016年5月19日 下午1:20:23
 * 
 */
public class Main {
	public static String serverIP;// 服务器地址
	public static int serverPort;// 服务器IP
	public static int THREAD_COUNT;// 配置THREAD_COUNT个线程
	public static int ATMS_PER_THREAD;// 每个线程模拟ATMS_PER_THREAD台atm
	public static int INTERVAL_MS;// 配置每个线程600毫秒发一次包，相当于每个atm60秒发一次包
	public static String bufferLogPath;// bufferlog文件的存放目录

	public static void loadConfig() {
		Properties pro = new Properties();
		try {
			InputStream in = new FileInputStream("config.properties");
			pro.load(in);
			serverIP = pro.getProperty("HOST");
			serverPort = Integer.parseInt(pro.getProperty("PORT"));
			THREAD_COUNT = Integer.parseInt(pro.getProperty("THREAD_COUNT"));
			ATMS_PER_THREAD = Integer.parseInt(pro.getProperty("ATMS_PER_THREAD"));
			INTERVAL_MS = Integer.parseInt(pro.getProperty("INTERVAL_MS"));
			bufferLogPath = pro.getProperty("bufferLogPath");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * @Title: main @Description: TODO(这里用一句话描述这个方法的作用) @param @param args
	 * 设定文件 @return void 返回类型 @throws
	 */

	public static void main(String[] args) {
		loadConfig();
		// 定义单例队列
		ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(20000, true);

		AtomicInteger counter = new AtomicInteger();

		// 启动生产者，单线程读文件写入队列
		Reader reader = new Reader(queue, bufferLogPath);
		Executors.newSingleThreadExecutor().execute(reader);

		int iStartAtmNo = 90000000;// 模拟atmno的起始编号，每个线程（sender）用100个号
		// 启动消费者，多线程从队列中取数发送到服务器
		ScheduledThreadPoolExecutor stpeList[] = new ScheduledThreadPoolExecutor[THREAD_COUNT];
		for (ScheduledThreadPoolExecutor stpe : stpeList) {
			stpe = new ScheduledThreadPoolExecutor(1);
			Sender sender = new Sender(queue, serverIP, serverPort, iStartAtmNo, counter);
			iStartAtmNo += 100; // 模拟atmno的起始编号，每个线程（sender）用100个号
			stpe.scheduleAtFixedRate(sender, new Random().nextInt(INTERVAL_MS), INTERVAL_MS, TimeUnit.MILLISECONDS);
		}

		// 定时检查Queue状态
		ScheduledThreadPoolExecutor monitor = new ScheduledThreadPoolExecutor(1);
		monitor.scheduleAtFixedRate(new QueueMonitor(queue, counter), 0, 1, TimeUnit.SECONDS);
	}

}
