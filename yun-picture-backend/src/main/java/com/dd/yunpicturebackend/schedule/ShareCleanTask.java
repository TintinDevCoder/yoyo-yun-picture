package com.dd.yunpicturebackend.schedule;

import com.dd.yunpicturebackend.mapper.SharePictureMapper;
import com.dd.yunpicturebackend.service.SharePictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/**
 * 定时清理分享图片数据，每天0点清理
 */
@Slf4j
@Configuration
public class ShareCleanTask {
    @Autowired
    private SharePictureService sharePictureService;

    // 定时任务：每天 0 点执行
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanUpOldPictures() {
        // 清空数据
        // 清空整个表
        sharePictureService.lambdaUpdate().remove();
        log.info("已清空分享图片数据");
    }
}
