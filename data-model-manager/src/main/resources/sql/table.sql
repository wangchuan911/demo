create table MD_CONNECT
(
    URL         varchar(500)                        null,
    USER        varchar(100)                        null,
    PASSWORD    varchar(200)                        null,
    NAME        varchar(100)                        not null
        primary key,
    MODEL       int(4)                              null,
    CREATE_TIME timestamp default CURRENT_TIMESTAMP null
);

create table MD_DATA_TYPE
(
    ID   int auto_increment
        primary key,
    NAME varchar(20) null
);

create table MD_INSTANCE
(
    ID          int auto_increment
        primary key,
    code        varchar(200)                        null,
    name        varchar(200)                        null,
    object_id   int                                 null,
    parent_id   int                                 null,
    CREATE_TIME timestamp default CURRENT_TIMESTAMP null
);

create table MD_LINK
(
    LINK_ID      int                                 null,
    TYPE_ID      int                                 null,
    INSTANCE_ID  int                                 null,
    OBJECT_ID    int                                 null,
    ID           int auto_increment
        primary key,
    VALUE_ID     int                                 null,
    SEQUENCE     int(3)                              null,
    ATTRIBUTE_ID int                                 null,
    PARENT_ID    int       default 0                 null,
    `DESC`       varchar(200)                        null,
    CREATE_TIME  timestamp default CURRENT_TIMESTAMP null
);

create table MD_OBJECT
(
    ID                int auto_increment
        primary key,
    CODE              varchar(20)                         null,
    NAME              varchar(20)                         null,
    PARENT_ID         int                                 null,
    TYPE_ID           int                                 null,
    CONSTRUCT_LINK_ID int                                 null,
    CREATE_TIME       timestamp default CURRENT_TIMESTAMP null
);

create table MD_OBJECT_ATTR
(
    ID          int auto_increment
        primary key,
    OBJECT_ID   int                                 null,
    CODE        varchar(200)                        null,
    NAME        varchar(200)                        null,
    PARENT_ID   int                                 null,
    TYPE_ID     int                                 not null,
    CREATE_TIME timestamp default CURRENT_TIMESTAMP null
);

create table MD_VALUE
(
    ID            int auto_increment
        primary key,
    TYPE_ID       int                                 null,
    VALUE         varchar(4000)                       null,
    SEQUNECE      int(3)    default 0                 null,
    VALUE_TYPE_ID int                                 null,
    `DESC`        varchar(200)                        null,
    PARENT_ID     int                                 null,
    CREATE_TIME   timestamp default CURRENT_TIMESTAMP null
);
