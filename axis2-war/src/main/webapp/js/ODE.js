/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var baseURL;
if (location.host.indexOf('/') == -1 && location.protocol.indexOf('/') == -1) {
    baseURL = location.protocol + "//" + location.host + "/";
}else if(location.host.indexOf('/') != -1 && location.protocol.indexOf('/') == -1){
    baseURL = location.protocol + "//" + location.host;
}
var baseDirectoryName = location.pathname.substring(0,location.pathname.indexOf('/',1));
if(baseDirectoryName.indexOf('/') == 0){
    baseDirectoryName = baseDirectoryName.substring(1);
}
var baseDirectoryURL = baseURL + baseDirectoryName;

debug = function (log_txt) {
    if (window.console != undefined) {
        console.log(log_txt);
    }
}

var org;
if (!org) {
    org = {};
}
else if (typeof org != "object") {
        throw new Error("org already exists and is not an object");
}

if (!org.apache) {
    org.apache = {};
}
else if (typeof org.apache != "object") {
        throw new Error("org.apache already exists and is not an object");
}

if (!org.apache.ode) {
    org.apache.ode = {};
}
else if (typeof org.apache.ode != "object") {
        throw new Error("org.apache.ode already exists");
}

if(org.apache.ode.XHRObject){
    throw new Error("org.apache.ode.XHRObject already exists");
}

org.apache.ode.XHRObject = {};

(function (){
    var msxml_progid = [
        'Microsoft.XMLHTTP',
        'MSXML2.XMLHTTP.3.0',
        'MSXML2.XMLHTTP'
        ]
    function createXhrObject(){
        var xhr;
        try
        {
            // Instantiates XMLHttpRequest in non-IE browsers and assigns to http.
            xhr = new XMLHttpRequest();
        }
        catch(e)
        {
            for(var i=0; i<msxml_progid.length; ++i){
                try
                {
                    // Instantiates XMLHttpRequest for IE and assign to http
                    xhr = new ActiveXObject(msxml_progid[i]);
                    break;
                }
                catch(e){}
            }
        }
        finally
        {
            return xhr;
        }
    }

    function xhrSyncGetRequest(url, text){
        var request = createXhrObject();
        request.open('GET', url, false);
        request.send(null);

        if (request.status == 200) {  // Make sure there were no errors     // Make sure the response is an XML document
            if(text == true){
                return request.responseText;
            }
            if (request.getResponseHeader("Content-Type").match(/text\/xml/) != null || request.getResponseHeader("Content-Type").match(/application\/xml/) != null) {
                return request.responseXML;
            }else{
                return request.responseText;
            }

        }else {
            aler("Error occurred during the GET request");
            return null;
        }
    }
    var ns = org.apache.ode.XHRObject;
    ns.xhrSyncGetRequest = xhrSyncGetRequest;

})();

if (org.apache.ode.DOMHelper) {
    throw new Error("org.apache.ode.DOMHelper already exists");
}

// Utility methods for handling DOM in cross browser way.
org.apache.ode.DOMHelper = {};

(function(){

    function getElementsByTagName(tagName, ns, prefix, scope){
        var elementListForReturn = scope.getElementsByTagName(prefix+":"+tagName);
        if(elementListForReturn.length == 0){
            elementListForReturn = scope.getElementsByTagName(tagName);
            if(elementListForReturn.length == 0){
                elementListForReturn = scope.getElementsByTagName("ns:"+tagName);
                if(elementListForReturn.length == 0 && document.getElementsByTagNameNS){
                    elementListForReturn = scope.getElementsByTagNameNS(ns, tagName);
                }
            }
        }

        return elementListForReturn;
    }

    // Find all Text nodes at or beneath the node n.
    // Concatenate their content and return it as a string.
    function getText(n){
        var strings = [];
        getStrings(n, strings);
        return strings.join("");

        function getStrings(n, strings){
            if (n.nodeType == 3 /* Node.TEXT_NODE */)
                strings.push(n.data);
            else if (n.nodeType == 1 /* Node.ELEMENT_NODE */) {
                    for (var m = n.firstChild; m != null; m = m.nextSibling) {
                        getStrings(m, strings);
                    }
            }
        }
    }

    var ns = org.apache.ode.DOMHelper;
    ns.getElementsByTagName = getElementsByTagName;
    ns.getText = getText;
})();

if(org.apache.ode.Widgets){
    throw new Error("org.apache.ode.Widgets already exists");
}

org.apache.ode.Widgets = {};

(function(){
    function operationConfirmation(msg, handleYes, handleNo){

        var handleYesWrapper= function(){
            this.hide();
            handleYes();

        }

        var handleNoWrapper = function(){
            this.hide();
            handleNo();

        }

        var simpleDiag = new YAHOO.widget.SimpleDialog('confimationdialogue',{
            width:'350px',
            fixedcenter:true,
            visible:true,
            draggable:true,
            close:false,
            text:msg,
            icon:YAHOO.widget.SimpleDialog.ICON_WARN,
            constraintoviewport:true,
            buttons:[{
                text:'Yes',
                handler:handleYesWrapper,
                isDefault:true
            },{
                text:'No',
                handler:handleNoWrapper
            }]
        });

        simpleDiag.setHeader('Apache ODE');
        simpleDiag.render('content');
        simpleDiag.show();

    }

    function alert(msg, iconT){
        if(iconT == 'undefined')
            iconT = YAHOO.widget.SimpleDialog.ICON_INFO;

        var handleOK = function(){
            this.hide();
        }

        var simpleAlert = new YAHOO.widget.SimpleDialog('alertbox',{
            width:'350px',
            fixedcenter:true,
            visible:true,
            draggable:true,
            close:false,
            text:msg,
            icon:iconT,
            constraintoviewport:true,
            buttons:[{text:'OK',
            handler:handleOK,
            isDefault:true}]
        });

        simpleAlert.setHeader('Apache ODE');
        simpleAlert.render('content');
        simpleAlert.show();
    }

    var ns = org.apache.ode.Widgets;
    ns.operationConfirm = operationConfirmation;
    ns.alert = alert;
})();



// Definition of Process Information processing class start from here.
// This class will be used to get the details of processes from ODE
// process management service and visualize them in Web interface.
// Process retiring, activation is also handle from this class.
if (org.apache.ode.ProcessHandling) {
    throw new Error("org.apache.ode.ProcessHandling already exists");
}

org.apache.ode.ProcessHandling = {};

