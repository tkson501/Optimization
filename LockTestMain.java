import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class LockTestMain {

	//Read Write Lock 
	private static final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock(); 

	private static final Condition rwCondition = rwlock.writeLock().newCondition();

	public static void main(String[] args) throws Exception {

		if(args.length != 4) {

			Print.printMsg("Usage : java LockTestMain [coreCnt] [maxCnt] [keepalive] [queueCnt]");
			return;

		}

		//Check input value
		int coreCnt = Integer.parseInt(args[0]);	
		int maxCnt = Integer.parseInt(args[1]);	
		long keepalive = Long.parseLong(args[2]);	
		int queueCnt = Integer.parseInt(args[3]);	

		//Input 
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

		LinkedBlockingQueue<Runnable> workQueue = null;
		if(queueCnt == 0) {
			workQueue = new LinkedBlockingQueue<>();
		} else {
			workQueue = new LinkedBlockingQueue<>(queueCnt);
		}
	
		//Make pool of thread
		ThreadPoolExecutor exePool = new ThreadPoolExecutor(coreCnt, maxCnt, keepalive, TimeUnit.SECONDS, workQueue); 	

		Print.printMsg("Enter any key for starting test !!!");

		while(true) {

			Print.printMsg("Enter command !! [QUIT][INFO][RUN]");
			String tname = (input.readLine()).toUpperCase();

			if(tname.equals("QUIT")) break;
			else if(tname.equals("INFO")) {
				printPoolInfo(exePool,workQueue);
				continue;
			}

			Print.printMsg("Enter name of thread !!!");
			tname = (input.readLine()).toUpperCase();
			Print.printMsg("Enter time(second) of sleep  !!!");
			long sleepTime = Long.parseLong(input.readLine());

				
			Print.printMsg("Enter kind of test (read/write/try) !!!");
			String kind = (input.readLine()).toUpperCase();

			Runnable tsk = null;
			
			if(kind.equals("READ")) {
				tsk = new ReadLock(tname,sleepTime);
			} else if(kind.equals("WRITE")) {
				tsk = new WriteLock(tname,sleepTime);
			} else if(kind.equals("TRY")) {
				tsk = new TryLock(tname,sleepTime);
			}

			try {
				exePool.execute(tsk);
			} catch (Exception ex) {
				Print.printMsg(ex.toString());	
			}


			Print.printMsg("==============================================");

		}

		exePool.awaitTermination(5,TimeUnit.SECONDS);
		exePool.shutdown();

	}

	public static void printPoolInfo(ThreadPoolExecutor exePool, LinkedBlockingQueue<Runnable> workQueue) {
		int activeCnt = exePool.getActiveCount();
		int corePoolSize = exePool.getCorePoolSize();
		long keepAliveTime = exePool.getKeepAliveTime(TimeUnit.SECONDS);
		int largestPoolSize = exePool.getLargestPoolSize();
		int maximumPoolSize = exePool.getMaximumPoolSize();
		int poolSize = exePool.getPoolSize();
		int queueRemainCap = workQueue.remainingCapacity();
		int queueSize = workQueue.size();

		Print.printMsg("Information Pool ");
		String infoPool = null;
		infoPool =  "active count            : " + activeCnt;
		infoPool += "\npool size             : " + poolSize;
		infoPool += "\nQueue Size            : " + queueSize;
		infoPool += "\nkeepAliveTime         : " + keepAliveTime;
		infoPool += "\nlargest pool size     : " + largestPoolSize;
		infoPool += "\ncore pool size        : " + corePoolSize;
		infoPool += "\nmaximum pool size     : " + maximumPoolSize;
		infoPool += "\nQueue remain capacity : " + queueRemainCap;

		Print.printMsg(infoPool);
		Print.printMsg("==============================================");
	}
	

	static class ReadLock implements Runnable {
	
		long sleepTime = 0L;
		String tName = null;	
	
		public ReadLock(String tName, long sleepTime) {
			this.tName = tName;
			this.sleepTime = sleepTime;
		}
	
		@Override
		public void run() {
	
			try {

				rwlock.readLock().lock();
	
				Print.printMsg(" >> Start thread " + tName + ", sleep : " + sleepTime); 
				TimeUnit.SECONDS.sleep(sleepTime);
				Print.printMsg(" >> End thread " + tName + ", sleep : " + sleepTime); 
	
			} catch (Exception e) {
				Print.printMsg(e.toString());
			} finally {

				rwlock.readLock().unlock();	
		
			}
	
		}
	
	}

	static class WriteLock implements Runnable {
	
		long sleepTime = 0L;
		String tName = null;	
	
		public WriteLock(String tName, long sleepTime) {
			this.tName = tName;
			this.sleepTime = sleepTime;
		}
	
		@Override
		public void run() {
	
			try {

				rwlock.writeLock().lock();
	
				Print.printMsg(" >> Start thread " + tName + ", sleep : " + sleepTime); 
				TimeUnit.SECONDS.sleep(sleepTime);
				Print.printMsg(" >> End thread " + tName + ", sleep : " + sleepTime); 
	
			} catch (Exception e) {
				Print.printMsg(e.toString());
			} finally {

				rwlock.writeLock().unlock();	
		
			}
	
		}
	
	}

	static class TryLock implements Runnable {
	
		long sleepTime = 0L;
		String tName = null;	
	
		public TryLock(String tName, long sleepTime) {
			this.tName = tName;
			this.sleepTime = sleepTime;
		}
	
		@Override
		public void run() {

			boolean acquire = false;
	
			try {

				acquire = rwlock.writeLock().tryLock(100,TimeUnit.SECONDS);

				if(acquire) { 
	
					Print.printMsg(" >> Start thread " + tName + ", sleep : " + sleepTime); 
					TimeUnit.SECONDS.sleep(sleepTime);
					Print.printMsg(" >> End thread " + tName + ", sleep : " + sleepTime); 

				} else {

					Print.printMsg(" >> Fail to aquire lock by thread " + tName + ", sleep : " + sleepTime); 


				}
	
			} catch (Exception e) {
				Print.printMsg(e.toString());
				e.printStackTrace();
			} finally {

				if(acquire) rwlock.writeLock().unlock();	
		
			}
	
		}
	
	}
	
	static class Print {
	
	 	public static void printMsg(String msg) {printMsg(msg,false);}
	 	public static void printMsg(String msg, boolean header) {
	
	  		if(header) {
	   			System.out.println("===============================================");
	 		}
	
			System.out.println(msg);
	
			if(header) {
				System.out.println("===============================================");
			}
		}
	}

}
