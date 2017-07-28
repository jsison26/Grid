package model;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JobTypeDB {
	
	private List<JobType> jobTypes;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();
	
	public JobTypeDB() {
		jobTypes = new LinkedList<JobType>();
	}
	
	public void reloadJobTypesAll() throws SQLException {
		writeLock.lock();
		try {
			jobTypes.clear();
			jobTypes = DBAccess.getInstance().getJobTypes();
		}
		finally {
			writeLock.unlock();
		}
	}
		
	public List<JobType> getJobTypes(){
		readLock.lock();
		try {
			return Collections.unmodifiableList(jobTypes);
		}
		finally {
			readLock.unlock();
		}
	}
}

