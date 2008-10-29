package org.apache.ode.daohib.bpel.hobj;

/**
 * @hibernate.class table="BPEL_RES_ROUTE"
 */
public class HResourceRoute extends HObject {

    private String _url;
    private String _method;
    private String _channelId;
    private int _index;
    private HProcessInstance _instance;

    public HResourceRoute() {
        super();
    }

    /**
     * @hibernate.property column="URL" not-null="true" size="255"
     */
    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    /**
     * @hibernate.property column="METHOD" not-null="true" size="8"
     */
    public String getMethod() {
        return _method;
    }

    public void setMethod(String method) {
        _method = method;
    }

    /**
     * @hibernate.property column="CHANNEL" not-null="true"
     */
    public String getChannelId() {
        return _channelId;
    }

    public void setChannelId(String channelId) {
        _channelId = channelId;
    }

    /**
     * @hibernate.property column="INDEX" not-null="true"
     */
    public int getIndex() {
        return _index;
    }

    public void setIndex(int index) {
        _index = index;
    }

    /**
     * @hibernate.many-to-one column="INSTANCE"
     */
    public HProcessInstance getInstance() {
        return _instance;
    }

    public void setInstance(HProcessInstance instance) {
        _instance = instance;
    }
}
