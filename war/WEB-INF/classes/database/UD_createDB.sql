SET AUTO ON;
SET SQL DIALECT 3;
SET NAMES WIN1251;
CREATE DATABASE 'D:\\DATABASE\\ipplan_ud.fdb' user 'SYSDBA' password 'masterkey' PAGE_SIZE 8192
DEFAULT CHARACTER SET UTF8 COLLATION UTF8;
CONNECT 'D:\\DATABASE\\ipplan_ud.fdb' user 'SYSDBA' password 'masterkey';


/*==============================================================*/
/* Generator: NewRecordId                                       */
/*==============================================================*/
CREATE GENERATOR NewRecordId;
SET GENERATOR NewRecordId TO 0;
