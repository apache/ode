-- Apache ODE - SimpleScheduler Database Schema
--
-- Apache Derby scripts by Maciej Szefler.
--
--

DROP TABLE ode_job;

CREATE TABLE ode_job (
  jobid VARCHAR(64)  NOT NULL,
  ts number(37)  NOT NULL,
  nodeid varchar(64),
  scheduled int  NOT NULL,
  transacted int  NOT NULL,

  instanceId number(37),
  mexId varchar(255),
  processId varchar(255),
  type varchar(255),
  channel varchar(255),
  correlatorId varchar(255),
  correlationKeySet varchar(255),
  retryCount int,
  inMem int,
  detailsExt blob,

  PRIMARY KEY(jobid));

CREATE INDEX IDX_ODE_JOB_TS ON ode_job(ts);
CREATE INDEX IDX_ODE_JOB_NODEID ON ode_job(nodeid);


