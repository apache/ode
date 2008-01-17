CREATE TABLE ODE_JOB (jobid varchar2(64 char) DEFAULT '' NOT NULL, ts number(19,0) DEFAULT 0 NOT NULL, nodeid varchar2(64 char) NULL, scheduled number(12,0) DEFAULT 0 NOT NULL, transacted number(12,0) DEFAULT 0 NOT NULL, details BLOB, PRIMARY KEY(jobid));

CREATE INDEX IDX_ODE_JOB_TS ON ode_job(ts);
CREATE INDEX IDX_ODE_JOB_NODEID ON ode_job(nodeid);


