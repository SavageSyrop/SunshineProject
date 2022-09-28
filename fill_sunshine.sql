drop database IF EXISTS sunshineDB;
create DATABASE sunshineDB;
USE sunshineDB;
SET NAMES 'utf8';


create TABLE permissions (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
name varchar (32) NOT NULL,
role_id bigint REFERENCES roles(id)
);


create TABLE roles (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
name varchar (32) NOT NULL
);


create TABLE user_infos(
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
username varchar (64) NOT NULL,
password varchar (64) NOT NULL,
email varchar (64) NOT NULL,
first_name varchar (64) NOT NULL,
last_name varchar (64) NOT NULL,
gender_type varchar (64) NOT NULL,
date_of_birth date NOT NULL,
city varchar (64),
open_profile bit NOT NULL
);


create TABLE walls (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
post_permission varchar (32) NOT NULL,
comment_permission varchar (32) NOT NULL
);


create TABLE users (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
user_info_id bigint REFERENCES user_infos(id),
role_id bigint NOT NULL REFERENCES roles(id),
wall_id bigint NOT NULL REFERENCES walls(id),
activation_code varchar (255),
restore_password_code varchar (255),
is_banned bit NOT NULL DEFAULT false
);



create TABLE friend_requests (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
sender_id bigint REFERENCES users(id),
recipient_id bigint REFERENCES users(id),
is_accepted bit NOT NULL
);


create TABLE chats (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
name varchar (64) NOT NULL,
chat_type varchar (32) NOT NULL,
owner_id bigint REFERENCES users(id)
);

create TABLE messages (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
sender_id bigint  NOT NULL REFERENCES users(id),
text varchar (255) NOT NULL,
sending_time TIMESTAMP NOT NULL,
chat_id bigint REFERENCES chats(id)
);


create TABLE chat_participations (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
user_id bigint REFERENCES users(id),
chat_id bigint REFERENCES chats(id)
);

create TABLE table_groups (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
name varchar (64) NOT NULL,
info varchar (255) NOT NULL,
wall_type varchar (32) NOT NULL,
open_to_join bit NOT NULL
);

create TABLE group_memberships (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
user_id bigint REFERENCES users(id),
group_id bigint REFERENCES table_groups(id),
group_role varchar (32) NOT NULL
);

create TABLE posts (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
text varchar (255) NOT NULL,
sending_time TIMESTAMP NOT NULL,
author_id bigint REFERENCES users(id),
group_id bigint REFERENCES table_groups(id),
wall_id bigint REFERENCES walls(id),
is_published bit NOT NULL DEFAULT 0x01
);


create TABLE comments (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
author_id bigint REFERENCES users(id),
text varchar (255) NOT NULL,
sending_time TIMESTAMP NOT NULL,
post_id bigint REFERENCES posts(id)
);

create TABLE likes (
id bigint PRIMARY KEY NOT NULL AUTO_INCREMENT,
user_id bigint REFERENCES users(id),
sending_time TIMESTAMP NOT NULL,
comment_id bigint REFERENCES comments(id),
post_id bigint REFERENCES posts(id)
);

insert into permissions (id, name, role_id) values
(1, 'WRITING', 1),
(2, 'WRITING', 2),
(3, 'ADMIN_ACTIONS', 1);

insert into roles (id, name) values
(1, 'ADMIN'),
(2, 'USER');

insert into user_infos(id, username, password, email, first_name, last_name, gender_type, date_of_birth, city, open_profile) values
(1, 'desertfox', '$2a$10$dCKE0qv1SW3dKBTXkauFburkrCGOznBAhdXaV3Km9yre7qysphk1u','fess.2002@mail.ru', 'Yaroslav', 'Shevtsov', 'MALE', STR_TO_DATE('09.11.2001',"%d.%m.%Y"),'Tomsk', 0x01),
(2, 'aleoonka', '$2a$10$Uimw7bv5iTa.5miRSn4M4uGosxfyh1d89aVEHIkSNsPFkz6NmgOLq','project.sunshine@mail.ru', 'Alena', 'Tsemkalo', 'FEMALE', STR_TO_DATE('23.07.2002',"%d.%m.%Y"),'Moscow', 0x01),
(3, 'Gump', '$2a$10$s.HMSOhwXDkV6aK87ArD2.SBooCd4S9T1pAtMF.GygkMPRZaldyDK','project.sunshine@mail.ru', 'Forrest', 'Gump', 'KROGAN', STR_TO_DATE('09.05.1945',"%d.%m.%Y"),'Greenbow', 0x01),
(4,'Belka','$2a$10$R0o2eqVOcKpt.320Dr6rqujuXTSV87Pt.o0NydQafz3kDIzPnpzAO','project.sunshine@mail.ru','Squirel','Thompson','FEMALE', STR_TO_DATE('07.09.2022',"%d.%m.%Y"),'Las-Vegas',0x00),
(5,'BOT','$2a$10$ksNrhvJfhTKJ46mz2wLpa.dDJMjs0PtPh1tr7TjkqNRUwvcsPhNMO','project.sunshine@mail.ru','Roflan','Rabotyaga','KROGAN', STR_TO_DATE('01.01.2000',"%d.%m.%Y"),'Vinnitsa',0x00),
(6,'Reaper','$2a$10$O1alPtSNaqQ1FXvEGc/Y/.VJb9N2rZbRNN72uDEhXd4FLGO/isPZy','project.sunshine@mail.ru','The','Sovereign','MALE', STR_TO_DATE('13.03.37',"%d.%m.%Y"),'Citadel',0x01);

