--
-- to keep a permanent record of all jobs create an 'ANALYSIS_JOB_ARCHIVE' table,
-- and a trigger which copies from ANALYSIS_JOB to ANALYSIS_JOB_ARCHIVE whenever 
-- a record is deleted
-- 
CREATE TABLE "ANALYSIS_JOB_ARCHIVE" 
(   
  "JOB_NO" NUMBER(10,0) NOT NULL ENABLE, 
    "TASK_ID" NUMBER(10,0), 
    "STATUS_ID" NUMBER(10,0), 
    "DATE_SUBMITTED" TIMESTAMP (6), 
    "DATE_COMPLETED" TIMESTAMP (6), 
    "USER_ID" VARCHAR2(4000 BYTE), 
    "ISINDEXED" NUMBER(1,0) NOT NULL ENABLE, 
    "ACCESS_ID" NUMBER(10,0), 
    "JOB_NAME" VARCHAR2(4000 BYTE), 
    "LSID" VARCHAR2(4000 BYTE), 
    "TASK_LSID" VARCHAR2(4000 BYTE), 
    "TASK_NAME" VARCHAR2(4000 BYTE), 
    "PARENT" NUMBER(10,0), 
    "DELETED" NUMBER(1,0) NOT NULL ENABLE, 
    "PARAMETER_INFO" CLOB
) 

 
CREATE OR REPLACE TRIGGER on_analysis_job_del
BEFORE
DELETE
ON analysis_job
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
BEGIN
INSERT INTO ANALYSIS_JOB_ARCHIVE VALUES
(:OLD.JOB_NO, :OLD.TASK_ID, :OLD.STATUS_ID, :OLD.DATE_SUBMITTED,
:OLD.DATE_COMPLETED, :OLD.USER_ID, :OLD.isindexed, :OLD.access_id,
:OLD.job_name, :OLD.lsid, :OLD.task_lsid, :OLD.task_name, :OLD.PARENT,
:OLD.deleted, :OLD.parameter_info);
END ON_ANALYSIS_JOB_DEL;

CREATE OR REPLACE FORCE VIEW ANALYSIS_JOB_TOTAL (
  "JOB_NO", 
  "TASK_ID", 
  "STATUS_ID", 
  "DATE_SUBMITTED", 
  "DATE_COMPLETED", 
  "USER_ID", 
  "ISINDEXED", 
  "ACCESS_ID", 
  "JOB_NAME", 
  "LSID", 
  "TASK_LSID", 
  "TASK_NAME", 
  "PARENT", 
  "DELETED") AS 
select 
  job_no, 
  task_id, 
  status_id, 
  date_submitted,
  date_completed, 
  user_id, 
  isindexed, 
  access_id, 
  job_name, 
  lsid, 
  task_lsid, 
  task_name, 
  parent, 
  deleted
from 
  analysis_job
union select 
  job_no, 
  task_id, 
  status_id, 
  date_submitted,
  date_completed, 
  user_id, 
  isindexed, 
  access_id,
  job_name, 
  lsid, 
  task_lsid, 
  task_name, 
  parent, 
  deleted
from 
  analysis_job_archive;

