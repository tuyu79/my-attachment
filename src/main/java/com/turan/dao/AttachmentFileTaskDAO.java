package com.turan.dao;

import com.turan.po.AttachmentFileTask;
import com.turan.po.AttachmentFileTaskExample;
import org.springframework.stereotype.Repository;

/**
 * AttachmentFileTaskDAO继承基类
 */
@Repository
public interface AttachmentFileTaskDAO extends MyBatisBaseDao<AttachmentFileTask, Long, AttachmentFileTaskExample> {
}