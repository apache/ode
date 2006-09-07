package org.apache.ode.daohib.bpel.hobj;


import org.apache.ode.daohib.hobj.HObject;

/**
 * @hibernate.class table="BPEL_CORRELATOR_ENTRY"
 * @hibernate.discriminator column="CLSTYPE"
 * 
 */
public class HCorrelatorEntry extends HObject {
    private HCorrelator _correlator;

    private String _correlationKey;

    /**
     * @hibernate.property column="CORRELATION_KEY"
     * @hibernate.column name="CORRELATION_KEY"
     *                   unique-key="IDX_CORRELATOR_CORRELATION_KEY_UNIQ"
     *                   not-null="true"
     *                   
     */
    public String getCorrelationKey() {
        return _correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        _correlationKey = correlationKey;
    }

    /**
     * @hibernate.many-to-one
     * @hibernate.column name="CORRELATOR" index="IDX_CORRELATORENTRY_CORRELATOR"
     */
    public HCorrelator getCorrelator() {
        return _correlator;
    }

    public void setCorrelator(HCorrelator correlator) {
        _correlator = correlator;
    }

}
