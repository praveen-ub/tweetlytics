drop database pubsub_phase2;

create database pubsub_phase2;

use pubsub_phase2;

create table publisher (

    id bigint not null auto_increment,
    webhook_url varchar(255) not null,
    nick_name varchar(255),
    is_active tinyint(1) default 1,
    primary key(id)
);

create table subscriber (
    id bigint not null auto_increment,
    webhook_url varchar(255) not null,
    nick_name varchar(255),
    is_active tinyint(1) default 1,
    primary key(id)
);

create table topic (

    id bigint not null auto_increment,
    name varchar(255),
    is_deleted tinyint default 0,
    primary key(id)
);

create table message(

    id bigint not null auto_increment,
    message_id varchar(255) not null,
    content text not null,
    publisher_id bigint not null,
    topic_id bigint not null,
    is_deleted tinyint(1),
    primary key(id),
    foreign key (publisher_id) references publisher(id),
    foreign key(topic_id) references topic(id)
);

create table subscription(

    id bigint not null auto_increment,
    subscriber_id bigint not null,
    topic_id bigint not null,
    primary key(id),
    foreign key (subscriber_id) references subscriber(id),
    foreign key (topic_id) references topic(id)
);
