-- Apache ODE - SimpleScheduler Database Schema
-- 
-- MySQL scripts by Maciej Szefler.
-- 
-- 
DROP TABLE IF EXISTS ODE_JOB;

CREATE TABLE ODE_JOB (
  jobid CHAR(64)  NOT NULL DEFAULT '',
  ts BIGINT  NOT NULL DEFAULT 0,
  nodeid char(64)  NULL,
  scheduled int  NOT NULL DEFAULT 0,
  transacted int  NOT NULL DEFAULT 0,

  instanceId BIGINT,
  mexId varchar(255),
  processId varchar(255),
  type varchar(255),
  channel varchar(255),
  correlatorId varchar(255),
  correlationKeySet varchar(255),
  retryCount int,
  inMem int,
  detailsExt blob(4096),

  PRIMARY KEY(jobid),
  INDEX IDX_ODE_JOB_TS(ts),
  INDEX IDX_ODE_JOB_NODEID(nodeid)
)
TYPE=InnoDB;

COMMIT;

