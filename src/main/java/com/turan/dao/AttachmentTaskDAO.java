package com.turan.dao;

import com.turan.po.AttachmentTask;
import com.turan.po.AttachmentTaskExample;
import org.springframework.stereotype.Repository;

/**
 * AttachmentTaskDAO继承基类
 */
@Repository
public interface AttachmentTaskDAO extends MyBatisBaseDao<AttachmentTask, Long, AttachmentTaskExample> {
}