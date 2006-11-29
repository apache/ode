package org.apache.ode.store.hib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.store.ProcessConfDAO;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.UnaryFunction;

/**
 * @author mriou <mriou at apache dot org>
 * @hibernate.class table="STORE_PROCESS"
 */
public class ProcessConfDaoImpl extends HibObj implements ProcessConfDAO {

    private DeploymentUnitDaoImpl _du;
    
    /** {@link HProcessProperty}s for this process. */
    private Map<String,String> _properties = new HashMap<String,String>();

    /** Simple name of the process. */
    private String _processId;

    /** Process type. */
    private String _type;

    
    /** Process version. */
    private int _version;

    /** Process state.*/
    private String _state;


    /**
     * @hibernate.many-to-one 
     * @hibernate.column name="DU"
     */
    public DeploymentUnitDaoImpl getDeploymentUnit() {
        return _du;
    }
    
    public void setDeploymentUnit(DeploymentUnitDaoImpl du) {
        _du = du;
    }
    /**
     * @hibernate.map table="STORE_PROCESS_PROP" role="properties_"
     * @hibernate.collection-key column="propId" 
     * @hibernate.collection-index column="name" type="string" 
     * @hibernate.collection-element column="value" type="string"
     */
    public Map<String,String> getProperties_() {
        return _properties;
    }

    public void setProperties_(Map<String,String> properties) {
        _properties = properties;
    }

    /**
     *
     * @hibernate.id generator-class="assigned"
     * @hibernate.column
     *  name="PID"
     *  not-null="true"
     */
    public String getPID_() {
        return _processId;
    }

    public void setPID_(String processId) {
        _processId = processId;
    }


    /**
     * The type of the process (BPEL process definition name).
     * @hibernate.property
     *     column="TYPE"
     */
    public String getType_() {
        return _type;
    }

    public void setType_(String type) {
        _type = type;
    }


    /**
     * The process version.
     * @hibernate.property
     *    column="version"
     */
    public int getVersion() {
        return _version;
    }

    public void setVersion(int version) {
        _version = version;
    }

    /**
     * The process state.
     * @hibernate.property
     *    column="STATE"
     */
    public String getState_() {
        return _state;
    }

    public void setState_(String state) {
        _state = state;
    }


    public QName getPID() {
        return QName.valueOf(getPID_());
    }

    public void setPID(QName pid) {
        setPID_(pid.toString());
    }
    
    public void setState(ProcessState state) {
        setState_(state.toString());
    }

    public void setProperty(QName name, String content) {
        _properties.put(name.toString(),content);
    }

    public void delete() {
        super.delete();
    }

    public QName getType() {
        return QName.valueOf(getType_());
    }

    public void setType(QName type) {
        setType_(type.toString());
    }

    public ProcessState getState() {
        return ProcessState.valueOf(getState_());
    }
   
    public String getProperty(QName name) {
        return _properties.get(name.toString());
    }

    public Collection<QName> getPropertyNames() {
        return CollectionsX.transform(new ArrayList<QName>(), _properties.keySet(),new UnaryFunction<String,QName>() {
            public QName apply(String x) {
                return QName.valueOf(x);
            }
            
        });
    }


}