insert into walls(id, post_permission, comment_permission) values
(1,'OWNER','OWNER'),
(2,'FRIENDS','FRIENDS'),
(3,'ALL','ALL'),
(4,'OWNER','FRIENDS'),
(5,'FRIENDS','OWNER'),
(6,'OWNER','ALL');

insert into users(id, user_info_id, role_id, wall_id, activation_code, is_banned) values
(1, 1, 1, 1, null, 0x00),
(2, 2, 2, 2, NULL, 0x00),
(3, 3, 2, 3, NULL, 0x00),
(4, 4, 2, 4, NULL, 0x00),
(5, 5, 1, 5, NULL, 0x00),
(6, 6, 2, 6, NULL, 0x00);


insert into friend_requests (id, sender_id, recipient_id, is_accepted) values
(1,1,2,0x01),
(2,1,5,0x01),
(3,2,1,0x01),
(4,2,4,0x00),
(5,3,6,0x01),
(6,3,2,0x00),
(7,4,6,0x00),
(8,4,3,0x00),
(9,5,1,0x01),
(10,5,2,0x00),
(11,6,2,0x00),
(12,6,3,0x01);

insert into chats (id, name, chat_type, owner_id) values
(1,"Founders","PUBLIC",1),
(2,"Gump / Reaper", "PRIVATE", NULL);


insert into messages (id, sender_id, text, sending_time, chat_id) values
(1,1,"Хай, я создал чат с основателями!","2022-09-15 23:32:12",1),
(2,1,"Сегодня 15.09.2022, мы уложились в срок!","2022-09-15 23:32:34",1),
(3,2,"Ураааааа!!! Куда пойдем отмечать?","2022-09-15 23:32:50",1),
(4,5,"ОШиБкаАаааА, работа никогда не кончается!","2022-09-15 23:33:22",1),
(5,2,":(","2022-09-15 23:33:34",1),
(6,3,"Привет, я лесной болван!","2022-09-15 23:34:57",2),
(7,6,"This form is redundant!","2022-09-15 23:35:21",2);

insert into chat_participations (id, user_id, chat_id) values
(1,1,1),
(2,2,1),
(3,5,1),
(4,3,2),
(5,6,2);

insert into table_groups(id, name, info, wall_type, open_to_join) values
(1,"Welcome!","Hello world!","ALL",0x01),
(2,"Admin group","Closed","ADMINS",0x00),
(3,"Predloghka","Write and we will post!","OFFERED_POSTS",0x01);

insert into group_memberships (id, user_id, group_id, group_role) values
(1,6,1,"OWNER"),
(2,1,2,"OWNER"),
(3,5,2,"ADMIN"),
(4,3,2,"AWAITING_CHECK"),
(5,2,3,"OWNER"),
(6,4,3,"SUBSCRIBER"),
(7,2,1,"SUBSCRIBER");


insert into posts(id, text, sending_time, author_id, group_id, wall_id, is_published) values
(1,"We impose order on the chaos of organic evolution. You exist because we allow it, and you will end because we demand it!","2022-09-16 19:47:15",6,1,NULL,0x01),
(2,"Что за фанатик ведет эту группу?","2022-09-16 19:48:02",4,1,NULL,0x01),
(3,"СЛАВСЯ ВЕЛИКИЙ ВЛАСТЕЛИН! УДАР!","2022-09-16 19:48:24",2,1,NULL,0x01),
(4,"Отчёт BOT: В строю","2022-09-16 19:51:47",5,2,NULL,0x01),
(5,"Привет, солнышки!\nДелитесь своими увлекательными историями, оставляя их в предложенных постах, а я их опубликую!","2022-09-16 19:55:59",2,3,NULL,0x01),
(6,"Я маслопит!","2022-09-16 19:59:28",4,3,NULL,0x00),
(7,"Assuming direct control","2022-09-16 20:49:32",6,NULL,6,0x01),
(8,"desertfox был здесь","2022-09-16 21:14:23",1,NULL,2,0x01);

insert into comments (id, author_id, text, sending_time, post_id) values
(1,6,"You cannot resist!","2022-09-16 20:29:06",2),
(2,1,"Тебе там не темно?","2022-09-16 20:32:24",3),
(3,1,"Принято, BOT!","2022-09-16 20:33:26",4),
(4,4,"Круто, сейчас подам новость!","2022-09-16 20:36:02",5),
(5,4,"Сгинь!","2022-09-16 20:51:46",7),
(6,2,"aleoonka была здесь","2022-09-16 21:15:04",8);

insert into likes (id, user_id, sending_time, comment_id, post_id) values
(1,2,"2022-09-16 20:27:04",NULL,1),
(2,1,"2022-09-16 20:27:20",NULL,1),
(3,6,"2022-09-16 20:27:25",NULL,1),
(4,3,"2022-09-16 20:27:57",NULL,2),
(5,2,"2022-09-16 20:29:51",1,NULL),
(6,5,"2022-09-16 20:33:48",3,NULL),
(7,1,"2022-09-16 20:35:19",NULL,5),
(8,4,"2022-09-16 20:35:26",NULL,5),
(9,2,"2022-09-16 20:52:02",NULL,7),
(10,6,"2022-09-16 20:52:08",NULL,7),
(11,1,"2022-09-16 20:53:03",NULL,7),
(12,2,"2022-09-16 20:53:38",5,NULL),
(13,1,"2022-09-16 20:53:49",5,NULL),
(14,2,"2022-09-16 21:15:12",NULL,8),
(15,1,"2022-09-16 21:15:46",6,NULL);