INSERT INTO CONFIG_KEY_VALUE_INTEGER(CONFIGURATION_KEY, CONFIGURATION_VALUE) VALUES ('CenterHierarchyExists',1);
INSERT INTO CONFIG_KEY_VALUE_INTEGER(CONFIGURATION_KEY, CONFIGURATION_VALUE) VALUES ('ClientCanExistOutsideGroup',0);
INSERT INTO CONFIG_KEY_VALUE_INTEGER(CONFIGURATION_KEY, CONFIGURATION_VALUE) VALUES ('GroupCanApplyLoans',0);

UPDATE DATABASE_VERSION SET DATABASE_VERSION = 157 WHERE DATABASE_VERSION = 156;