
# MySQL DDL

CREATE TABLE `ODE_JOB` (
  `jobid` CHAR(64)  NOT NULL DEFAULT '',
  `ts` BIGINT  NOT NULL DEFAULT 0,
  `nodeid` char(64)  NULL,
  `scheduled` int  NOT NULL DEFAULT 0,
  `transacted` int  NOT NULL DEFAULT 0,
  `details` blob(4096)  NULL,
  PRIMARY KEY(`jobid`),
  INDEX `IDX_ODE_JOB_TS`('ts'),
  INDEX `IDX_ODE_JOB_NODEID`('nodeid')
)

