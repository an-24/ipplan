/*==============================================================*/
/* DBMS name:      InterBase 6.x                                */
/* Created on:     08.07.2013 10:03:22                          */
/*==============================================================*/


/*==============================================================*/
/* Table: agreed                                                */
/*==============================================================*/
create table agreed (
agreed_id            INTEGER                        not null,
bargain_id           INTEGER                        not null,
agreed_date          DATE                           not null,
agreed_note          VARCHAR(200),
constraint PK_AGREED primary key (agreed_id)
);

/*==============================================================*/
/* Table: bargain                                               */
/*==============================================================*/
create table bargain (
bargain_id           INTEGER                        not null,
bargain_name         VARCHAR(220)                   not null,
puser_id             INTEGER                        not null,
root_bargain_id      INTEGER                        not null,
status_id            INTEGER,
customer_id          INTEGER                        not null,
contract_id          INTEGER,
bargain_ver          INTEGER                        default 0 not null,
bargain_start        TIMESTAMP                      not null,
bargain_finish       TIMESTAMP                      not null,
bargain_revenue      INTEGER,
bargain_prepayment   INTEGER,
bargain_costs        INTEGER,
bargain_payment_costs INTEGER,
bargain_fine         INTEGER,
bargain_tax          INTEGER,
bargain_head         INTEGER,
bargain_created      TIMESTAMP                      not null,
bargain_visible      INTEGER                        default 1 not null,
bargain_note         VARCHAR(460),
constraint PK_BARGAIN primary key (bargain_id),
constraint AK_KEY_2_BARGAIN unique (root_bargain_id, bargain_ver)
);

/*==============================================================*/
/* Table: bargaincosts                                          */
/*==============================================================*/
create table bargaincosts (
bargaincosts_id      INTEGER                        not null,
costs_id             INTEGER                        not null,
bargain_id           INTEGER                        not null,
bargaincosts_value   INTEGER                        not null,
bargaincosts_payment INTEGER,
bargaincosts_note    VARCHAR(120),
constraint PK_BARGAINCOSTS primary key (bargaincosts_id)
);

/*==============================================================*/
/* Table: calendar                                              */
/*==============================================================*/
create table calendar (
bargain_id           INTEGER                        not null,
calendar_google_id   VARCHAR(256),
constraint PK_CALENDAR primary key (bargain_id)
);

/*==============================================================*/
/* Table: contract                                              */
/*==============================================================*/
create table contract (
contract_id          INTEGER                        not null,
customer_id          INTEGER                        not null,
contract_name        VARCHAR(256)                   not null,
contract_data        BLOB,
contract_uri         VARCHAR(2048),
constraint PK_CONTRACT primary key (contract_id)
);

/*==============================================================*/
/* Table: costs                                                 */
/*==============================================================*/
create table costs (
costs_id             INTEGER                        not null,
costs_sortcode       INTEGER                        not null,
costs_name           VARCHAR(120)                   not null,
constraint PK_COSTS primary key (costs_id),
constraint AK_KEY_2_COSTS unique (costs_name)
);

insert into costs values(1,2,'Сырье и материалы');
insert into costs values(2,1,'Расходы на хранение, подработку, подсортировку и упаковку товаров');
insert into costs values(3,3,'Транспортные расходы');
insert into costs values(4,6,'Охрана');
insert into costs values(5,5,'Аренда');
insert into costs values(6,4,'Командировочные и представительские расходы');
insert into costs values(7,8,'Затраты по оплате процентов за пользование кредитами и займами');
insert into costs values(8,7,'Потери товаров и технологические отходы');
insert into costs values(9,9,'Расходы на тару');
insert into costs values(10,10,'Прочие расходы');
commit;





/*==============================================================*/
/* Table: customer                                              */
/*==============================================================*/
create table customer (
customer_id          INTEGER                        not null,
customer_name        VARCHAR(256)                   not null,
customer_lookup_key  VARCHAR(256),
customer_primary_email VARCHAR(256),
customer_emails      VARCHAR(256),
customer_primary_phone VARCHAR(256),
customer_phones      VARCHAR(256),
customer_company     VARCHAR(120),
customer_position    VARCHAR(120),
customer_birthday    TIMESTAMP,
customer_photo       VARCHAR(8190),
customer_lastupdate  TIMESTAMP,
customer_visible     INTEGER                        default 1 not null,
customer_IMs         VARCHAR(256),
constraint PK_CUSTOMER primary key (customer_id)
);

/*==============================================================*/
/* Table: filelinks                                             */
/*==============================================================*/
create table filelinks (
filelinks_id         INTEGER                        not null,
bargain_id           INTEGER                        not null,
provider_id          INTEGER                        not null,
filelinks_name       VARCHAR(120)                   not null,
filelinks_uri        VARCHAR(320)                   not null,
constraint PK_FILELINKS primary key (filelinks_id)
);

/*==============================================================*/
/* Table: provider                                              */
/*==============================================================*/
create table provider (
provider_id          INTEGER                        not null,
provider_name        VARCHAR(60)                    not null,
provider_token       VARCHAR(100),
provider_refresh_token VARCHAR(100),
provider_expires_in  INTEGER,
provider_granted     TIMESTAMP,
constraint PK_PROVIDER primary key (provider_id),
constraint AK_KEY_2_PROVIDER unique (provider_name)
);

