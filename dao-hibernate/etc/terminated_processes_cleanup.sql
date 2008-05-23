delete from BPEL_ACTIVITY_RECOVERY where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60));

delete from BPEL_CORRELATION_PROP where CORR_SET_ID in (select ID from BPEL_CORRELATION_SET where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60)));
delete from BPEL_CORRELATION_SET where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60));

delete from LARGE_DATA where ID in (select LDATA_EPR_ID from BPEL_MESSAGE_EXCHANGE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60)));
delete from LARGE_DATA where ID in (select LDATA_CEPR_ID from BPEL_MESSAGE_EXCHANGE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60)));
delete from LARGE_DATA where ID in (select DATA from BPEL_MESSAGE where MEX in (select REQUEST from BPEL_MESSAGE_EXCHANGE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60))));
delete from LARGE_DATA where ID in (select DATA from BPEL_MESSAGE where MEX in (select RESPONSE from BPEL_MESSAGE_EXCHANGE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60))));
delete from BPEL_MESSAGE where MEX in (select REQUEST from BPEL_MESSAGE_EXCHANGE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60)));
delete from BPEL_MESSAGE where MEX in (select RESPONSE from BPEL_MESSAGE_EXCHANGE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60)));
delete from BPEL_CORRELATOR_MESSAGE_CKEY where CORRELATOR_MESSAGE_ID in (select ID from BPEL_UNMATCHED where MEX in (select ID from BPEL_MESSAGE_EXCHANGE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60))));
delete from BPEL_UNMATCHED where MEX in (select ID from BPEL_MESSAGE_EXCHANGE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60)));
delete from BPEL_MEX_PROPS where MEX in (select ID from BPEL_MESSAGE_EXCHANGE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60)));
delete from BPEL_MESSAGE_EXCHANGE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60));

delete from LARGE_DATA where ID in (select LDATA_ID from BPEL_FAULT where ID in (select FAULT from BPEL_INSTANCE where STATE in (30,40,60)));
delete from BPEL_FAULT where ID in (select FAULT from BPEL_INSTANCE where STATE in (30,40,60));

delete from LARGE_DATA where ID in (select LDATA_ID from BPEL_XML_DATA where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60)));
delete from VAR_PROPERTY where XML_DATA_ID in (select ID from BPEL_XML_DATA where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60)));
delete from BPEL_XML_DATA where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60));

delete from BPEL_SELECTORS where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60));

delete from LARGE_DATA where ID in (select MYROLE_EPR from BPEL_PLINK_VAL where SCOPE in (select ID from BPEL_SCOPE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60))));
delete from LARGE_DATA where ID in (select PARTNERROLE_EPR from BPEL_PLINK_VAL where SCOPE in (select ID from BPEL_SCOPE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60))));
delete from BPEL_PLINK_VAL where SCOPE in (select ID from BPEL_SCOPE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60)));
delete from BPEL_SCOPE where PIID in (select ID from BPEL_INSTANCE where STATE in (30,40,60));

delete from LARGE_DATA where ID in (select LDATA_ID from BPEL_EVENT);
delete from BPEL_EVENT;

delete from BPEL_INSTANCE where STATE in (30,40,60);

