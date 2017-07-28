package model;

public class Cluster {

	private int clusterId;
	private String clusterName;
	private String status;
	
	public int getClusterId() {
		return clusterId;
	}
	
	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}
	
	public String getClusterName() {
		return clusterName;
	}
	
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + clusterId;
		result = prime * result + ((clusterName == null) ? 0 : clusterName.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cluster other = (Cluster) obj;
		if (clusterId != other.clusterId)
			return false;
		if (clusterName == null) {
			if (other.clusterName != null)
				return false;
		} else if (!clusterName.equals(other.clusterName))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Cluster [clusterId=" + clusterId + ", clusterName=" + 
				clusterName + ", status=" + status + "]";
	}
	
}
