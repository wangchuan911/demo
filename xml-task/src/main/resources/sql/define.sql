create table XML_TASK_TEMPLATE
(
    NAME varchar(100),
    CONTENT text,
    ID numeric(5) not null
        constraint XML_TASK_TEMPLATE_PK
            primary key
);
create table XML_TASK_CONNECT
(
    HOST varchar(500),
    PORT numeric(5),
    DB varchar(100),
    USR varchar(100),
    PW varchar(100),
    NAME varchar(100),
    MODEL varchar(20)
);