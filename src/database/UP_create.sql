/*==============================================================*/
/* DBMS name:      InterBase 6.x                                */
/* Created on:     19.07.2013 13:44:53                          */
/*==============================================================*/


/*==============================================================*/
/* Table: messages                                              */
/*==============================================================*/
create table messages (
messages_id          INTEGER                        not null,
puser_r_id           INTEGER,
puser_s_id           INTEGER                        not null,
messages_date        TIMESTAMP                      not null,
messages_text        VARCHAR(256)                   not null,
messages_type        INTEGER                        not null,
constraint PK_MESSAGES primary key (messages_id)
);

/*==============================================================*/
/* Table: payments                                              */
/*==============================================================*/
create table payments (
payments_id          INTEGER                        not null,
puser_id             INTEGER                        not null,
payments_period      INTEGER                        not null,
payments_summa       INTEGER                        not null,
payments_date        TIMESTAMP                      not null,
constraint PK_PAYMENTS primary key (payments_id)
);

/*==============================================================*/
/* Table: puser                                                 */
/*==============================================================*/
create table puser (
puser_id             INTEGER                        not null,
owner_puser_id       INTEGER,
puser_email          VARCHAR(210)                   not null,
puser_login          VARCHAR(60)                    not null,
puser_pswd           VARCHAR(512)                   not null,
puser_dbname         VARCHAR(256)                   not null,
puser_boss           INTEGER                        not null,
puser_created        TIMESTAMP                      not null,
puser_lastaccess     TIMESTAMP                      not null,
puser_lastaccess_device VARCHAR(20),
puser_lock           INTEGER                        not null,
puser_lock_reason    VARCHAR(256),
puser_tarif          INTEGER                        not null,
puser_flags          INTEGER                        default 0,
puser_taxtype        INTEGER                        not null,
puser_taxpercent     INTEGER                        default 15 not null,
constraint PK_PUSER primary key (puser_id),
constraint AK_KEY_2_PUSER unique (puser_email)
);

insert into puser values (-2,NULL,'ipp@rbc.vrt.hbo.com','Норинский Альберт','875b854107b408d2899cce9dff917e70','',0,'NOW','NOW',NULL,0,NULL,0,0,1,15);
insert into puser values (-1,NULL,'ipplan2013@gmail.com','ipplan2013','875b854107b408d2899cce9dff917e70','',0,'NOW','NOW',NULL,0,NULL,1,0,2,15);
commit;

/*==============================================================*/
/* Table: sync                                                  */
/*==============================================================*/
create table sync (
sync_id              INTEGER                        not null,
puser_id             INTEGER                        not null,
sync_imei            VARCHAR(20)                    not null,
sync_last            TIMESTAMP                      not null,
constraint PK_SYNC primary key (sync_id)
);

alter table messages
   add constraint FK_MESSAGES_REFERENCE_PUSER_R foreign key (puser_r_id)
      references puser (puser_id)
      on delete set null
      on update set null;

alter table messages
   add constraint FK_MESSAGES_REFERENCE_PUSER_S foreign key (puser_s_id)
      references puser (puser_id)
      on delete cascade
      on update cascade;

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

