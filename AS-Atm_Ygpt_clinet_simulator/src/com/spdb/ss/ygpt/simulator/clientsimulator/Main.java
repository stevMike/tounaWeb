/**   
* @Title: Main.java 
* @Package com.spdb.ss.ygpt.simulator.clientsimulator 
* @Description: TODO(��һ�仰�������ļ���ʲô) 
* @author Hao Wei  
* @date 2016��5��19�� ����1:20:23 
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
 * @Description: TODO(������һ�仰��������������)
 * @author Hao Wei
 * @date 2016��5��19�� ����1:20:23
 * 
 */
public class Main {
	public static String serverIP;// ��������ַ
	public static int serverPort;// ������IP
	public static int THREAD_COUNT;// ����THREAD_COUNT���߳�
	public static int ATMS_PER_THREAD;// ÿ���߳�ģ��ATMS_PER_THREAD̨atm
	public static int INTERVAL_MS;// ����ÿ���߳�600���뷢һ�ΰ����൱��ÿ��atm60�뷢һ�ΰ�
	public static String bufferLogPath;// bufferlog�ļ��Ĵ��Ŀ¼

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
	 * @Title: main @Description: TODO(������һ�仰�����������������) @param @param args
	 * �趨�ļ� @return void �������� @throws
	 */

	public static void main(String[] args) {
		loadConfig();
		// ���嵥������
		ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(20000, true);

		AtomicInteger counter = new AtomicInteger();

		// ���������ߣ����̶߳��ļ�д�����
		Reader reader = new Reader(queue, bufferLogPath);
		Executors.newSingleThreadExecutor().execute(reader);

		int iStartAtmNo = 90000000;// ģ��atmno����ʼ��ţ�ÿ���̣߳�sender����100����
		// ���������ߣ����̴߳Ӷ�����ȡ�����͵�������
		ScheduledThreadPoolExecutor stpeList[] = new ScheduledThreadPoolExecutor[THREAD_COUNT];
		for (ScheduledThreadPoolExecutor stpe : stpeList) {
			stpe = new ScheduledThreadPoolExecutor(1);
			Sender sender = new Sender(queue, serverIP, serverPort, iStartAtmNo, counter);
			iStartAtmNo += 100; // ģ��atmno����ʼ��ţ�ÿ���̣߳�sender����100����
			stpe.scheduleAtFixedRate(sender, new Random().nextInt(INTERVAL_MS), INTERVAL_MS, TimeUnit.MILLISECONDS);
		}

		// ��ʱ���Queue״̬
		ScheduledThreadPoolExecutor monitor = new ScheduledThreadPoolExecutor(1);
		monitor.scheduleAtFixedRate(new QueueMonitor(queue, counter), 0, 1, TimeUnit.SECONDS);
	}

}
