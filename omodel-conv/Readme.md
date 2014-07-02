This sub-project is a helper to migrate from old OModel to new OModel(bpel-obj --> bpel-nobj)  
It mainly use spoon (http://spoon.gforge.inria.fr/) to analyze java source code which uses eclipse jdt backend.  
There are two converter currently. The converter are supported by processors in turn.  

After applying the converter, the sed scripts are used to handle some other things.  
sed_inst1 -- Convertor
sec_inst2 -- FieldAccConvertor
