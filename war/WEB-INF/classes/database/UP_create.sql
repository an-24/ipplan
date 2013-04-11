/*==============================================================*/
/* DBMS name:      InterBase 6.x                                */
/* Created on:     07.04.2013 18:26:11                          */
/*==============================================================*/


/*==============================================================*/
/* Table: payments                                              */
/*==============================================================*/
create table payments (
payments_id          INTEGER                        not null,
puser_id             INTEGER                        not null,
payments_period      INTEGER                        not null,
payments_summa       INTEGER                        not null,
payments_date        DATE                           not null,
constraint PK_PAYMENTS primary key (payments_id)
);

/*==============================================================*/
/* Table: puser                                                 */
/*==============================================================*/
create table puser (
puser_id             INTEGER                        not null,
owner_puser_id       INTEGER,
puser_email          VARCHAR(320)                   not null,
puser_login          VARCHAR(60)                    not null,
puser_pswd           VARCHAR(512)                   not null,
puser_dbname         VARCHAR(256)                   not null,
puser_boss           INTEGER                        not null,
puser_created        DATE                           not null,
puser_lastaccess     DATE                           not null,
puser_lastaccess_device VARCHAR(20),
puser_lock           INTEGER                        not null,
puser_lock_reason    VARCHAR(256),
puser_trial          INTEGER                        not null,
constraint PK_PUSER primary key (puser_id),
constraint AK_KEY_2_PUSER unique (puser_email)
);

insert into puser values (-1,NULL,'ipplan2013@gmail.com','ipplan2013','rol','ipplan2013-gmail-com',1,'NOW','NOW',NULL,0,NULL,1);
commit;

/*==============================================================*/
/* Table: sync                                                  */
/*==============================================================*/
create table sync (
sync_id              INTEGER                        not null,
puser_id             INTEGER                        not null,
sync_imei            VARCHAR(20)                    not null,
sync_last            DATE                           not null,
constraint PK_SYNC primary key (sync_id)
);

alter table payments
   add constraint FK_PAYMENTS_REFERENCE_PUSER foreign key (puser_id)
      references puser (puser_id)
      on delete cascade
      on update cascade;

alter table puser
   add constraint FK_PUSER_REFERENCE_PUSER foreign key (owner_puser_id)
      references puser (puser_id)
      on delete cascade
      on update cascade;

alter table sync
   add constraint FK_SYNC_REFERENCE_PUSER foreign key (puser_id)
      references puser (puser_id)
      on delete cascade
      on update cascade;

