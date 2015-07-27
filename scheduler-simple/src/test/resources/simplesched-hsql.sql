CREATE TABLE ode_job (
  jobid varchar(64)  NOT NULL,
  ts BIGINT  NOT NULL,
  nodeid varchar(64),
  scheduled int  NOT NULL,
  transacted int  NOT NULL,

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

  PRIMARY KEY(jobid));

