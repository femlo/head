-- Ordinarily we'd need to preserve the data in the tables.
-- But since surveys aren't being used yet, we feel OK in
-- just doing it this way (the CHANGE COLUMN doesn't get along
-- with the foreign keys).
-- 
-- With that exception (that the CHANGE COLUMN doesn't quite
-- work), what we are doing should be equivalent to:
-- ALTER TABLE SURVEY_INSTANCE MODIFY COLUMN OFFICER_ID SMALLINT;
-- ALTER TABLE SURVEY_INSTANCE CHANGE COLUMN CLIENT_ID CUSTOMER_ID INTEGER;
--ALTER TABLE SURVEY_INSTANCE ADD COLUMN ACCOUNT_ID INTEGER;
--
--ALTER TABLE SURVEY_INSTANCE ADD FOREIGN KEY (ACCOUNT_ID)
--  REFERENCES ACCOUNT(ACCOUNT_ID)
--    ON DELETE NO ACTION
--    ON UPDATE NO ACTION;
  
drop table SURVEY_RESPONSE;
drop table SURVEY_INSTANCE;

CREATE TABLE SURVEY_INSTANCE (
  INSTANCE_ID INTEGER AUTO_INCREMENT NOT NULL,
  SURVEY_ID INTEGER NOT NULL,
  CUSTOMER_ID INTEGER,
  OFFICER_ID SMALLINT,
  DATE_CONDUCTED DATE NOT NULL,
  COMPLETED_STATUS INTEGER NOT NULL,
  ACCOUNT_ID INTEGER,
  PRIMARY KEY(INSTANCE_ID),
  FOREIGN KEY(SURVEY_ID)
    REFERENCES SURVEY(SURVEY_ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(CUSTOMER_ID) 
    REFERENCES CUSTOMER(CUSTOMER_ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(OFFICER_ID)
    REFERENCES PERSONNEL(PERSONNEL_ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(ACCOUNT_ID)
    REFERENCES ACCOUNT(ACCOUNT_ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE TABLE SURVEY_RESPONSE (
  RESPONSE_ID INTEGER AUTO_INCREMENT NOT NULL,
  INSTANCE_ID INTEGER NOT NULL,
  QUESTION_ID INTEGER NOT NULL,

  FREETEXT_VALUE TEXT,
  CHOICE_VALUE INTEGER,
  DATE_VALUE DATE,
  NUMBER_VALUE DECIMAL(16,5),

  UNIQUE(INSTANCE_ID, QUESTION_ID),
  PRIMARY KEY(RESPONSE_ID),
  FOREIGN KEY(QUESTION_ID)
    REFERENCES QUESTIONS(QUESTION_ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(INSTANCE_ID)
    REFERENCES SURVEY_INSTANCE(INSTANCE_ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(CHOICE_VALUE)
    REFERENCES QUESTION_CHOICES(CHOICE_ID)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

update DATABASE_VERSION set DATABASE_VERSION = 125 where DATABASE_VERSION = 124;
