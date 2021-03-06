-- record of user upload files, generated by [hibernatetool] 
create table user_upload (
    id bigint generated by default as identity (start with 1), 
    user_id varchar(255), 
    path varchar(255), 
    name varchar(255), 
    last_modified timestamp, 
    file_length bigint, 
    extension varchar(255), 
    kind varchar(255), 
    num_parts integer, 
    num_parts_recd integer, 
    primary key (id),
    unique (user_id, path)
);
create index idx_user_upload_user_id on user_upload (user_id);

-- for GenomeSpace integration, link GP user account to GS user account
create table GS_ACCOUNT (
    -- use the File.canonicalPath as the primary key
    GP_USERID varchar (255) primary key references GP_USER(USER_ID),
    -- owner of the file
    TOKEN varchar (255)
);

-- improve performance by creating indexes on the ANALYSIS_JOB table
CREATE INDEX IDX_AJ_STATUS ON ANALYSIS_JOB(STATUS_ID);
CREATE INDEX IDX_AJ_PARENT ON ANALYSIS_JOB(PARENT);

-- for SGE integration
CREATE TABLE JOB_SGE
(
    GP_JOB_NO INTEGER NOT NULL,
    SGE_JOB_ID VARCHAR(4000),
    SGE_SUBMIT_TIME TIMESTAMP,
    SGE_START_TIME TIMESTAMP,
    SGE_END_TIME TIMESTAMP,
    SGE_RETURN_CODE INTEGER,
    SGE_JOB_COMPLETION_STATUS VARCHAR(4000)
);

CREATE INDEX IDX_JOB_SGE_GP_JOB_NO ON JOB_SGE (GP_JOB_NO);
CREATE INDEX IDX_SGE_JOB_ID on JOB_SGE (SGE_JOB_ID);
