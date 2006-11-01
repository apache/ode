package org.apache.ode.store.hobj;

/**
 * The property content is stored into a classic varchar if it's a simple type
 * and into a CLOB if it's mixed (so one of the two will always be null).
 * @hibernate.class table="BPEL_PROCESS_PROPERTY"
 */
public class HProcessProperty {

    private Long _id;
    private String _name;
    private String _namespace;
    private String _simpleContent;
    private String _mixedContent;
    private HProcessConf _process;

    public HProcessProperty() {
        super();
    }

    /**
     * @hibernate.id generator-class="native" column="ID"
     */
    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        _id = id;
    }

    /**
     * @hibernate.property column="PROPNAME"
     */
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    /**
     * @hibernate.property column="PROPNS"
     */
    public String getNamespace() {
        return _namespace;
    }

    public void setNamespace(String namespace) {
        _namespace = namespace;
    }

    /**
     * @hibernate.property column="SIMPLE_CNT"
     */
    public String getSimpleContent() {
        return _simpleContent;
    }

    public void setSimpleContent(String simpleContent) {
        _simpleContent = simpleContent;
    }

    /**
     * @hibernate.property
     *  column="MIXED_CNT"
     *  type="text"
     *  length="1000000000"
     */
    public String getMixedContent() {
        return _mixedContent;
    }

    public void setMixedContent(String mixedContent) {
        _mixedContent = mixedContent;
    }

    /**
     * @hibernate.many-to-one column="PROCESS_ID"
     */
    public HProcessConf getProcess() {
        return _process;
    }

    public void setProcess(HProcessConf process) {
        _process = process;
    }

}