insert into provider(provider_id,provider_name) values(1,'Google Disk');
insert into provider(provider_id,provider_name) values(2,'Dropbox');
commit;

/*==============================================================*/
/* Table: puser                                                 */
/*==============================================================*/
create table puser (
puser_id             INTEGER                        not null,
owner_puser_id       INTEGER,
puser_login          VARCHAR(60)                    not null,
puser_email          VARCHAR(320)                   not null,
puser_taxtype        INTEGER                        not null,
puser_google_token   VARCHAR(100),
puser_google_refresh_token VARCHAR(100),
puser_google_expires_in INTEGER,
puser_google_granted TIMESTAMP,
puser_contact_lastsync TIMESTAMP,
puser_calendar_lastsync TIMESTAMP,
puser_contact_sync_duration INTEGER                        default 0 not null,
puser_calendar_sync_duration INTEGER                        default 0 not null,
puser_googlecalendar_id VARCHAR(250),
constraint PK_PUSER primary key (puser_id),
constraint AK_KEY_2_PUSER unique (puser_email)
);

/*==============================================================*/
/* Table: status                                                */
/*==============================================================*/
create table status (
status_id            INTEGER                        not null,
puser_id             INTEGER                        not null,
status_name          VARCHAR(60)                    not null,
status_daylimit      INTEGER,
constraint PK_STATUS primary key (status_id)
);

/*==============================================================*/
/* Table: task                                                  */
/*==============================================================*/
create table task (
task_id              INTEGER                        not null,
bargain_id           INTEGER                        not null,
tasktype_id          INTEGER                        not null,
after_status_id      INTEGER,
task_name            VARCHAR(240)                   not null,
task_deadline        TIMESTAMP                      not null,
task_start           TIMESTAMP,
task_warning_duration INTEGER,
task_warning_unit    INTEGER,
task_place           VARCHAR(200),
task_executed        INTEGER                        default 0 not null,
task_lastupdate      TIMESTAMP,
constraint PK_TASK primary key (task_id)
);

/*==============================================================*/
/* Table: tasktype                                              */
/*==============================================================*/
create table tasktype (
tasktype_id          INTEGER                        not null,
tasktype_name        VARCHAR(40),
constraint PK_TASKTYPE primary key (tasktype_id)
);

insert into tasktype values(1,'Написать электронное письмо');
insert into tasktype values(2,'Позвонить');
insert into tasktype values(3,'Подготовить документы');
insert into tasktype values(4,'Отправить документы по почте');
insert into tasktype values(5,'Встреча/Совещание');
insert into tasktype values(6,'Изготовить/Поставить');
insert into tasktype values(7,'Другое');
commit;

alter table agreed
   add constraint FK_AGREED_REFERENCE_BARGAIN foreign key (bargain_id)
      references bargain (bargain_id)
      on delete cascade
      on update cascade;

alter table bargain
   add constraint FK_BARGAIN_REFERENCE_BARGAIN foreign key (root_bargain_id)
      references bargain (bargain_id)
      on delete cascade
      on update cascade;

alter table bargain
   add constraint FK_BARGAIN_REFERENCE_PUSER foreign key (puser_id)
      references puser (puser_id);

alter table bargain
   add constraint FK_BARGAIN_REFERENCE_STATUS foreign key (status_id)
      references status (status_id);

alter table bargain
   add constraint FK_BARGAIN_REFERENCE_CUSTOMER foreign key (customer_id)
      references customer (customer_id);

alter table bargain
   add constraint FK_BARGAIN_REFERENCE_CONTRACT foreign key (contract_id)
      references contract (contract_id);

alter table bargaincosts
   add constraint FK_BARGAINC_REFERENCE_COSTS foreign key (costs_id)
      references costs (costs_id);

alter table bargaincosts
   add constraint FK_BARGAINC_REFERENCE_BARGAIN foreign key (bargain_id)
      references bargain (bargain_id);

alter table calendar
   add constraint FK_CALENDAR_REFERENCE_BARGAIN foreign key (bargain_id)
      references bargain (bargain_id)
      on delete cascade
      on update cascade;

alter table contract
   add constraint FK_CONTRACT_REFERENCE_CUSTOMER foreign key (customer_id)
      references customer (customer_id);

alter table filelinks
   add constraint FK_FILELINK_REFERENCE_BARGAIN foreign key (bargain_id)
      references bargain (bargain_id)
      on delete cascade
      on update cascade;

alter table filelinks
   add constraint FK_FILELINK_REFERENCE_PROVIDER foreign key (provider_id)
      references provider (provider_id);

alter table puser
   add constraint FK_PUSER_REFERENCE_PUSER foreign key (owner_puser_id)
      references puser (puser_id);

alter table status
   add constraint FK_STATUS_REFERENCE_PUSER foreign key (puser_id)
      references puser (puser_id)
      on delete cascade
      on update cascade;

alter table task
   add constraint FK_TASK_REFERENCE_CALENDAR foreign key (bargain_id)
      references calendar (bargain_id)
      on delete cascade
      on update cascade;

alter table task
   add constraint FK_TASK_REFERENCE_TASKTYPE foreign key (tasktype_id)
      references tasktype (tasktype_id);

alter table task
   add constraint FK_TASK_REFERENCE_STATUS foreign key (after_status_id)
      references status (status_id)
      on delete set null
      on update set null;

