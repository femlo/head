CREATE UNIQUE INDEX PRD_OFFERING_NAME_IDX ON PRD_OFFERING (PRD_OFFERING_NAME);

UPDATE DATABASE_VERSION SET DATABASE_VERSION = 243 WHERE DATABASE_VERSION = 242;