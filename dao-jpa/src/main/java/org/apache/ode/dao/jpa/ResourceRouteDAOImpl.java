package org.apache.ode.dao.jpa;

import org.apache.ode.bpel.dao.ResourceRouteDAO;

import javax.persistence.*;

@Entity
@Table(name="ODE_RESOURCE_ROUTE")
public class ResourceRouteDAOImpl extends OpenJPADAO implements ResourceRouteDAO {

    @Id @Column(name="ID")
	@GeneratedValue(strategy= GenerationType.AUTO)
	private Long _id;
    @Basic @Column(name="URL", length=255, unique=true)
    private String _url;
    @Basic @Column(name="METHOD", length=8)
    private String _method;
    @Basic @Column(name="CHANNEL")
    private String _channelId;
    @Basic @Column(name="ROUTE_INDEX")
    private int _index;

    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="INSTANCE_ID")
    private ProcessInstanceDAOImpl _instance;

    public ResourceRouteDAOImpl() { }

    public ResourceRouteDAOImpl(String url, String method, String channelId, int index, ProcessInstanceDAOImpl instance) {
        _url = url;
        _channelId = channelId;
        _index = index;
        _instance = instance;
        _method = method;
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        _id = id;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public String getPickResponseChannel() {
        return _channelId;
    }

    public void setPickResponseChannel(String channelId) {
        _channelId = channelId;
    }

    public int getSelectorIdx() {
        return _index;
    }

    public void setSelectorIdx(int index) {
        _index = index;
    }

    public String getMethod() {
        return _method;
    }

    public void setMethod(String method) {
        _method = method;
    }

    public ProcessInstanceDAOImpl getInstance() {
        return _instance;
    }

    public void setInstance(ProcessInstanceDAOImpl instance) {
        _instance = instance;
    }
}
