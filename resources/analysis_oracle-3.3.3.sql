-- record of user upload files
create table GS_ACCOUNT (
    GP_USERID varchar2(255),
    TOKEN varchar2 (255),
constraint gsa_pk primary key (GP_USERID),
constraint gsa_fk foreign key (GP_USERID) references GP_USER(USER_ID)
);

CREATE INDEX IDX_GSA_GP_USERID ON GS_ACCOUNT (GP_USERID);

-- update schema version
INSERT INTO PROPS (KEY, VALUE) VALUES ('registeredVersion3.3.3', '3.3.3');

COMMIT;






