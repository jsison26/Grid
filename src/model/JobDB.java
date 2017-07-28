package model;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import model.Job;


public class JobDB {
	private List<Job> jobs;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();
	
	public JobDB() {
		jobs = new LinkedList<Job>();
	}
	
	public int addJob(Job job) throws SQLException {
		int numOfTries = 0;
		int jobId;
		
		writeLock.lock();
		try {
			do{
				jobId = DBAccess.getInstance().createJob(job);			
				if (jobId < 0) {
					job.setJobId(jobId);
					jobs.add(job);
				}
			} while (jobId < 0 && numOfTries++ < 10);
		}
		finally {
			writeLock.unlock();
		}
		
		return jobId;
	}
	
	public void updateJob (Job job) throws SQLException {
		writeLock.lock();
		try {
			DBAccess.getInstance().updateJob(job);
			
			for (Job j: jobs) {
				if (j.equals(job)) {
					j.setJobName(job.getJobName());
					j.setJobTypeId(job.getJobTypeId());
					j.setJobTypeName(job.getJobTypeName());
					j.setStatus(job.getStatus());
					break;
				}
			}
		}
		finally {
			writeLock.unlock();
		}
	}
	
	
	public void insertJob (Job job) {
		writeLock.lock();
		try {
			jobs.add(job);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	public void deleteJob (Job job) throws SQLException {
		writeLock.lock();
		try {
			DBAccess.getInstance().deleteJob(job);
			jobs.remove(job);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	public void truncateJobs() throws SQLException {
		writeLock.lock();
		try {
			DBAccess.getInstance().truncateJobs();
			jobs.clear();
		} 
		finally {
			writeLock.unlock();
		}
	}
	
	public void reloadJobsAll() throws SQLException {
		writeLock.lock();
		try {
			jobs.clear();
			jobs = DBAccess.getInstance().getJobsAll();
		}
		finally {
			writeLock.unlock();
		}
	}
	
	public void reloadJobsByClusterName(String clusterName) throws SQLException {
		writeLock.lock();
		try {
			jobs.clear();
			jobs = DBAccess.getInstance().getJobsByClusterName(clusterName);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	public List<Job> getJobs() {
		readLock.lock();
		try {
			return Collections.unmodifiableList(jobs);
		}
		finally {
			readLock.unlock();
		}
	}
	
	public Job getJobByJobName(String jobName) {
		Job j = null;
		
		readLock.lock();
		try {
			for (Job job: jobs) {
				if (job.equals(jobName)) {
					j = job;
					break;
				}
			}
		}
		finally {
			readLock.unlock();
		}
		
		return j;
	}
	
	public List<Job> getQueuedJobs() {
		List<Job> queuedJob = new LinkedList<Job>();
		
		readLock.lock();
		try {
			for (Job job: jobs) {
				if (job.getStatus().equals(JobStatus.QUEUED) || job.getStatus().equals(JobStatus.ACTIVE)) {
					queuedJob.add(job);				
				}
			}
		}
		finally {
			readLock.unlock();
		}

		return Collections.unmodifiableList(queuedJob);
	}

}
