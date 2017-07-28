package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


public class DBAccess {
	private static Vector<Connection> connectionPool = new Vector<Connection>();
	private static final int MAX_POOL_SIZE = 10;
	
	private String url = "jdbc:mysql://192.168.0.1/grid";
	private String userName = "root";
	private String password = "admin";
	
	private DBAccess() {
		initialize();
	}
	
	private void initialize() {
		initializeConnectionPool();
	}
	
	private static class DBHelper {
		private static final DBAccess Instance = new DBAccess();
	}
	
	public static DBAccess getInstance() {
		return DBHelper.Instance;
	}
	
	private void initializeConnectionPool() {
        while (!checkIfConnectionPoolIsFull()) {
            connectionPool.addElement(createNewConnectionForPool());
        }
    }
	
	private synchronized boolean checkIfConnectionPoolIsFull() {
        if (connectionPool.size() < MAX_POOL_SIZE) {
            return false;
        }
        return true;
    }
	
	private Connection createNewConnectionForPool() {
        Connection connection = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, userName, password);
        }
        catch (SQLException sqle) {
            System.err.println("SQLException: "+sqle);
            return null;
        }
        catch (ClassNotFoundException cnfe) {
            System.err.println("ClassNotFoundException: " + cnfe);
            return null;
        }

        return connection;
    }
	
	public synchronized Connection getConnectionFromPool() {
        Connection connection = null;
        
        if (connectionPool.size() > 0) {
            connection = (Connection) connectionPool.firstElement();
            connectionPool.removeElementAt(0);
        }
        
        return connection;
    }

    public synchronized void returnConnectionToPool(Connection connection) {
        connectionPool.addElement(connection);
    }
	
	public void disconnect() throws SQLException {
		for (Connection conn: connectionPool) {
			if (conn != null)
				conn.close();
		}
	}
	
	public int createJob(Job job) throws SQLException {
		int jobId = -1;
		int paramIndex = 1;
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query = " insert into jobs (jobId, jobName, jobTypeId, status, clusterName) "
				+ " values (null, ?, ?, ?, ?) ";
		
		try (PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {			
			if (job.getJobName() != null && job.getJobName().length() > 0)
				st.setString(paramIndex++, job.getJobName());
			else
				st.setNull(paramIndex++, Types.CHAR);
			
			st.setInt(paramIndex++, job.getJobTypeId());			
			
			if (job.getStatus() != null && job.getStatus().length() > 0)
				st.setString(paramIndex++, job.getStatus());
			else
				st.setNull(paramIndex++, Types.CHAR);
			
			st.setInt(paramIndex++, job.getClusterId());
			
			st.executeUpdate();			
			ResultSet generatedKeys = st.getGeneratedKeys();
            if (generatedKeys.next()) {
                jobId = generatedKeys.getInt(1);
            }
            else {
                throw new SQLException("Create job failed, no ID obtained.");
            }
		} finally {
			returnConnectionToPool(conn);
		}
		
		return jobId;
	}
	
	public void updateJob(Job job) throws SQLException {
		int paramIndex = 1;
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query = " update jobs "
			+ " set "  + (job.getJobName() == null ? " jobName = null " : " jobName = ? ")
				+ " ,jobTypeId = ? "
				+ (job.getStatus() == null ? " ,status = null " : " ,status = ? ")
				+ " ,clusterName = ? "
			+ " where jobId = ?";
		
		try (PreparedStatement st = conn.prepareStatement(query);) {
			if(job.getJobName() != null) st.setString(paramIndex++, job.getJobName());
			st.setInt(paramIndex++, job.getJobTypeId());
			if(job.getStatus() != null) st.setString(paramIndex++, job.getStatus());
			st.setInt(paramIndex++, job.getClusterId());
			st.setInt(paramIndex++, job.getJobId());
			
			st.executeUpdate();		
		} finally {
			returnConnectionToPool(conn);
		}
	}
	
	public void deleteJob(Job job) throws SQLException {
		int paramIndex = 1;
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query = " delete jobs where jobId = ? ";
		
		try (PreparedStatement st = conn.prepareStatement(query);) {			
			st.setInt(paramIndex++, job.getJobId());
			
			st.executeUpdate();
		} finally {
			returnConnectionToPool(conn);
		}
	}
	
	public void truncateJobs() throws SQLException {
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query = " truncate table jobs ";
		
		try (PreparedStatement st = conn.prepareStatement(query);) {
			st.executeUpdate();
		} finally {
			returnConnectionToPool(conn);
		}
	}
	
	public List<Job> getJobsAll() throws SQLException {
		List<Job> result = new LinkedList<Job>();		
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query = " select j.jobId, j.jobName, j.jobTypeId, jt.jobTypeName, "
			+ " j.status, j.clusterId,  c.clusterName "
			+ " from jobs j "
				+ " left outer join jobTypes jt "
				+ " on j.jobTypeId = jt.jobTypeId "
				+ " left outer join clusters c "
				+ " on j.clusterId = c.clusterId ";
		
		try (PreparedStatement st = conn.prepareStatement(query);
			ResultSet rs = st.executeQuery();) {
						
			while (rs.next()) {
				Job job = new Job();
				job.setJobId(rs.getInt("jobId"));
				job.setJobName(rs.getString("jobName"));
				job.setJobTypeId(rs.getInt("jobTypeId"));
				job.setJobTypeName(rs.getString("jobTypeName"));
				job.setStatus(rs.getString("status"));
				job.setClusterId(rs.getInt("clusterId"));
				job.setClusterName(rs.getString("clusterName"));
				
				result.add(job);
	        }
		} finally {
			returnConnectionToPool(conn);
		}
		
		return result;
	}
	
	public List<Job> getJobsByClusterName(String clusterName) throws SQLException {
		List<Job> result = new LinkedList<Job>();
		int paramIndex = 1;
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query = " select j.jobId, j.jobName, j.jobTypeId, jt.jobTypeName, "
			+ " j.status, j.clusterId, c.clusterName "
			+ " from jobs j "
				+ " left outer join jobTypes jt "
				+ " on j.jobTypeId = jt.jobTypeId "
				+ " left outer join clusters c "
				+ " on j.clusterId = c.clusterId "
			+ " where c.clusterName = ? ";
		
		try (PreparedStatement st = conn.prepareStatement(query);) {		
			st.setString(paramIndex++, clusterName);
			
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				Job job = new Job();
				job.setJobId(rs.getInt("jobId"));
				job.setJobName(rs.getString("jobName"));
				job.setJobTypeId(rs.getInt("jobTypeId"));
				job.setJobTypeName(rs.getString("jobTypeName"));
				job.setStatus(rs.getString("status"));
				job.setClusterId(rs.getInt("clusterId"));
				job.setClusterName(rs.getString("clusterName"));
				
				result.add(job);
	        }
		} finally {
			returnConnectionToPool(conn);
		}
		
		return result;
	}
	
	public int createCluster(Cluster cluster) throws SQLException {
		int clusterId = -1;
		int paramIndex = 1;
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query = " insert into clusters (clusterId, clusterName, status) "
				+ " values (null, ?, ?) ";
		
		try (PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {			
			if (cluster.getClusterName() != null && cluster.getClusterName().length() > 0)
				st.setString(paramIndex++, cluster.getClusterName());
			else
				st.setNull(paramIndex++, Types.CHAR);
			
			if (cluster.getStatus() != null && cluster.getStatus().length() > 0)
				st.setString(paramIndex++, cluster.getStatus());
			else
				st.setNull(paramIndex++, Types.CHAR);
						
			st.executeUpdate();			
			ResultSet generatedKeys = st.getGeneratedKeys();
            if (generatedKeys.next()) {
                clusterId = generatedKeys.getInt(1);
            }
            else {
                throw new SQLException("Create cluster failed, no ID obtained.");
            }
		} finally {
			returnConnectionToPool(conn);
		}
		
		return clusterId;
	}
	
	public void updateCluster(Cluster cluster) throws SQLException {
		int paramIndex = 1;
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query=" update clusters "
			+ " set clusterName = ?, "
				+ " status = ? "
			+ " where clusterId = ?";
		
		try (PreparedStatement st = conn.prepareStatement(query);) {			
			st.setString(paramIndex++, cluster.getClusterName());
			st.setString(paramIndex++, cluster.getStatus());
			st.setInt(paramIndex++, cluster.getClusterId());
			
			st.executeUpdate();
		} finally {
			returnConnectionToPool(conn);
		}
	}
	
	public List<Cluster> getClustersAll() throws SQLException {
		List<Cluster> result = new LinkedList<Cluster>();
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query = " select clusterId, clusterName, status from clusters ";
		
		try (PreparedStatement st = conn.prepareStatement(query);) {			
			ResultSet rs = st.executeQuery();            
			while (rs.next()) {
				Cluster cluster = new Cluster();
				cluster.setClusterId(rs.getInt("clusterId"));
				cluster.setClusterName(rs.getString("clusterName"));
				cluster.setStatus(rs.getString("status"));
				
				result.add(cluster);
	        }
		} 
		finally {
			returnConnectionToPool(conn);
		}
		
		return result;
	}
	
	public Cluster getClusterByName(String clusterName) throws SQLException {
		Cluster cluster = null;
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query=" select clusterId, clusterName, directoryPath, moduleCount, status "
			+ " from clusters "
			+ " where clusterName = ? ";
		
		try (PreparedStatement st = conn.prepareStatement(query);) {
			st.setString(1, clusterName);

			ResultSet rs = st.executeQuery();            
			if (rs.next()) {
				cluster = new Cluster();
				cluster.setClusterId(rs.getInt("clusterId"));
				cluster.setClusterName(rs.getString("clusterName"));
				cluster.setStatus(rs.getString("status"));
	        }
		} 
		finally {
			returnConnectionToPool(conn);
		}
		
		return cluster;
	}
	
	public List<JobType> getJobTypes() throws SQLException {
		List<JobType> result = new LinkedList<JobType>();
		Connection conn = getConnectionFromPool();
		
		if (conn.isValid(1_000))
			conn = createNewConnectionForPool();
		
		String query = " select jt.jobTypeId, jt.jobTypeName "
			+ " from jobtypes jt "
			+ " order by jt.jobTypeName ";
		
		try (PreparedStatement st = conn.prepareStatement(query);) {	
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				JobType jobType = new JobType();
				jobType.setJobTypeId(rs.getInt("jobTypeId"));
				jobType.setJobTypeName(rs.getString("jobTypeName"));
								
				result.add(jobType);
			}
		} finally {
			returnConnectionToPool(conn);
		}
		
		return result;
	}
}
