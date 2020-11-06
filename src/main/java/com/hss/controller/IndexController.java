package com.hss.controller;

import com.hss.task.UploadTask;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Scheduler scheduler;

    @RequestMapping(value = "/startJob", method = RequestMethod.GET)
    @ResponseBody
    public String index() throws SchedulerException {
        //cron表达式
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0/8 * * * * ?");
        //根据name 和group获取当前trgger 的身份
        TriggerKey triggerKey = TriggerKey.triggerKey("triggerName", "triggerGroup");
        CronTrigger triggerOld = null;
        try {
            //获取 触发器的信息
            triggerOld = (CronTrigger) scheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        if (triggerOld == null) {
            //将job加入到jobDetail中
            JobDetail jobDetail = JobBuilder.newJob(UploadTask.class).withIdentity("jobName", "jobGroup").build();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("triggerName","triggerGroup").withSchedule(cronScheduleBuilder).build();
            //执行任务
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            logger.info("当前job已存在--------------------------------------------");
        }
        return "success";
    }

    @RequestMapping(value = "/endJob", method = RequestMethod.GET)
    @ResponseBody
    public String endJob() throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey("triggerName", "triggerGroup");
        CronTrigger triggerOld = null;
        try {
            //获取 触发器的信息
            triggerOld = (CronTrigger) scheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        if (triggerOld != null) {
            scheduler.unscheduleJob(triggerKey);
            logger.info("成功关闭---------------------");
        }else{
            logger.info("job不存在--------------------");
        }
        return "success";
    }
}
