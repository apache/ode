alter table BPEL_XML_DATA add DATA blob;

update BPEL_XML_DATA bxd set DATA=(select BIN_DATA from LARGE_DATA where id = bxd.ldata_id);
delete from LARGE_DATA ld where ld.id in (select ldata_id from BPEL_XML_DATA);
alter table BPEL_XML_DATA drop column ldata_id;

alter table BPEL_XML_DATA add SIMPLE_VALUE varchar2(255);
