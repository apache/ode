package org.apache.ode.bpel.engine.cron;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.schedules.SchedulesDocument;
import org.apache.ode.bpel.schedules.TSchedule;
import org.apache.ode.bpel.dd.TCleanup;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.ProcessConf.CleanupInfo;
import org.apache.ode.bpel.iapi.ProcessConf.CronJob;
import org.apache.ode.store.ProcessCleanupConfImpl;
import org.apache.ode.utils.CronExpression;
import org.apache.xmlbeans.XmlOptions;

public class SystemSchedulesConfig {
    private final static Log __log = LogFactory.getLog(SystemSchedulesConfig.class);
    
    public final static String SCHEDULE_CONFIG_FILE_PROP_KEY = "org.apache.ode.scheduleConfigFile";
    private File schedulesFile;
    
    public SystemSchedulesConfig(File configRoot) {
        String scheduleConfigFile = System.getProperty(SCHEDULE_CONFIG_FILE_PROP_KEY);
        if( scheduleConfigFile != null ) {
            schedulesFile = new File(scheduleConfigFile);
            if( !new File(scheduleConfigFile).exists()) {
                __log.warn("A custom location for schedules has been set. However, the file does not exist at the location: " 
                        + schedulesFile.getAbsolutePath() + ". The file will be read when one gets created.");
            }
        } else {
            assert configRoot != null;
            schedulesFile = new File(configRoot, "schedules.xml");
        }
        __log.info("SYSTEM CRON configuration: " + schedulesFile.getAbsolutePath());
    }
    
    public File getSchedulesFile() {
        return schedulesFile;
    }
    
    /**
     * Returns the list of cron jobs configured for all processes. This call returns
     * a fresh snapshot.
     * 
     * @return the list of cron jobs
     */
    public List<CronJob> getSystemCronJobs() {
        List<CronJob> jobs = new ArrayList<CronJob>();
        
        if( schedulesFile != null && schedulesFile.exists() ) {
            for(TSchedule schedule : getSystemSchedulesDocument().getSchedules().getScheduleList()) {
                CronJob job = new CronJob();
                try {
                    job.setCronExpression(new CronExpression(schedule.getWhen()));
                    for(final TCleanup aCleanup : schedule.getCleanupList()) {
                        CleanupInfo cleanupInfo = new CleanupInfo();
                        assert !aCleanup.getFilterList().isEmpty();
                        cleanupInfo.setFilters(aCleanup.getFilterList());
                        ProcessCleanupConfImpl.processACleanup(cleanupInfo.getCategories(), aCleanup.getCategoryList());
                        
                        Map<String, Object> runnableDetails = new HashMap<String, Object>();
                        runnableDetails.put("cleanupInfo", cleanupInfo);
                        runnableDetails.put("transactionSize", 10);
                        job.getRunnableDetailList().add(runnableDetails);
                        __log.info("SYSTEM CRON configuration added a runtime data cleanup: " + runnableDetails);
                    }
                    jobs.add(job);
                } catch( ParseException pe ) {
                    __log.error("Exception during parsing the schedule cron expression: " + schedule.getWhen() + ", skipped the scheduled job.", pe);
                }
            }
        }
        
        __log.info("SYSTEM CRON configuration found cron jobs: " + jobs);
        return jobs;
    }

    @SuppressWarnings("unchecked")
    private SchedulesDocument getSystemSchedulesDocument() {
        SchedulesDocument sd = null;
        
        try {
            XmlOptions options = new XmlOptions();
            HashMap otherNs = new HashMap();

            otherNs.put("http://ode.fivesight.com/schemas/2006/06/27/dd",
                    "http://www.apache.org/ode/schemas/schedules/2009/05");
            options.setLoadSubstituteNamespaces(otherNs);
            sd = SchedulesDocument.Factory.parse(schedulesFile, options);
        } catch (Exception e) {
            throw new ContextException("Couldn't read schedule descriptor at location "
                    + schedulesFile.getAbsolutePath(), e);
        }
    
        return sd;
    }
}