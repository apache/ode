# Migration Section 1: 
# ============================================================================================================================

# All databases need to be migrated and the column type to be used is given below.
# MYSQL migration is provided here, users are requested to check appropriate database syntax for other databases.


# USE COLUMN TYPE FOR COLUMN "DATA" FOR THE RELEVANT DATABASE
# Mysql           LONGTEXT
# SQLServer       TEXT
# Postgres        TEXT
# Oracle          CLOB
# H2              CLOB
# Derby           CLOB
# HSQL            LONGVARCHAR


# Hibernate MYSQL Script
ALTER TABLE STORE_PROCESS_PROP ADD COLUMN DATA LONGTEXT;
CREATE TABLE STORE_PROCESS_PROP_TEST SELECT * FROM STORE_PROCESS_PROP;
UPDATE STORE_PROCESS_PROP A SET A.DATA=(SELECT VALUE FROM STORE_PROCESS_PROP_TEST  WHERE PROPID=A.PROPID AND NAME=A.NAME);
DROP TABLE STORE_PROCESS_PROP_TEST;
ALTER TABLE STORE_PROCESS_PROP DROP COLUMN VALUE;


# OpenJPA MYSQL Script
ALTER TABLE STORE_PROCESS_PROP ADD COLUMN DATA LONGTEXT;
CREATE TABLE STORE_PROCESS_PROP_TEST SELECT * FROM STORE_PROCESS_PROP;
UPDATE STORE_PROCESS_PROP A SET A.DATA=(SELECT PROP_VAL FROM STORE_PROCESS_PROP_TEST WHERE ID=A.ID);
DROP TABLE STORE_PROCESS_PROP_TEST;
ALTER TABLE STORE_PROCESS_PROP DROP COLUMN PROP_VAL;

# ==========================================================================================================================




# Migration Section 2:
# ======================================================================================================

# Additional Migration for MySQL for Hibernate. No other databases need the below changes.

ALTER TABLE BPEL_ACTIVITY_RECOVERY  MODIFY DETAILS              longblob;
ALTER TABLE BPEL_EVENT              MODIFY DETAIL               longtext;
ALTER TABLE BPEL_EVENT              MODIFY DATA                 longblob;
ALTER TABLE BPEL_FAULT              MODIFY DATA                 longblob;
ALTER TABLE BPEL_INSTANCE           MODIFY JACOB_STATE_DATA     longblob;
ALTER TABLE BPEL_MESSAGE            MODIFY MESSAGE_DATA         longblob;
ALTER TABLE BPEL_MESSAGE            MODIFY MESSAGE_HEADER       longblob;
ALTER TABLE BPEL_MESSAGE_EXCHANGE   MODIFY ENDPOINT             longblob;
ALTER TABLE BPEL_MESSAGE_EXCHANGE   MODIFY CALLBACK_ENDPOINT    longblob;
ALTER TABLE BPEL_PLINK_VAL          MODIFY MYROLE_EPR_DATA      longblob;
ALTER TABLE BPEL_PLINK_VAL          MODIFY PARTNERROLE_EPR_DATA longblob;
ALTER TABLE BPEL_XML_DATA           MODIFY DATA                 longblob;
# =======================================================================================================