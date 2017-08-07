package model;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ClusterDB {
	
	private List<Cluster> clusters;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();
	
	public ClusterDB() {
		clusters = new LinkedList<Cluster>();
	}
	
	public int addCluster(Cluster cluster) throws SQLException {
		int numOfTries = 0;
		int clusterId;
		
		writeLock.lock();
		try {
			do{
				clusterId = DBAccess.getInstance().createCluster(cluster);			
				if (clusterId >= 0) {
					cluster.setClusterId(clusterId);
					clusters.add(cluster);
				}
			} while (clusterId < 0 && numOfTries++ < 10);
		}
		finally {
			writeLock.unlock();
		}
		
		return clusterId;
	}
	
	public void reloadClusters() throws SQLException {
		writeLock.lock();
		try {
			clusters.clear();
			clusters = DBAccess.getInstance().getClustersAll();
		}
		finally {
			writeLock.unlock();
		}
	}
	
	public void reloadCluster(String clusterName) throws SQLException {
		writeLock.lock();
		try {
			clusters.clear();
			Cluster c = DBAccess.getInstance().getClusterByName(clusterName);
			if(c != null)
				clusters.add(c);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	public void updateCluster(Cluster cluster) throws SQLException {
				
		writeLock.lock();
		try {
			DBAccess.getInstance().updateCluster(cluster);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	public void insertCluster(Cluster cluster) {
		writeLock.lock();
		try {
			clusters.add(cluster);
		}
		finally {
			writeLock.unlock();
		}
	}
	
	public List<Cluster> getClusters() {
		readLock.lock();
		try {
			return Collections.unmodifiableList(clusters);
		}
		finally {
			readLock.unlock();
		}
	}	
	
	
	public Cluster getClusterByClusterName(String clusterName) {
		Cluster c = null;
		
		readLock.lock();
		try {
			for (Cluster cluster: clusters) {
				if (cluster.getClusterName().equals(clusterName)) {
					c = cluster;
					break;
				}
			}
		}
		finally {
			readLock.unlock();
		}
		
		return c;
	}
	
	public List<Cluster> getAvailableClusters() {
		List<Cluster> availableClusters = new LinkedList<Cluster>();
		
		readLock.lock();
		try {
			for (Cluster cluster: clusters) {
				if (cluster.getStatus().equals(ClusterStatus.AVAILABLE))
					availableClusters.add(cluster);
			}
		}
		finally {
			readLock.unlock();
		}
		
		return Collections.unmodifiableList(availableClusters);
	}
	
}