(function(){
    var processInfoNS = "http://www.apache.org/ode/pmapi/types/2006/08/02/";
    var processInfoNSPrefix = "ns";
    var processInfoTagName = "process-info";

    function loadProcessInfo(){
        // Use ProcessManagementService to get the details about currently available
        // processes in the engine. This method returns a XML document like following:
        // <?xml version='1.0' encoding='UTF-8'?>
        //<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
        //    <soapenv:Body>
        //        <axis2ns8:listAllProcessesResponse xmlns:axis2ns8="http://www.apache.org/ode/pmapi">
        //            <process-info-list>
        //                <ns:process-info xmlns:ns="http://www.apache.org/ode/pmapi/types/2006/08/02/">
        //                    <ns:pid>{http://ode/bpel/unit-test}HelloWorld2-3</ns:pid>
        //                    <ns:status>ACTIVE</ns:status>
        //                    <ns:version>3
        //                    </ns:version>
        //                    <ns:definition-info>
        //                        <ns:process-name xmlns:unit="http://ode/bpel/unit-test">unit:HelloWorld2
        //                        </ns:process-name>
        //                    </ns:definition-info>
        //                    <ns:deployment-info>
        //                        <ns:package>HelloWorld2</ns:package>
        //                        <ns:document>HelloWorld2.bpel
        //                        </ns:document>
        //                        <ns:deploy-date>2008-06-15T17:52:04.523+05:30</ns:deploy-date>
        //                    </ns:deployment-info>
        //                    <ns:instance-summary>
        //                        <ns:instances state="ACTIVE" count="0"/>
        //                        <ns:instances state="COMPLETED" count="0"/>
        //                        <ns:instances state="ERROR" count="0"/>
        //                        <ns:instances state="FAILED" count="0"/>
        //                        <ns:instances state="SUSPENDED" count="0"/>
        //                        <ns:instances state="TERMINATED" count="0"/>
        //                    </ns:instance-summary>
        //                    <ns:properties />
        //                    <ns:endpoints/>
        //                    <ns:documents>
        //                        <ns:document>
        //                            <ns:name>HelloWorld2.bpel</ns:name>
        //                           <ns:type>http://schemas.xmlsoap.org/ws/2004/03/business-process/
        //                            </ns:type>
        //                            <ns:source>file:/home/milinda/programs/apache-tomcat-5.5.26/webapps/ode/WEB-INF/processes/HelloWorld2/HelloWorld2.bpel
        //                            </ns:source>
        //                        </ns:document>
        //                        <ns:document>
        //                            <ns:name>HelloWorld2.wsdl</ns:name>
        //                            <ns:type>http://schemas.xmlsoap.org/wsdl/</ns:type>
        //                            <ns:source>
        //                                file:/home/milinda/programs/apache-tomcat-5.5.26/webapps/ode/WEB-INF/processes/HelloWorld2/HelloWorld2.wsdl
        //                            </ns:source>
        //                        </ns:document>
        //                        <ns:document>
        //                            <ns:name>HelloWorld2.cbp</ns:name>
        //                            <ns:type>http://www.fivesight.com/schemas/2005/12/19/CompiledBPEL
        //                            </ns:type>
        //                            <ns:source>file:/home/milinda/programs/apache-tomcat-5.5.26/webapps/ode/WEB-INF/processes/HelloWorld2/HelloWorld2.cbp
        //                            </ns:source>
        //                        </ns:document>
        //                    </ns:documents>
        //                </ns:process-info>
        //          </process-info-list>
        //      </axis2ns8:listAllProcessesResponse>
        //  </soapenv:Body>
        //</soapenv:Envelope>

        try {
            var listAllProcessesRes = ProcessManagementService.listAllProcesses();
            return listAllProcessesRes;
        }
        catch (e) {
            // probably a connection error. We don't want to spam the user, so we're just logging it if a console is available
            debug("Exception in " + arguments.callee.toString().match(/function\s+([^(]+)/)[1] + ": " + e.toString());
            return null;
        }
    }

    function InstanceSummary(activeIns, completedIns, errorIns, failedIns, suspendedIns, terminatedIns){
        this.activeInstances = activeIns;
        this.completedInstances = completedIns;
        this.errorInstances = errorIns;
        this.failedInstances = failedIns;
        this.suspendedInstances = suspendedIns;
        this.terminatedInstances = terminatedIns;
    }

    function Process(
        pid, version, status, depDate, nameWithVer, processName, urlOfName, prefixOfName, instanceSummary){
        this.pid = pid;
        this.version = version;
        this.status = status;
        this.depDate = depDate;
        this.nameWithVer = nameWithVer;
        this.processName = processName;
        this.urlOfName = urlOfName;
        this.prefixOfName = prefixOfName;
        this.instanceSummary = instanceSummary;
    }

    function processProcessInfoList(listAllProcessesRes){
        if (listAllProcessesRes == null) {
          return 0;
        }

        var returnInfoArray = [];
        var processInfoList = org.apache.ode.DOMHelper.getElementsByTagName(
            processInfoTagName,
            processInfoNS,
            processInfoNSPrefix,
            listAllProcessesRes);
        if(processInfoList.length == 0){
            return 0;
        }else{

            for(var i = 0; i < processInfoList.length; i++){
                var activeInstances = 0;
                var completedInstances = 0;
                var errorInstances = 0;
                var failedInstances = 0;
                var suspendedInstances = 0;
                var terminatedInstances = 0;

                var scopeEle = processInfoList[i];

                var pidEle = org.apache.ode.DOMHelper.getElementsByTagName("pid", processInfoNS, processInfoNSPrefix, scopeEle)[0];
                var pid = org.apache.ode.DOMHelper.getText(pidEle);

                var versionEle = org.apache.ode.DOMHelper.getElementsByTagName('version', processInfoNS, processInfoNSPrefix, scopeEle)[0];
                var version = org.apache.ode.DOMHelper.getText(versionEle);

                var statusEle = org.apache.ode.DOMHelper.getElementsByTagName('status', processInfoNS, processInfoNSPrefix, scopeEle)[0];
                var status = org.apache.ode.DOMHelper.getText(statusEle);

                var depInfoEle = org.apache.ode.DOMHelper.getElementsByTagName("deployment-info", processInfoNS, processInfoNSPrefix, scopeEle)[0];
                var depDateEle = org.apache.ode.DOMHelper.getElementsByTagName("deploy-date", processInfoNS, processInfoNSPrefix, scopeEle)[0];
                var depDate = org.apache.ode.DOMHelper.getText(depDateEle);

                var defInfoEle = org.apache.ode.DOMHelper.getElementsByTagName("definition-info", processInfoNS, processInfoNSPrefix, scopeEle)[0];
                var processNameEle = org.apache.ode.DOMHelper.getElementsByTagName("process-name", processInfoNS, processInfoNSPrefix, scopeEle)[0];
                var processName = org.apache.ode.DOMHelper.getText(processNameEle);

                var len = pid.length;
                var endPos = pid.indexOf('}');
                var startPos = pid.indexOf('{');
                var nameWithVersion = pid.substr(++endPos, len);
                var indexOfColon = processName.indexOf(':');
                var prefixOfName = processName.substring(0, indexOfColon);
                var urlOfName = pid.substr(++startPos, (endPos - 2));

                var instanceSummaryEle = org.apache.ode.DOMHelper.getElementsByTagName("instance-summary", processInfoNS, processInfoNSPrefix, scopeEle)[0];

                for(var m = instanceSummaryEle.firstChild; m != null; m = m.nextSibling){
                    var state = m.getAttribute("state");
                    var count = m.getAttribute("count");

                    if (state == 'ACTIVE') {
                        activeInstances = parseInt(count);
                    }else if (state == 'COMPLETED') {
                        completedInstances = parseInt(count);
                    }else if (state == 'ERROR') {
                        errorInstances = parseInt(count);
                    }else if (state == 'FAILED') {
                        failedInstances = parseInt(count);
                    }else if (state == 'SUSPENDED') {
                        suspendedInstances = parseInt(count);
                    }else if (state == 'TERMINATED') {
                        terminatedInstances = parseInt(count);
                    }
                }

                var instanceSummary = new InstanceSummary(
                                        activeInstances,
                                        completedInstances,
                                        errorInstances,
                                        failedInstances,
                                        suspendedInstances,
                                        terminatedInstances);

                var processInfo = new Process(pid,
                                        version,
                                        status,
                                        depDate,
                                        nameWithVersion,
                                        processName,
                                        urlOfName,
                                        prefixOfName,
                                        instanceSummary);

                returnInfoArray[i] = processInfo;
            }
        }

        return returnInfoArray;
    }

    function createProcessWidget(process, i){
        var retireBtnID = 'retire' + i;
        var retierBtnVar = 'retireBtn' + i;
        var activateBtnID = 'activate'+i;
        var activateVar = 'activateBtn' + i;
        var viewProDetID = 'viewProcessDet'+i;
        var viewProDetVar = 'viewProcessDetVar'+i;
        var active = "true";
        var retire = "false";
        if(process.status.toUpperCase() == 'ACTIVE'){
            active = "true";
            retire = "false";
        }else{
            active = "false";
            retire = "true";
        }
        str = '<div class="yui-cms-item yui-panel selected"><div class="hd">'+
              process.pid +
              '</div><div class="bd"><div class="fixed">'+
              '<table><tr><td class="alt"> Process Summary</td></tr><tr><th>' +
              'Deploy Date:</th><td>' +
              process.depDate +
              '</td><th>Status:</th><td>' +
              process.status +
              '</td><th>Version:</th><td>' +
              process.version +
              '</td></tr>' +
              '<table><tr><td class="alt"> Instance Summary</td></tr><tr><th>Active:</th><td>' +
              process.instanceSummary.activeInstances +
              '</td><th>Terminated:</th><td>' +
              process.instanceSummary.terminatedInstances +
              '</td><th>Completed:</th><td>' +
              process.instanceSummary.completedInstances +
              '</td><th>Error:</th><td>' +
              process.instanceSummary.errorInstances +
              '</td><th>Failed:</th><td>' +
              process.instanceSummary.failedInstances +
              '</td><th>Suspended:</th><td>' +
              process.instanceSummary.suspendedInstances +
              '</td> </tr></table></table>' +
              '</div></div><div class="ft">'+
              '<span id="'+ retireBtnID +
              '" class="yui-button yui-push-button"><span class="first-child"><input type="button" name="'+ retireBtnID +'name" value="Retire"></span></span>'+
              '<span id="'+ activateBtnID +
              '" class="yui-button yui-push-button"><span class="first-child"><input type="button" name="'+ activateVar +'name" value="Activate"></span></span>'+
              '<span id="'+ viewProDetID +
              '" class="yui-button yui-push-button"><span class="first-child"><input type="button" name="'+ viewProDetVar +'name" value="Details"></span></span>'+
              '<script type="text/javascript">'+
              'function '+retierBtnVar+'retireProcess(){org.apache.ode.ProcessHandling.retireProcess("'+ process.nameWithVer +'","'+ process.urlOfName +'","'+process.prefixOfName+'");}'+
              'function '+activateVar+'activateProcess(){org.apache.ode.ProcessHandling.activateProcess("'+ process.nameWithVer +'","'+ process.urlOfName +'","'+process.prefixOfName+'");}'+
              'function '+viewProDetVar+'viewProcessDetails(){org.apache.ode.ProcessHandling.viewProcessDetails("'+ process.nameWithVer +'","'+ process.urlOfName +'","'+process.prefixOfName+'");}'+

              'var ' + retierBtnVar + '=new YAHOO.widget.Button("'+ retireBtnID +'");'+
              retierBtnVar + '.addListener("click", '+retierBtnVar+'retireProcess); '+
              retierBtnVar+'.set("disabled",'+retire+');'+
              'var ' + activateVar + '=new YAHOO.widget.Button("'+ activateBtnID + '");'    +
              activateVar+'.addListener("click", '+activateVar+'activateProcess); ' +
              activateVar+'.set("disabled",'+active+');'+
              'var ' + viewProDetVar + '=new YAHOO.widget.Button("'+ viewProDetID + '");'   +
              viewProDetVar+'.addListener("click", '+viewProDetVar+'viewProcessDetails); '  +
              '</script>'+
              '</div> <div class="actions"><a href="#" class="accordionToggleItem">&nbsp;</a>'+
              '</div><div class="actions"><a href="#" class="accordionToggleItem">&nbsp;</a>'+
              '</div></div>'

        return str;
    }

    function populateContentArea(){

        var contentHTML = '<h2>Currently Deployed Processes</h2>';
        var processesInfo = loadProcessInfo();

        var processArray = processProcessInfoList(processesInfo);
        if (processArray != 0 ) {
            for (var i = 0; i < processArray.length; i++) {
                contentHTML += createProcessWidget(processArray[i], i);
            }
        }else{
            contentHTML += '<p>Currently no processes are available.</p>'
        }
        var content = document.getElementById('content');
        var newDiv = document.createElement('div');
        YAHOO.util.Dom.addClass(newDiv, 'myAccordion');
        var innerDiv = document.createElement('div');
        YAHOO.util.Dom.addClass(innerDiv, 'yui-cms-accordion multiple fade fixIE');
        innerDiv.innerHTML = contentHTML;
        newDiv.appendChild(innerDiv);
        if(content.firstChild){
            content.replaceChild(newDiv, content.firstChild);
        }else{
            content.appendChild(newDiv);
        }

    }

    function getStatistics(){
        function Statistics(numOfProces, active, terminated, error, failed, suspended, completed){
            this.numOfProcesses = numOfProces;
            this.activeInst = active;
            this.terminatedInst = terminated;
            this.errorInst = error;
            this.failedInst = failed;
            this.suspendedInst = suspended;
            this.completedInst = completed;
            this.totalInst = active + terminated + error + failed + suspended + completed;
        }
        var stat;
        var processes = processProcessInfoList(loadProcessInfo());
        var numOfProcesses = (processes == 0) ? 0 : processes.length;
        var _ter = 0;
        var _act = 0;
        var _error = 0;
        var _fail = 0;
        var _susp = 0;
        var _com = 0;
        if(numOfProcesses != 0){
            for(var i = 0; i < processes.length; i++){
                _act += processes[i].instanceSummary.activeInstances;
                _ter += processes[i].instanceSummary.terminatedInstances;
                _error += processes[i].instanceSummary.errorInstances;
                _fail += processes[i].instanceSummary.failedInstances;
                _susp += processes[i].instanceSummary.suspendedInstances;
                _com += processes[i].instanceSummary.completedInstances;
            }
            stat = new Statistics(numOfProcesses, _act, _ter, _error, _fail, _susp, _com);
        }else{
            stat = new Statistics(0,0,0,0,0,0,0);
        }
        return stat;
    }

    function _populateContentArea(){
        setTimeout("populateContentArea()", 5000);
    }

    function retireProcess(processName, url, prefix){
        try {

            function handleYes(){
                var response;
                try{
                    response = ProcessManagementService.setRetired(true, processName, url, prefix);
                }catch(e){
                    if (typeof(e) == "string") {
                        org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString(), YAHOO.widget.SimpleDialog.ICON_ALARM);
                    }
                    else {
                        org.apache.ode.Widgets.alert("Exception occurred!", YAHOO.widget.SimpleDialog.ICON_ALARM);
                    }
                }
                var prefixWithName = prefix + ':' + processName;
                var defInfoEle = org.apache.ode.DOMHelper.getElementsByTagName('definition-info', 'http://www.apache.org/ode/pmapi/types/2006/08/02/', 'ns', response)[0];

                if (defInfoEle) {
                    var processNameEle = org.apache.ode.DOMHelper.getElementsByTagName('process-name', 'http://www.apache.org/ode/pmapi/types/2006/08/02/', 'ns', defInfoEle)[0];
                    if (processNameEle) {
                        var proNameFromRes = org.apache.ode.DOMHelper.getText(processNameEle);
                        if (prefixWithName == proNameFromRes) {
                            org.apache.ode.ProcessHandling.populateContent();
                        }
                        else {
                            org.apache.ode.Widgets.alert('Error occurred during retiring the process!', YAHOO.widget.SimpleDialog.ICON_ALARM);
                        }
                    }
                }else{
                    org.apache.ode.Widgets.alert('Error occurred during retiring the process!', YAHOO.widget.SimpleDialog.ICON_ALARM);
                }
                return true;

            }

            function handleNo(){
                org.apache.ode.Widgets.alert('Retiring cancelled!', YAHOO.widget.SimpleDialog.ICON_INFO);
            }
            var msg = 'Do you want to retire the process '+ processName + '?';
            org.apache.ode.Widgets.operationConfirm(msg, handleYes, handleNo);
        }
        catch (e) {
            if (typeof(e) == "string") {
                org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString());
            }
            else {
                org.apache.ode.Widgets.alert("Exception occurred!");
            }

        }
        return false;
    }

    function activateProcess(processName, url, prefix){
        try {
            function handleYes(){
                var response;
                try{
                    response = ProcessManagementService.activate(processName, url, prefix);
                }catch(e){
                    if (typeof(e) == "string") {
                        org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString());
                    }
                    else {
                        org.apache.ode.Widgets.alert("Exception occurred!");
                    }
                }
                var prefixWithName = prefix + ':' + processName;
                var defInfoEle = org.apache.ode.DOMHelper.getElementsByTagName('definition-info', 'http://www.apache.org/ode/pmapi/types/2006/08/02/', 'ns', response)[0];
                if (defInfoEle) {
                    var processNameEle = org.apache.ode.DOMHelper.getElementsByTagName('process-name', 'http://www.apache.org/ode/pmapi/types/2006/08/02/', 'ns', defInfoEle)[0];

                    if(processNameEle){
                        var proNameFromRes = org.apache.ode.DOMHelper.getText(processNameEle);
                        if (prefixWithName == proNameFromRes) {
                            org.apache.ode.ProcessHandling.populateContent();
                        }else{
                            org.apache.ode.Widgets.alert('Error occurred while activating process!');
                        }
                    }
                }else{
                    org.apache.ode.Widgets.alert('Error occurred while activating process!');
                }
            }

            function handleNo(){
                org.apache.ode.Widgets.alert('Process Activation Cancelled!');
            }

            var msg = 'Do you want to activate the process '+ processName+'?';
            org.apache.ode.Widgets.operationConfirm(msg, handleYes, handleNo);

        }catch (e) {
            if (typeof(e) == "string") {
                org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString());
            }
            else {
                org.apache.ode.Widgets.alert("Exception occurred!");
            }
        }
        return false;
    }

    function viewProcessDetails(processName, url, prefix){
        var proPID = 'prodet'+processName;
        var processDefURL = '';
        var processN = processName.substring(0, (processName.indexOf('-')));

        //var urlRequestURL = 'http://localhost:8080/ode/deployment/getProcessDefinition/' + processN ;
        var urlRequestURL = baseDirectoryURL + '/deployment/getProcessDefinition/' + processN ;

        try{
            var response = ProcessManagementService.getProcessInfo(processName, url, prefix);
            var processInfoEle = org.apache.ode.DOMHelper.getElementsByTagName(
            processInfoTagName,
            processInfoNS,
            processInfoNSPrefix,
            response)[0];



            var handleSuccess = function(o){
                if(o.responseXML){
                    var proDefURLEle = o.responseXML.getElementsByTagName('url')[0];
                    var proDefURL = org.apache.ode.DOMHelper.getText(proDefURLEle);

                    var handleSuccessIn = function(o){
                        var myPanel = new YAHOO.widget.Panel(proPID, {
                                width:"600px",
                                fixedcenter: true,
                                underlay:"shadow",
                                close:true,
                                visible:true,
                                draggable: true,
                                zindex:4,
                                modal:true} );

                        myPanel.setHeader("Process Details: " + processName);

                        var processInfoStr = ProcessManagementService.text.replace(/>/g, '>\n');
                        processInfoStr = processInfoStr.replace(/<\//g, '\n</');
                        processInfoStr = processInfoStr.replace(/>\n>\n<\//g, '>\n</');

                        var processDefStr = o.responseText;
                        processDefStr = processDefStr.replace(/<\//g, '\n</');
                        processDefStr = processDefStr.replace(/>\n>\n<\//g, '>\n</');

                        var detailsTabs = new YAHOO.widget.TabView();

                        var proInfoTab = new YAHOO.widget.Tab( {
                            label: 'Process Info',
                            content:'<textarea id="proinfo" class="codepress html" style="width: 568px; height: 570px;"></textarea>'

                        });
                        detailsTabs.addTab(proInfoTab);

                        var proDefTab = new YAHOO.widget.Tab({
                            label: 'Process Definition',
                            content:'<textarea id="prodef" class="codepress html" style="width: 568px; height: 570px;"></textarea>',
                            active: true
                        });

                        detailsTabs.addTab(proDefTab);


                        var tabDiv = document.createElement('div');
                        detailsTabs.appendTo(tabDiv);


                        myPanel.setBody(tabDiv);
                        myPanel.cfg.setProperty("underlay","matte");
                        myPanel.render("content");
                        var proInfoEle = document.getElementById('proinfo');
                        proInfoEle.appendChild(document.createTextNode(processInfoStr));
                        var proDefEle = document.getElementById('prodef');
                        proDefEle.appendChild(document.createTextNode(processDefStr));
                    }

                    var handleFailureIn = function(o){
                        org.apache.ode.Widgets.alert("Request Failed: Getting Process Definition.");
                    }

                    var callbackIn = {
                        success: handleSuccessIn,
                        failure: handleFailureIn
                    }

                    var requestIn = YAHOO.util.Connect.asyncRequest('GET', proDefURL, callbackIn);
                }
            }

            var handleFailure = function(o){
                org.apache.ode.Widgets.alert("Request failed: Geting process definition URL");
            }

            var callback = {
                success: handleSuccess,
                failure: handleFailure
            }

            var request = YAHOO.util.Connect.asyncRequest('GET', urlRequestURL, callback);

        }catch(e){
            if (typeof(e) == "string") {
                org.apache.ode.Widgets.alert("Exception occured in viewProcessDetails :\n" + e.toString());
            }
            else {
                org.apache.ode.Widgets.alert("Exception occurred in viewProcessDetails !");
            }
        }
        return false;
    }

    var ns = org.apache.ode.ProcessHandling;
    ns.loadProcessInfo = loadProcessInfo;
    ns.processInfo = processProcessInfoList;
    ns.activateProcess = activateProcess;
    ns.retireProcess = retireProcess;
    ns.populateContent = populateContentArea;
    ns.viewProcessDetails = viewProcessDetails;
    ns.stats = getStatistics;

})();

if (org.apache.ode.InstanceHandling) {
    throw new Error("org.apache.ode.InstanceHandling already exists");
}

org.apache.ode.InstanceHandling = {};

(function(){
    var instanceInfoNS = "http://www.apache.org/ode/pmapi/types/2006/08/02/";
    var instanceInfoNSPrefix = "ns";
    var instanceInfoTagName = "instance-info";

    function loadInstanceInfo(){
        try {
            var responseDoc = InstanceManagementService.listAllInstances();
            return responseDoc;
        }
        catch (e) {
            // probably a connection error. We don't want to spam the user, so we're just logging it if a console is available
            debug("Exception in " + arguments.callee.toString().match(/function\s+([^(]+)/)[1] + ": " + e.toString());
            return null;
        }
    }

    function InstanceInfo(iid, pid, rootScope, siid, statusR, nameR, modelID, statusI, dateStarted, dateLastActive){
        this.iid = iid;
        this.pid = pid;
        this.rootScope = rootScope;
        this.siid = siid;
        this.statusR = statusR;
        this.nameR = nameR;
        this.modelID = modelID;
        this.statuI = statusI;
        this.dateStarted = dateStarted;
        this.dateLastActive = dateLastActive;
    }

    function processInstanceInfo(instanceInfoDoc){
        if (instanceInfoDoc == null) {
          return 0;
        }
        var returnInstanceArray = [];
        var instanceInfoList = org.apache.ode.DOMHelper.getElementsByTagName(
            instanceInfoTagName,
            instanceInfoNS,
            instanceInfoNSPrefix,
            instanceInfoDoc);
        if (instanceInfoList.length == 0){
            return 0;
        }else{
            for(var i = 0; i < instanceInfoList.length; i++){
                var scopeEle = instanceInfoList[i];
                var iidEle = org.apache.ode.DOMHelper.getElementsByTagName('iid', instanceInfoNS, instanceInfoNSPrefix, scopeEle)[0];
                var iid = org.apache.ode.DOMHelper.getText(iidEle);

                var pidEle = org.apache.ode.DOMHelper.getElementsByTagName('pid', instanceInfoNS, instanceInfoNSPrefix, scopeEle)[0];
                var pid = org.apache.ode.DOMHelper.getText(pidEle);

                var rootScopeEle = null;          //this element is "minOccurs=0"
                var siid = 'not defined';
                var statusR = 'not defined';
                var nameR = 'not defined';
                var modelID = 'not defined';
                rootScopeEle = org.apache.ode.DOMHelper.getElementsByTagName('root-scope', instanceInfoNS, instanceInfoNSPrefix, scopeEle)[0];
                if(rootScopeEle != null){
                    siid = rootScopeEle.getAttribute('siid');
                    statusR = rootScopeEle.getAttribute('status');
                    nameR = rootScopeEle.getAttribute('name');
                    modelID = rootScopeEle.getAttribute('modelId');
                }

                var statusIEle = org.apache.ode.DOMHelper.getElementsByTagName('status', instanceInfoNS, instanceInfoNSPrefix, scopeEle)[0];
                var statusI = org.apache.ode.DOMHelper.getText(statusIEle);

                var dateSEle = org.apache.ode.DOMHelper.getElementsByTagName('dt-started', instanceInfoNS, instanceInfoNSPrefix, scopeEle)[0];
                var dateStarted = org.apache.ode.DOMHelper.getText(dateSEle);

                var lastAEle = org.apache.ode.DOMHelper.getElementsByTagName('dt-last-active', instanceInfoNS, instanceInfoNSPrefix, scopeEle)[0];
                var lastActive = org.apache.ode.DOMHelper.getText(lastAEle);

                var instance = new InstanceInfo(iid, pid, rootScopeEle, siid, statusR, nameR, modelID, statusI, dateStarted, lastActive);

                returnInstanceArray[i] = instance;
            }
        }

        return returnInstanceArray;
    }

    function createInstanceWidget(instance, i){
        var terminateBtnID = 'terminateIns'+i;
        var terminateBtnVar = 'terminateVar'+i;
        var suspendBtnID = 'suspendBtn' + i;
        var suspenBtnVar = 'suspendBtnVar' + i;
        var resumeBtnID = 'resumeBtn' + i;
        var resumeBtnVar = 'resumeBtnVar' + i;
        var _term = "false";
        var _susp = "false";
        var _resu = "true";
        if(instance.statuI.toUpperCase() == 'ACTIVE'){
            _term = "false";
            _susp = "false";
            _resu = "true";
        }else if(instance.statuI.toUpperCase() == 'COMPLETED' || instance.statuI.toUpperCase() == 'ERROR'
                || instance.statuI.toUpperCase() == 'FAILED'){
            _term = "false";
            _susp = "true";
            _resu = "true";
        }else if(instance.statuI.toUpperCase() == 'SUSPENDED'){
            _term = "false";
            _susp = "true";
            _resu = "false";
        }else if(instance.statuI.toUpperCase() == 'TERMINATED'){
            _term = "true";
            _susp = "true";
            _resu = "true";
        }

        str = '<div class="yui-cms-item yui-panel selected"><div class="hd">Instance ID: '+
              instance.iid +
              '</div><div class="bd"><div class="fixed">'+
              '<table><tr><td class="alt">Instance Summary</td></tr><tr><th>' +
              'Process:</th><td>' + instance.pid + '</td><th>Status:</th><td>' +
              instance.statuI + '</td></tr><tr><th>Date Started:</th><td>' +
              instance.dateStarted + '</td><th>Date Last Active</th><td>' +
              instance.dateLastActive + '</td></tr></table>' +
              '</div></div><div class="ft">'+
              '<span id="'+ terminateBtnID +
              '" class="yui-button yui-push-button"><span class="first-child"><input type="button" name="'+ terminateBtnID +'name" value="Terminate"></span></span>'+
              '<span id="'+ suspendBtnID +
              '" class="yui-button yui-push-button"><span class="first-child"><input type="button" name="'+ suspendBtnID +'name" value="Suspend"></span></span>'+
              '<span id="'+ resumeBtnID +
              '" class="yui-button yui-push-button"><span class="first-child"><input type="button" name="'+ resumeBtnID +'name" value="Resume"></span></span>'+
              '<script type="text/javascript">'+
              'function '+terminateBtnVar+'terminateIns(){org.apache.ode.InstanceHandling.terminateInstance("'+instance.iid +'");}'+
              'function '+suspenBtnVar+'suspendIns(){org.apache.ode.InstanceHandling.suspendInstance("'+instance.iid +'");}'+
              'function '+resumeBtnVar+'resumeIns(){org.apache.ode.InstanceHandling.resumeInstance("'+instance.iid +'");}'+

              'var ' + terminateBtnVar + '=new YAHOO.widget.Button("'+ terminateBtnID + '");'+
              terminateBtnVar + '.addListener("click", '+terminateBtnVar+'terminateIns); '+
              terminateBtnVar+ '.set("disabled",' + _term + ');'+
              'var ' + suspenBtnVar + '=new YAHOO.widget.Button("'+ suspendBtnID + '");'    +
              suspenBtnVar+'.addListener("click", '+suspenBtnVar+'suspendIns); '    +
             suspenBtnVar+ '.set("disabled",' + _susp + ');' +
              'var ' + resumeBtnVar + '=new YAHOO.widget.Button("'+ resumeBtnID + '");' +
              resumeBtnVar+'.addListener("click", '+resumeBtnVar+'resumeIns); ' +
              resumeBtnVar+ '.set("disabled",' + _resu + ');' +
              '</script>'+
              '</div> <div class="actions"><a href="#" class="accordionToggleItem">&nbsp;</a>'+
              '</div></div>'

        return str;
    }

    function populateContentArea(){

        var contentHTML = '<h2>Currently Available Instances</h2>';
        var instanceInfo = loadInstanceInfo();

        var instanceArray = processInstanceInfo(instanceInfo);
        if (instanceArray != 0 ) {
            for (var i = 0; i < instanceArray.length; i++) {
                contentHTML += createInstanceWidget(instanceArray[i], i);
            }
        }else{
            contentHTML += '<p>Currently no instances are available.</p>'
        }
        var content = document.getElementById('content');
        var newDiv = document.createElement('div');
        YAHOO.util.Dom.addClass(newDiv, 'myAccordion');
        var innerDiv = document.createElement('div');
        YAHOO.util.Dom.addClass(innerDiv, 'yui-cms-accordion multiple fade fixIE');
        innerDiv.innerHTML = contentHTML;
        newDiv.appendChild(innerDiv);
        if(content.firstChild){
            content.replaceChild(newDiv, content.firstChild);
        }else{
            content.appendChild(newDiv);
        }

    }

    function terminateInstance(instanceID){
        try {

            function handleYes(){
                var response;
                try{
                    response = InstanceManagementService.terminate(instanceID);
                } catch (e) {
                    if (typeof(e) == "string") {
                        org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString());
                    }
                    else {
                        org.apache.ode.Widgets.alert("Exception occurred!");
                    }
                }
                var insEle = org.apache.ode.DOMHelper.getElementsByTagName('instance-info', 'http://www.apache.org/ode/pmapi/types/2006/08/02/', 'ns', response)[0];
                if(insEle){
                    var iidEle = org.apache.ode.DOMHelper.getElementsByTagName('iid', 'http://www.apache.org/ode/pmapi/types/2006/08/02/', 'ns', insEle)[0];
                    if(iidEle){
                        var iidFromRes = org.apache.ode.DOMHelper.getText(iidEle);
                        if (iidFromRes == instanceID) {
                            org.apache.ode.InstanceHandling.populateContent();
                        }else{
                            org.apache.ode.Widgets.alert('Error occurred during termination of instance!');
                        }
                    }else{
                        org.apache.ode.Widgets.alert('Error occurred during termination of instance!');
                    }
                }else{
                    org.apache.ode.Widgets.alert('Error occurred during instance termination!');
                }

            }
            function handleNo(){
                org.apache.ode.Widgets.alert('Instance terminating cancelled!');
            }

            var msg = 'Do you want to terminate instance '+ instanceID + '?';
            org.apache.ode.Widgets.operationConfirm(msg, handleYes, handleNo);
        } catch (e) {
            if (typeof(e) == "string") {
                org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString());
            }
            else {
                org.apache.ode.Widgets.alert("Exception occurred!");
            }
        }
        return false;
    }

    function suspendInstance(instanceID){
        try {
            function handleYes(){
                var response;
                try{
                    response = InstanceManagementService.suspend(instanceID);
                } catch (e) {
                    if (typeof(e) == "string") {
                        org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString());
                    }
                    else {
                        org.apache.ode.Widgets.alert("Exception occurred!");
                    }
                }
                var insEle = org.apache.ode.DOMHelper.getElementsByTagName('instance-info', 'http://www.apache.org/ode/pmapi/types/2006/08/02/', 'ns', response)[0];
                if(insEle){
                    var iidEle = org.apache.ode.DOMHelper.getElementsByTagName('iid', 'http://www.apache.org/ode/pmapi/types/2006/08/02/', 'ns', insEle)[0];
                    if(iidEle){
                        var iidFromRes = org.apache.ode.DOMHelper.getText(iidEle);
                        if (iidFromRes == instanceID) {
                            org.apache.ode.InstanceHandling.populateContent();
                        }else{
                            org.apache.ode.Widgets.alert('Error occurred during suspending of instance!');
                        }
                    }else{
                        org.apache.ode.Widgets.alert('Error occurred during suspending of instance!');
                    }
                }else{
                    org.apache.ode.Widgets.alert('Error occurred during instance suspending!');
                }
            }

            function handleNo(){
                org.apache.ode.Widgets.alert('Instance suspending cancelled!');
            }
            var msg = 'Do you want to suspend instance ' + instanceID + '?';
            org.apache.ode.Widgets.operationConfirm(msg, handleYes, handleNo);
        } catch (e) {
            if (typeof(e) == "string") {
                org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString());
            }
            else {
                org.apache.ode.Widgets.alert("Exception occurred!");
            }
        }

    }

    function resumeInstance(instanceID){
        try {
            function handleYes(){
                var response;
                try{
                    response = InstanceManagementService.resume(instanceID);
                } catch (e) {
                    if (typeof(e) == "string") {
                        org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString());
                    }
                    else {
                        org.apache.ode.Widgets.alert("Exception occurred!");
                    }
                }

                var insEle = org.apache.ode.DOMHelper.getElementsByTagName('instance-info', 'http://www.apache.org/ode/pmapi/types/2006/08/02/', 'ns', response)[0];
                 if(insEle){
                    var iidEle = org.apache.ode.DOMHelper.getElementsByTagName('iid', 'http://www.apache.org/ode/pmapi/types/2006/08/02/', 'ns', insEle)[0];
                    if(iidEle){
                        var iidFromRes = org.apache.ode.DOMHelper.getText(iidEle);
                        if (iidFromRes == instanceID) {
                            org.apache.ode.InstanceHandling.populateContent();
                        }else{
                            org.apache.ode.Widgets.alert('Error occurred during resuming of instance!');
                        }
                    }else{
                        org.apache.ode.Widgets.alert('Error occurred during resuming of instance!');
                    }
                }else{
                    org.apache.ode.Widgets.alert('Error occurred during instance resuming!');
                }

            }

            function handleNo(){
                org.apache.ode.Widgets.alert('Instance resuming cancelled!');
            }

            var msg = 'Do you want to resume instance'+ instanceID + '?';
            org.apache.ode.Widgets.operationConfirm(msg, handleYes, handleNo);
        } catch (e) {
            if (typeof(e) == "string") {
                org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString());
            }
            else {
                org.apache.ode.Widgets.alert("Exception occurred!");
            }
        }
        return false;
    }

    var ns = org.apache.ode.InstanceHandling;
    ns.terminateInstance = terminateInstance;
    ns.suspendInstance = suspendInstance;
    ns.resumeInstance = resumeInstance;
    ns.populateContent = populateContentArea;
})();

