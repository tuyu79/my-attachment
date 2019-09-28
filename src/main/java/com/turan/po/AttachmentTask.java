package com.turan.po;

import java.io.Serializable;
import java.util.Date;

/**
 * attachment_task
 * @author 
 */
public class AttachmentTask implements Serializable {
    /**
     * 自增id
     */
    private Long id;

    /**
     * 任务编号
     */
    private Long taskNo;

    private Long vehicleNo;

    /**
     * 平台报警唯一id
     */
    private Long platformAlarmUid;

    /**
     * 上传文件数量
     */
    private Integer fileCount;

    /**
     * 上传状态(0-等待上传,1-正在上传,2-上传成功,3-上传失败,4-已取消)
     */
    private Integer status;

    private Date createAt;

    private Date updateAt;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(Long taskNo) {
        this.taskNo = taskNo;
    }

    public Long getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(Long vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public Long getPlatformAlarmUid() {
        return platformAlarmUid;
    }

    public void setPlatformAlarmUid(Long platformAlarmUid) {
        this.platformAlarmUid = platformAlarmUid;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        AttachmentTask other = (AttachmentTask) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTaskNo() == null ? other.getTaskNo() == null : this.getTaskNo().equals(other.getTaskNo()))
            && (this.getVehicleNo() == null ? other.getVehicleNo() == null : this.getVehicleNo().equals(other.getVehicleNo()))
            && (this.getPlatformAlarmUid() == null ? other.getPlatformAlarmUid() == null : this.getPlatformAlarmUid().equals(other.getPlatformAlarmUid()))
            && (this.getFileCount() == null ? other.getFileCount() == null : this.getFileCount().equals(other.getFileCount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateAt() == null ? other.getCreateAt() == null : this.getCreateAt().equals(other.getCreateAt()))
            && (this.getUpdateAt() == null ? other.getUpdateAt() == null : this.getUpdateAt().equals(other.getUpdateAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTaskNo() == null) ? 0 : getTaskNo().hashCode());
        result = prime * result + ((getVehicleNo() == null) ? 0 : getVehicleNo().hashCode());
        result = prime * result + ((getPlatformAlarmUid() == null) ? 0 : getPlatformAlarmUid().hashCode());
        result = prime * result + ((getFileCount() == null) ? 0 : getFileCount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateAt() == null) ? 0 : getCreateAt().hashCode());
        result = prime * result + ((getUpdateAt() == null) ? 0 : getUpdateAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", taskNo=").append(taskNo);
        sb.append(", vehicleNo=").append(vehicleNo);
        sb.append(", platformAlarmUid=").append(platformAlarmUid);
        sb.append(", fileCount=").append(fileCount);
        sb.append(", status=").append(status);
        sb.append(", createAt=").append(createAt);
        sb.append(", updateAt=").append(updateAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}