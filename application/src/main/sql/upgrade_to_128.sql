ALTER TABLE REPORT ADD COLUMN ACTIVITY_ID SMALLINT;
UPDATE REPORT SET ACTIVITY_ID = null;

update DATABASE_VERSION set DATABASE_VERSION = 128 where DATABASE_VERSION = 127;