update LOOKUP_ENTITY set ENTITY_NAME= 'AccountState' where ENTITY_ID= 5;
update LOOKUP_ENTITY set ENTITY_NAME= 'SavingsStatus' where ENTITY_ID= 72;

update LOOKUP_VALUE set LOOKUP_NAME= 'LOAN_PARTIAL_APPLICATION' where LOOKUP_ID= 17;
update LOOKUP_VALUE set LOOKUP_NAME= 'LOAN_PENDING_APPROVAL' where LOOKUP_ID= 18;
update LOOKUP_VALUE set LOOKUP_NAME= 'LOAN_APPROVED' where LOOKUP_ID= 19;
update LOOKUP_VALUE set LOOKUP_NAME= 'LOAN_DISBURSED_TO_LOAN_OFFICER' where LOOKUP_ID= 20;
update LOOKUP_VALUE set LOOKUP_NAME= 'LOAN_ACTIVE_IN_GOOD_STANDING' where LOOKUP_ID= 21;
update LOOKUP_VALUE set LOOKUP_NAME= 'LOAN_CLOSED_OBLIGATIONS_MET' where LOOKUP_ID= 22;
update LOOKUP_VALUE set LOOKUP_NAME= 'LOAN_CLOSED_WRITTEN_OFF' where LOOKUP_ID= 23;
update LOOKUP_VALUE set LOOKUP_NAME= 'LOAN_CLOSED_RESCHEDULED' where LOOKUP_ID= 24;
update LOOKUP_VALUE set LOOKUP_NAME= 'LOAN_ACTIVE_IN_BAD_STANDING' where LOOKUP_ID= 25;

update LOOKUP_VALUE set LOOKUP_NAME= 'LOAN_CANCELLED' where LOOKUP_ID= 141;
update LOOKUP_VALUE set LOOKUP_NAME= 'CUSTOMER_ACCOUNT_ACTIVE' where LOOKUP_ID= 142;
update LOOKUP_VALUE set LOOKUP_NAME= 'CUSTOMER_ACCOUNT_INACTIVE' where LOOKUP_ID= 143;

update LOOKUP_VALUE set LOOKUP_NAME= 'SAVINGS_PARTIAL_APPLICATION' where LOOKUP_ID= 181;
update LOOKUP_VALUE set LOOKUP_NAME= 'SAVINGS_PENDING_APPROVAL' where LOOKUP_ID= 182;
update LOOKUP_VALUE set LOOKUP_NAME= 'SAVINGS_CANCELLED' where LOOKUP_ID= 183;
update LOOKUP_VALUE set LOOKUP_NAME= 'SAVINGS_ACTIVE' where LOOKUP_ID= 184;
update LOOKUP_VALUE set LOOKUP_NAME= 'SAVINGS_CLOSED' where LOOKUP_ID= 185;
update LOOKUP_VALUE set LOOKUP_NAME= 'SAVINGS_INACTIVE' where LOOKUP_ID= 210;


UPDATE DATABASE_VERSION SET DATABASE_VERSION = 161 WHERE DATABASE_VERSION = 160;