if(org.apache.ode.DeploymentHandling){
    throw new Error("org.apache.ode.DeploymentHandling already exists");
}

org.apache.ode.DeploymentHandling = {};

(function(){
    //var bundleDataUrl = 'http://localhost:8080/ode/deployment/bundles/';
    //var packageDocsUrl = 'http://localhost:8080/ode/deployment/getBundleDocs/';
    var bundleDataUrl = baseDirectoryURL + '/deployment/bundles/';
    var packageDocsUrl = baseDirectoryURL + '/deployment/getBundleDocs/';

    function loadDeployedPackages(){
        try{
            var response = DeploymentService.listDeployedPackages();
            return response;
        }catch(e){
            // probably a connection error. We don't want to spam the user, so we're just logging it if a console is available
            debug("Exception in " + arguments.callee.toString().match(/function\s+([^(]+)/)[1] + ": " + e.toString());
            return null;
        }
    }

    function getDeployedPackages(){
        var packageNames = [];
        var response = loadDeployedPackages();
        if (response == null) {
          return 0;
        }
        var names = org.apache.ode.DOMHelper.getElementsByTagName('name',"http://www.apache.org/ode/deployapi","deployapi",response);
        //var names = response.getElementsByTagName('name');
        if (names.length != 0) {
            for (var i = 0; i < names.length; i++) {
                packageNames[i] = org.apache.ode.DOMHelper.getText(names[i]);
            }
            return packageNames;
        }else{
            return 0;
        }

    }

    function getProcesses(packageName){
        try{
            var processes = [];
            var response = DeploymentService.listProcesses(packageName);
            var ids = org.apache.ode.DOMHelper.getElementsByTagName('id',"http://www.apache.org/ode/deployapi","deployapi",response);
            if(ids.length != 0){
                for(var i =0; i < ids.length; i++){
                    processes[i] = org.apache.ode.DOMHelper.getText(ids[i]);
                }
                return processes;
            }else{
                return 0;
            }

        }catch(e){
            // probably a connection error. We don't want to spam the user, so we're just logging it if a console is available
            debug("Exception in " + arguments.callee.toString().match(/function\s+([^(]+)/)[1] + ": " + e.toString());
            return 0;
        }
    }
    function getPackageContents(packageName){
        var contents = [];
        var i = 0;
        var url = packageDocsUrl + packageName;
        try{
        var response = org.apache.ode.XHRObject.xhrSyncGetRequest(url, false);
        }catch(e){
            org.apache.ode.Widgets.alert("Exception occurred during getting paackage contents.");
            return null;
        }
        if(response != null && typeof response != 'string'){
            var processEle = response.getElementsByTagName('process')[0];
            for(var m = processEle.firstChild; m != null; m = m.nextSibling){
                if(m.localName != 'pid'){
                    contents[i] = org.apache.ode.DOMHelper.getText(m);
                    i++;
                }

            }
            return contents;
        }else{
            return null;
        }
    }

    function populateDeployedPackages(){
        var contentHtml = '';
        var deployedPacks = getDeployedPackages();
        for(var i = 0; i < deployedPacks.length; i++){
            var packageundepId = deployedPacks[i].replace(/-/, "_")+"undeployid";
            var packageundepVar = deployedPacks[i].replace(/-/,"_")+"undeployvar";
            var packageDetailsId = deployedPacks[i].replace(/-/,"_")+"detid";
            var packageDetailsVar = deployedPacks[i].replace(/-/,"_")+"detvar";
            contentHtml += '<div class="yui-cms-item yui-panel selected"><div class="hd">'+
                        deployedPacks[i] +
                        '</div><div class="bd"><div class="fixed">'+
                        '<table><tr class="alt"><td>Processes:</td></tr><tr><td>'
            var processes = getProcesses(deployedPacks[i]);
            if(processes != 0){
                for(var j = 0; j < processes.length; j++){
                    contentHtml += processes[j] + (j+1 < processes.length ? ', ' : '');
                }
            }else{
                contentHtml += 'Error occurred during getting processes or no processes.';
            }
            contentHtml += '</td></tr><tr class="alt"><td>Contents:</td></tr><tr><td>';
            var content  = getPackageContents(deployedPacks[i]);
            if(content != null){
                for(var k =0; k < content.length; k++){
                    var strC = content[k];
                    var index = strC.indexOf('/');
                    contentHtml += strC.substr(index+1) + (k+1 < content.length ? ", " : "");
                }
            }else{
                contentHtml += 'Error occurred during getting package Content or no content.'
            }
            contentHtml += '</td></tr></table></div></div><div class="ft">'+
                        '<span id="'+ packageundepId + '" class="yui-button yui-push-button">'+
                        '<span class="first-child"><input type="button" name="'+ packageundepVar +
                        'name" value="Undeploy"></span></span>'+
                        '<span id="'+ packageDetailsId +
                        '" class="yui-button yui-push-button"><span class="first-child">'+
                        '<input type="button" name="'+ packageDetailsVar +'name" value="Details"></span></span>'+
                        '<script type="text/javascript">'+
                        'function '+ packageundepVar +
                        'undeployPackage(){org.apache.ode.DeploymentHandling.undeployPackage("'+
                        deployedPacks[i] +'");}' +
                        'function '+ packageDetailsVar +
                        'viewDetails(){org.apache.ode.DeploymentHandling.viewPackDetails("'+
                        deployedPacks[i] +'"'+ ');}' +
                        'var ' + packageundepVar + '=new YAHOO.widget.Button("'+ packageundepId + '");'+
                        packageundepVar + '.addListener("click", ' + packageundepVar + 'undeployPackage); '+
                        'var ' + packageDetailsVar + '=new YAHOO.widget.Button("'+ packageDetailsId + '");' +
                        packageDetailsVar +'.addListener("click", ' + packageDetailsVar + 'viewDetails); '  +
                        '</script>'+
                        '</div> <div class="actions"><a href="#" class="accordionToggleItem">&nbsp;</a>'+
                        '</div></div>'
            //alert(contentHtml);

        }
        var deployed = document.getElementById('deployed');
        var newDiv = document.createElement('div');
        YAHOO.util.Dom.addClass(newDiv, 'myAccordion');
        var innerDiv = document.createElement('div');
        YAHOO.util.Dom.addClass(innerDiv, 'yui-cms-accordion multiple fade fixIE');
        innerDiv.innerHTML = contentHtml;
        newDiv.appendChild(innerDiv);
        if(deployed.firstChild){
            deployed.replaceChild(newDiv, deployed.firstChild);
        }else{
            deployed.appendChild(newDiv);
        }


    }

    function viewPackDetails(packageName){
        var winID = 'deployedPack'+packageName;
        var bundleDocs = [];

        bundleDocs = getPackageContents(packageName);

        var packPanel = new YAHOO.widget.Panel(winID, {
                            width:"600px",
                            fixedcenter:true,
                            underlay:"shadow",
                            close:true,
                            visible:true,
                            draggable:true,
                            zindex:4,
                            modal:true});

        packPanel.setHeader("Package Details: " + packageName);

        var packDetTabs = new YAHOO.widget.TabView();

        for(var j = 0; j < bundleDocs.length; j++){
            var activeS = false;
            var strC = bundleDocs[j];
            if (j == 0){
                activeS = true;
            }
            var index = strC.indexOf('/');
            var docName = strC.substr(index+1);
            var indexDot = docName.indexOf('.');
            var idTxt = docName.substr(0, (indexDot-1)) + j;
            var textAreaStr = '<textarea id="'+ idTxt + '"` class="codepress html" style="width: 568px; height:570px;"></textarea>';
            packDetTabs.addTab(new YAHOO.widget.Tab({
                            label:docName,
                            content:textAreaStr,
                            active:activeS}));
        }

        var tabsDiv = document.createElement('div');
        packDetTabs.appendTo(tabsDiv);

        packPanel.setBody(tabsDiv);
        packPanel.cfg.setProperty("underlay","matte");
        packPanel.render("content");

        for(var k = 0; k < bundleDocs.length; k++){
            var strC = bundleDocs[k];
            var index = strC.indexOf('/');
            var docName = strC.substr(index+1);
            var indexDot = docName.indexOf('.');
            var idTxt = docName.substr(0, (indexDot-1)) + k;
            var bundleUrl = bundleDataUrl + strC;
            var responseText = org.apache.ode.XHRObject.xhrSyncGetRequest(bundleUrl, true);
            //var resStr = responseText.replace(/<\//g, '\n</');
            //resStr = resStr.replace(/>\n>\n<\//g, '>\n</');

            if(typeof responseText == 'string'){
                var textAreaEle = document.getElementById(idTxt);
                textAreaEle.appendChild(document.createTextNode(responseText));
            }else{
                org.apache.ode.Widgets.alert('Response Type not recognized for document: '+ docName);
            }
        }
    }

    function undeployPackage(packageName){
        try{
            function handleYes(){
                var response;
                try{
                    response = DeploymentService.undeploy(packageName);
                } catch (e) {
                    if (typeof(e) == "string") {
                        org.apache.ode.Widgets.alert("Exception occured:\n" + e.toString());
                    }
                    else {
                        org.apache.ode.Widgets.alert("Exception occurred!");
                    }
                }
                if(response == true){
                    org.apache.ode.Widgets.alert('Package '+ packageName + 'undeployed successfully.');
                    org.apache.ode.DeploymentHandling.populateDeployedPacks();
                }else{
                    org.apache.ode.Widgets.alert('Error occurred during undeployment or undeplyment unsuccessful.');
                }
            }

            function handleNo(){
                org.apache.ode.Widgets.alert('Package undeployment cancelled!');
            }

            var msg = 'Dou you want to undeploy package ' + packageName + '?';

            org.apache.ode.Widgets.operationConfirm(msg,handleYes, handleNo);

        }catch(e){
            if(typeof e == 'string'){
                org.apache.ode.Widgets.alert("Exception occurred while undeploying the package: " + e.toString());
            }else{
                org.apache.ode.Widgets.alert("Exception occurred while undeploying the package.");
            }
        }
    }

    var ns = org.apache.ode.DeploymentHandling;
    ns.getDeployedPackages = getDeployedPackages;
    ns.undeployPackage = undeployPackage;
    ns.viewPackDetails = viewPackDetails;
    ns.populateDeployedPacks = populateDeployedPackages;
})();

