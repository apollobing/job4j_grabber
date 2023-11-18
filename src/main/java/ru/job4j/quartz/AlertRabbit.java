package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    private Connection cn;

    private void init() throws IOException, ClassNotFoundException, SQLException {
        Properties config = getProperties();
        Class.forName(config.getProperty("driver"));
        cn = DriverManager.getConnection(
                config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password")
        );
    }

    public static Properties getProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        }
        return properties;
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        AlertRabbit alertRabbit = new AlertRabbit();
        alertRabbit.init();
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", alertRabbit.cn);
            JobDetail job = newJob(Rabbit.class).usingJobData(data).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(getProperties().getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
            alertRabbit.cn.close();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement ps =
                         cn.prepareStatement("INSERT INTO rabbit(created_date) VALUES (?)")) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().withNano(0)));
                ps.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
