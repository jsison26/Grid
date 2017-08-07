package model;

public class Job {
	private int jobId;
	private String jobName;
	private int jobTypeId;
	private String jobTypeName;
	private String status;
	private int clusterId;
	private String clusterName;
	private String commandLine;
	private String commandArguments;
	private String workingDirectory;
	
	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

	public int getJobId() {
		return jobId;
	}
	
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public String getJobName() {
		return jobName;
	}
	
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public int getJobTypeId() {
		return jobTypeId;
	}
	
	public void setJobTypeId(int jobTypeId) {
		this.jobTypeId = jobTypeId;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public String getJobTypeName() {
		return jobTypeName;
	}

	public void setJobTypeName(String jobTypeName) {
		this.jobTypeName = jobTypeName;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getCommandLine() {
		return commandLine;
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}

	public String getCommandArguments() {
		return commandArguments;
	}

	public void setCommandArguments(String commandArguments) {
		this.commandArguments = commandArguments;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + clusterId;
		result = prime * result + ((clusterName == null) ? 0 : clusterName.hashCode());
		result = prime * result + ((commandArguments == null) ? 0 : commandArguments.hashCode());
		result = prime * result + ((commandLine == null) ? 0 : commandLine.hashCode());
		result = prime * result + jobId;
		result = prime * result + ((jobName == null) ? 0 : jobName.hashCode());
		result = prime * result + jobTypeId;
		result = prime * result + ((jobTypeName == null) ? 0 : jobTypeName.hashCode());
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
		Job other = (Job) obj;
		if (clusterId != other.clusterId)
			return false;
		if (clusterName == null) {
			if (other.clusterName != null)
				return false;
		} else if (!clusterName.equals(other.clusterName))
			return false;
		if (commandArguments == null) {
			if (other.commandArguments != null)
				return false;
		} else if (!commandArguments.equals(other.commandArguments))
			return false;
		if (commandLine == null) {
			if (other.commandLine != null)
				return false;
		} else if (!commandLine.equals(other.commandLine))
			return false;
		if (jobId != other.jobId)
			return false;
		if (jobName == null) {
			if (other.jobName != null)
				return false;
		} else if (!jobName.equals(other.jobName))
			return false;
		if (jobTypeId != other.jobTypeId)
			return false;
		if (jobTypeName == null) {
			if (other.jobTypeName != null)
				return false;
		} else if (!jobTypeName.equals(other.jobTypeName))
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
		return "Job [jobId=" + jobId + ", jobName=" + jobName + ", jobTypeId=" + jobTypeId + ", jobTypeName="
				+ jobTypeName + ", status=" + status + ", clusterId=" + clusterId + ", clusterName=" + clusterName
				+ ", commandLine=" + commandLine + ", commandArguments=" + commandArguments + ", workingDirectory="
				+ workingDirectory + "]";
	}

	
	
}
