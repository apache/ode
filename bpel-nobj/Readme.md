OModel 
=====
  Basically, the OModel class uses a map as container of all fields, and use jackson for serialization to / deserialization from json/smile.
  When serialize, the map are serialized on behalf of the class it stands. When deserialize, the whole json are deserialized to OProcessWrapper,
  and then construct OModel classes from map container(i.e. when getters are called). 
  a simple example of serialized json:
  
	{  
		"MAGIC":"VTVTAE9GSCAUBSk=",  
		"FORMAT":16,  
		"COMPILE_TIME":1401935206665,  
		"OTHER_HEADERS":{},  
		"PROCESS":{  
			...  
		}  
	}  
 
 So, getters and setters are responsible for the map <-> OModel class transition. More precisely, getField() method will locate the field in map
 and construct the OModel class from its value. setField(OBase obj) will extract the map representation of obj and put it into the map.  
 For example, getters and setters might like this:  
 
	OProcess getProcess(){  
		Map process = fieldContainer.get("PROCESS");  
		return new OProcess(process);  
	}  
	
	void setProcess(OProcess oProcess){  
		Map process = oProcess.getMapRepr();  
		fieldContainer.put("PROCESS", process);  
	}  
	
  Please refer to the source for more information :). 