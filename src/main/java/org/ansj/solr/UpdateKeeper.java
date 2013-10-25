package org.ansj.solr;

import java.io.IOException;
import java.util.Vector;

//TODO optimize
public class UpdateKeeper implements Runnable{
	
	public static interface UpdateJob{
		public void update() throws IOException ;

	}
		
	final static int INTERVAL = 1 * 60 * 1000;
	
	private static UpdateKeeper singleton;
	Vector<UpdateJob> filterFactorys;
	Thread worker;

	private UpdateKeeper(){
		filterFactorys = new Vector<UpdateJob>();

		worker = new Thread(this);
		worker.setDaemon(true);
		worker.start();
	}
	
	public static UpdateKeeper getInstance(){
		if(singleton == null){
			synchronized(UpdateKeeper.class){
				if(singleton == null){
					singleton = new UpdateKeeper();
					return singleton;
				}
			}
		}
		return singleton;
	}
	
	/*保留各个FilterFactory实例对象的引用，用于后期更新操作*/
	public void register(UpdateKeeper.UpdateJob filterFactory ){
		filterFactorys.add(filterFactory);
	}
	
	public void run() {
		while(true){
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(!filterFactorys.isEmpty()){
				for(UpdateJob factory: filterFactorys){
					try {
						factory.update();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
}

