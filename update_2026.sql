create table if not exists tardoc (
   lastupdate bigint,
   id varchar(40),
   deleted char(1),
   parent varchar(32),
   digniQuanti varchar(5),
   digniQuali varchar(255),
   sparte varchar(4),
   gueltigVon varchar(8),
   gueltigBis varchar(8),
   nickname varchar(25),
   tx255 varchar(255),
   code varchar(25),
   law varchar(3),
   isChapter char(1)
);
create table if not exists ch_elexis_arzttarife_ch_ambulantepauschalen(
   lastupdate bigint,
   id varchar(40),
   deleted char(1),
   code varchar(25),
   validFrom char(8),
   validTo char(8),
   tp char(8),
   digniQuali varchar(255),
   chapter varchar(255),
   text varchar(255),
   typ varchar(15)
);

create table if not exists tardoc_extension(
   lastupdate bigint,	
   id varchar(40),
   deleted char(1),
   code varchar(32),
   limits blob,
   med_interpret blob,
   tech_interpret blob
);
create table if not exists tardoc_group(
   lastupdate bigint,
   id varchar(40),
   deleted char(1),
   groupName varchar(32),
   services blob,
   law char(3),
   validFrom char(8),
   validTo char(8)
);

create table if not exists tardoc_definitionen(
   lastupdate bigint,
   id varchar(40),
   deleted char(1),
   spalte varchar(20),
   kuerzel varchar(5),
   titel varchar(255),
   law char(3)
);

create table if not exists tardoc_kumulation(
   lastupdate bigint,
   id varchar(40),
   deleted char(1),
   masterCode varchar(25),
   masterArt char(1),
   slaveCode varchar(25),
   slaveArt char(1),
   typ char(1),
   view char(1),
   validSide char(1),
   validFrom char(8),
   validTo char(8),
   law char(3)
);

alter table ch_elexis_arzttarife_ch_physio add column law varchar(3);
alter table output_log add column creatorid varchar(40);
alter table reminders add column groupid varchar(40);
ALTER TABLE `artikelstamm_ch` 
CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
