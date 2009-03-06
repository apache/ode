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
var address = baseURL + baseDirectoryName + "/processes/ProcessManagement";
//var address = baseURL + "ode/processes/ProcessManagement";

var ProcessManagementService = new WebServiceClient("ProcessManagementPort");

ProcessManagementService.getExtensibilityElements = function getExtensibilityElements(/* aidsType */ _aids, /* QName */ _pid) {
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.getExtensibilityElements.callback != null && typeof(this.getExtensibilityElements.callback) == 'function');
    request =
    '<p:getExtensibilityElements xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_aids == null ? '' : '<aids>' + _aids + '</aids>') +
    (_pid == null ? '' : '<pid>' + this._encodeXML(_pid) + '</pid>') +
    '</p:getExtensibilityElements>';

    if (isAsync) {
        try {
            this._call(
                    "getExtensibilityElements",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tProcessInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getExtensibilityElements.callback, this.getExtensibilityElements.onError)
                    );
        } catch (e) {
            var error;
            if (WebServiceError.prototype.isPrototypeOf(e)) {
                error = e;
            } else if (typeof(e) == "string") {
                error = new WebServiceError(e, "Internal Error");
            } else {
                error = new WebServiceError(e.description, e.number, e.number);
            }
            this.getExtensibilityElements.onError(error);
        }
    } else {
        try {
            response = this._call("getExtensibilityElements", request);
            resultValue = /* tProcessInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
ProcessManagementService.getExtensibilityElements.callback = null;

ProcessManagementService.activate =
function activate(/* String */ _name, /* String */ _nmspURL, /* String */ _prefix)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.activate.callback != null && typeof(this.activate.callback) == 'function');
    request =
    '<p:activate xmlns:p="http://www.apache.org/ode/pmapi">' +
    ((_name != null && _nmspURL != null && _prefix != null) ? '<pid xmlns:' + this._encodeXML(_prefix) + '="' + this._encodeXML(_nmspURL) + '">' +
                                                              this._encodeXML(_prefix) + ':' + this._encodeXML(_name) + '</pid>' : '') +
    '</p:activate>';

    if (isAsync) {
        try {
            this._call(
                    "activate",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tProcessInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.activate.callback, this.activate.onError)
                    );
        } catch (e) {
            var error;
            if (WebServiceError.prototype.isPrototypeOf(e)) {
                error = e;
            } else if (typeof(e) == "string") {
                error = new WebServiceError(e, "Internal Error");
            } else {
                error = new WebServiceError(e.description, e.number, e.number);
            }
            this.activate.onError(error);
        }
    } else {
        try {
            response = this._call("activate", request);
            resultValue = /* tProcessInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
ProcessManagementService.activate.callback = null;

ProcessManagementService.setProcessProperty =
function setProcessProperty(/* QName */ _propertyName, /* string */ _propertyValue, /* QName */ _pid)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.setProcessProperty.callback != null && typeof(this.setProcessProperty.callback) == 'function');
    request =
    '<p:setProcessProperty xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_propertyName == null ? '' : '<propertyName>' + this._encodeXML(_propertyName) + '</propertyName>') +
    (_propertyValue == null ? '' : '<propertyValue>' + this._encodeXML(_propertyValue) + '</propertyValue>') +
    (_pid == null ? '' : '<pid>' + this._encodeXML(_pid) + '</pid>') +
    '</p:setProcessProperty>';

    if (isAsync) {
        try {
            this._call(
                    "setProcessProperty",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tProcessInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.setProcessProperty.callback, this.setProcessProperty.onError)
                    );
        } catch (e) {
            var error;
            if (WebServiceError.prototype.isPrototypeOf(e)) {
                error = e;
            } else if (typeof(e) == "string") {
                error = new WebServiceError(e, "Internal Error");
            } else {
                error = new WebServiceError(e.description, e.number, e.number);
            }
            this.setProcessProperty.onError(error);
        }
    } else {
        try {
            response = this._call("setProcessProperty", request);
            resultValue = /* tProcessInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
ProcessManagementService.setProcessProperty.callback = null;

ProcessManagementService.getProcessInfoCustom =
function getProcessInfoCustom(/* string */ _customizer, /* QName */ _pid)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.getProcessInfoCustom.callback != null && typeof(this.getProcessInfoCustom.callback) == 'function');
    request =
    '<p:getProcessInfoCustom xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_customizer == null ? '' : '<customizer>' + this._encodeXML(_customizer) + '</customizer>') +
    (_pid == null ? '' : '<pid>' + this._encodeXML(_pid) + '</pid>') +
    '</p:getProcessInfoCustom>';

    if (isAsync) {
        try {
            this._call(
                    "getProcessInfoCustom",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tProcessInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getProcessInfoCustom.callback, this.getProcessInfoCustom.onError)
                    );
        } catch (e) {
            var error;
            if (WebServiceError.prototype.isPrototypeOf(e)) {
                error = e;
            } else if (typeof(e) == "string") {
                error = new WebServiceError(e, "Internal Error");
            } else {
                error = new WebServiceError(e.description, e.number, e.number);
            }
            this.getProcessInfoCustom.onError(error);
        }
    } else {
        try {
            response = this._call("getProcessInfoCustom", request);
            resultValue = /* tProcessInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
ProcessManagementService.getProcessInfoCustom.callback = null;

ProcessManagementService.listProcessesCustom =
function listProcessesCustom(/* string */ _filter, /* string */ _customizer, /* string */ _orderKeys)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.listProcessesCustom.callback != null && typeof(this.listProcessesCustom.callback) == 'function');
    request =
    '<p:listProcessesCustom xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_filter == null ? '' : '<filter>' + this._encodeXML(_filter) + '</filter>') +
    (_customizer == null ? '' : '<customizer>' + this._encodeXML(_customizer) + '</customizer>') +
    (_orderKeys == null ? '' : '<orderKeys>' + this._encodeXML(_orderKeys) + '</orderKeys>') +
    '</p:listProcessesCustom>';

    if (isAsync) {
        try {
            this._call(
                    "listProcessesCustom",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tProcessInfoList */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.listProcessesCustom.callback, this.listProcessesCustom.onError)
                    );
        } catch (e) {
            var error;
            if (WebServiceError.prototype.isPrototypeOf(e)) {
                error = e;
            } else if (typeof(e) == "string") {
                error = new WebServiceError(e, "Internal Error");
            } else {
                error = new WebServiceError(e.description, e.number, e.number);
            }
            this.listProcessesCustom.onError(error);
        }
    } else {
        try {
            response = this._call("listProcessesCustom", request);
            resultValue = /* tProcessInfoList */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
ProcessManagementService.listProcessesCustom.callback = null;

ProcessManagementService.getProcessInfo =
function getProcessInfo(/* String */ _name, /* String */ _nmspURL, /* String */ _prefix)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.getProcessInfo.callback != null && typeof(this.getProcessInfo.callback) == 'function');
    request =
    '<p:getProcessInfo xmlns:p="http://www.apache.org/ode/pmapi">' +
    ((_name != null && _nmspURL != null && _prefix != null) ? '<pid xmlns:' + this._encodeXML(_prefix) + '="' + this._encodeXML(_nmspURL) + '">' +
                                                              this._encodeXML(_prefix) + ':' + this._encodeXML(_name) + '</pid>' : '') +
    '</p:getProcessInfo>';

    if (isAsync) {
        try {
            this._call(
                    "getProcessInfo",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tProcessInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getProcessInfo.callback, this.getProcessInfo.onError)
                    );
        } catch (e) {
            var error;
            if (WebServiceError.prototype.isPrototypeOf(e)) {
                error = e;
            } else if (typeof(e) == "string") {
                error = new WebServiceError(e, "Internal Error");
            } else {
                error = new WebServiceError(e.description, e.number, e.number);
            }
            this.getProcessInfo.onError(error);
        }
    } else {
        try {
            response = this._call("getProcessInfo", request);
            resultValue = /* tProcessInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
ProcessManagementService.getProcessInfo.callback = null;

ProcessManagementService.listProcesses =
function listProcesses(/* string */ _filter, /* string */ _orderKeys)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.listProcesses.callback != null && typeof(this.listProcesses.callback) == 'function');
    request =
    '<p:listProcesses xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_filter == null ? '' : '<filter>' + this._encodeXML(_filter) + '</filter>') +
    (_orderKeys == null ? '' : '<orderKeys>' + this._encodeXML(_orderKeys) + '</orderKeys>') +
    '</p:listProcesses>';

    if (isAsync) {
        try {
            this._call(
                    "listProcesses",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tProcessInfoList */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.listProcesses.callback, this.listProcesses.onError)
                    );
        } catch (e) {
            var error;
            if (WebServiceError.prototype.isPrototypeOf(e)) {
                error = e;
            } else if (typeof(e) == "string") {
                error = new WebServiceError(e, "Internal Error");
            } else {
                error = new WebServiceError(e.description, e.number, e.number);
            }
            this.listProcesses.onError(error);
        }
    } else {
        try {
            response = this._call("listProcesses", request);
            resultValue = /* tProcessInfoList */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
ProcessManagementService.listProcesses.callback = null;

ProcessManagementService.setRetired =
function setRetired(/* boolean */ _retired, /* String */ _name, /* String */ _nmspURL, /* String */ _prefix)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.setRetired.callback != null && typeof(this.setRetired.callback) == 'function');
    request =
    '<p:setRetired xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_retired == null ? '' : '<retired>' + this._encodeXML(_retired) + '</retired>') +
    ((_name != null && _nmspURL != null && _prefix != null) ? '<pid xmlns:' + this._encodeXML(_prefix) + '="' + this._encodeXML(_nmspURL) + '">' +
                                                              this._encodeXML(_prefix) + ':' + this._encodeXML(_name) + '</pid>' : '') +
    '</p:setRetired>';

    if (isAsync) {
        try {
            this._call(
                    "setRetired",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tProcessInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.setRetired.callback, this.setRetired.onError)
                    );
        } catch (e) {
            var error;
            if (WebServiceError.prototype.isPrototypeOf(e)) {
                error = e;
            } else if (typeof(e) == "string") {
                error = new WebServiceError(e, "Internal Error");
            } else {
                error = new WebServiceError(e.description, e.number, e.number);
            }
            this.setRetired.onError(error);
        }
    } else {
        try {
            response = this._call("setRetired", request);
            resultValue = /* tProcessInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
ProcessManagementService.setRetired.callback = null;

ProcessManagementService.setProcessPropertyNode =
function setProcessPropertyNode(/* QName */ _propertyName, /* anyType */ _propertyValue, /* QName */ _pid)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.setProcessPropertyNode.callback != null && typeof(this.setProcessPropertyNode.callback) == 'function');
    request =
    '<p:setProcessPropertyNode xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_propertyName == null ? '' : '<propertyName>' + this._encodeXML(_propertyName) + '</propertyName>') +
    (_propertyValue == null ? '' : '<propertyValue>' + _propertyValue + '</propertyValue>') +
    (_pid == null ? '' : '<pid>' + this._encodeXML(_pid) + '</pid>') +
    '</p:setProcessPropertyNode>';

    if (isAsync) {
        try {
            this._call(
                    "setProcessPropertyNode",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tProcessInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.setProcessPropertyNode.callback, this.setProcessPropertyNode.onError)
                    );
        } catch (e) {
            var error;
            if (WebServiceError.prototype.isPrototypeOf(e)) {
                error = e;
            } else if (typeof(e) == "string") {
                error = new WebServiceError(e, "Internal Error");
            } else {
                error = new WebServiceError(e.description, e.number, e.number);
            }
            this.setProcessPropertyNode.onError(error);
        }
    } else {
        try {
            response = this._call("setProcessPropertyNode", request);
            resultValue = /* tProcessInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
ProcessManagementService.setProcessPropertyNode.callback = null;

ProcessManagementService.listAllProcesses = function listAllProcesses() {
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.listAllProcesses.callback != null && typeof(this.listAllProcesses.callback) == 'function');
    request =
    '<p:listAllProcesses xmlns:p="http://www.apache.org/ode/pmapi">' +
    '</p:listAllProcesses>';

    if (isAsync) {
        try {
            this._call(
                    "listAllProcesses",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tProcessInfoList */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.listAllProcesses.callback, this.listAllProcesses.onError)
                    );
        } catch (e) {
            var error;
            if (WebServiceError.prototype.isPrototypeOf(e)) {
                error = e;
            } else if (typeof(e) == "string") {
                error = new WebServiceError(e, "Internal Error");
            } else {
                error = new WebServiceError(e.description, e.number, e.number);
            }
            this.listAllProcesses.onError(error);
        }
    } else {
        try {
            response = this._call("listAllProcesses", request);
            resultValue = /* tProcessInfoList */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
ProcessManagementService.listAllProcesses.callback = null;



// WebService object.
function WebServiceClient(endpointName)
{
    this.readyState = 0;
    this.onreadystatechange = null;

    //public accessors for manually intervening in setting the address (e.g. supporting tcpmon)
    this.getAddress = function (endpointName)
    {
        return this._endpointDetails[endpointName].address;
    }

    this.setAddress = function (endpointName, address)
    {
        this._endpointDetails[endpointName].address = address;
    }

    // private helper functions
    this._getWSRequest = function()
    {
        var wsrequest;
        try {
            wsrequest = new WSRequest();
        } catch(e) {
            try {
                wsrequest = new ActiveXObject("WSRequest");
            } catch(e) {
                try {
                    wsrequest = new SOAPHttpRequest();
                    netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect");
                } catch (e) {
                    throw new WebServiceError("WSRequest object not defined.", "WebServiceClient._getWSRequest() cannot instantiate WSRequest object.");
                }
            }
        }
        return wsrequest;
    }

    this._endpointDetails =
    {
        "ProcessManagementPort": {
            "type" : "SOAP11",
            "address" : address,
            "action" : {
                "getExtensibilityElements" : "http://www.apache.org/ode/pmapi/ProcessManagementPortType/getExtensibilityElementsRequest",
                "activate" : "http://www.apache.org/ode/pmapi/ProcessManagementPortType/activateRequest",
                "setProcessProperty" : "http://www.apache.org/ode/pmapi/ProcessManagementPortType/setProcessPropertyRequest",
                "getProcessInfoCustom" : "http://www.apache.org/ode/pmapi/ProcessManagementPortType/getProcessInfoCustomRequest",
                "listProcessesCustom" : "http://www.apache.org/ode/pmapi/ProcessManagementPortType/listProcessesCustomRequest",
                "getProcessInfo" : "http://www.apache.org/ode/pmapi/ProcessManagementPortType/getProcessInfoRequest",
                "listProcesses" : "http://www.apache.org/ode/pmapi/ProcessManagementPortType/listProcessesRequest",
                "setRetired" : "http://www.apache.org/ode/pmapi/ProcessManagementPortType/setRetiredRequest",
                "setProcessPropertyNode" : "http://www.apache.org/ode/pmapi/ProcessManagementPortType/setProcessPropertyNodeRequest",
                "listAllProcesses" : "http://www.apache.org/ode/pmapi/ProcessManagementPortType/listAllProcessesRequest"
            }
        }
    };
    this.endpoint = endpointName;

    this._encodeXML = function (value) {
        var re;
        var str = value.toString();
        re = /&/g;
        str = str.replace(re, "&amp;");
        re = /</g;
        str = str.replace(re, "&lt;");
        return(str);
    };

    this._call = function (opName, reqContent, callback, userdata)
    {
        var details = this._endpointDetails[this.endpoint];
        if (details.type == 'SOAP12') this._options.useSOAP = 1.2;
        else if (details.type == 'SOAP11') this._options.useSOAP = 1.1;
        else if (details.type == 'HTTP') this._options.useSOAP = false;

        if (details.action != null) {
            this._options.useWSA = true;
            this._options.action = details.action[opName];
        } else if (details.soapaction != null) {
            this._options.useWSA = false;
            this._options.action = details.soapaction[opName];
        } else {
            this._options.useWSA = false;
            this._options.action = undefined;
        }

        if (details["httpmethod"] != null) {
            this._options.HTTPMethod = details.httpmethod[opName];
        } else {
            this._options.HTTPMethod = null;
        }

        if (details["httpinputSerialization"] != null) {
            this._options.HTTPInputSerialization = details.httpinputSerialization[opName];
        } else {
            this._options.HTTPInputSerialization = null;
        }

        if (details["httplocation"] != null) {
            this._options.HTTPLocation = details.httplocation[opName];
        } else {
            this._options.HTTPLocation = null;
        }

        if (details["httpignoreUncited"] != null) {
            this._options.HTTPLocationIgnoreUncited = details.httpignoreUncited[opName];
        } else {
            this._options.HTTPLocationIgnoreUncited = null;
        }

        if (details["httpqueryParameterSeparator"] != null) {
            this._options.HTTPQueryParameterSeparator = details.httpqueryParameterSeparator[opName];
        } else {
            this._options.HTTPQueryParameterSeparator = null;
        }

        var isAsync = (typeof(callback) == 'function');

        var thisRequest = this._getWSRequest();
        if (isAsync) {
            thisRequest._userdata = userdata;
            thisRequest.onreadystatechange =
            function() {
                if (thisRequest.readyState == 4) {
                    callback(thisRequest, userdata);
                }
            }
        }
        thisRequest.open(this._options, details.address, isAsync);
        thisRequest.send(reqContent);
        if (isAsync) {
            return "";
        } else {
            try {
                var resultContent = thisRequest.responseText;
                ProcessManagementService.text = resultContent;
                if (resultContent == "") {
                    throw new WebServiceError("No response", "WebServiceClient._call() did not recieve a response to a synchronous request.");
                }
                var resultXML = thisRequest.responseXML;
            } catch (e) {
                throw new WebServiceError(e);
            }
            return resultXML;
        }
    }
}
WebServiceClient.visible = false;

// library function for dynamically converting an element with js:type annotation to a Javascript type.
convertJSType.visible = false;
function convertJSType(element, isWrapped) {
    if (element == null) return "";
    var extractedValue = WSRequest.util._stringValue(element);
    var resultValue, i;
    var type = element.getAttribute("js:type");
    if (type == null) {
        type = "xml";
    } else {
        type = type.toString();
    }
    switch (type) {
        case "string":
            return extractedValue;
            break;
        case "number":
            return parseFloat(extractedValue);
            break;
        case "boolean":
            return extractedValue == "true" || extractedValue == "1";
            break;
        case "date":
            return xs_dateTime_to_date(extractedValue);
            break;
        case "array":
            resultValue = new Array();
            for (i = 0; i < element.childNodes.length; i++) {
                resultValue = resultValue.concat(convertJSType(element.childNodes[i]));
            }
            return(resultValue);
            break;
        case "object":
            resultValue = new Object();
            for (i = 0; i < element.childNodes.length; i++) {
                resultValue[element.childNodes[i].tagName] = convertJSType(element.childNodes[i]);
            }
            return(resultValue);
            break;
        case "xmllist":
            return element.childNodes;
            break;
        case "xml":
        default:
            if (isWrapped == true)
                return element.firstChild;
            else return element;
            break;
    }
}

// library function for parsing xs:date, xs:time, and xs:dateTime types into Date objects.
function xs_dateTime_to_date(dateTime)
{
    var buffer = dateTime;
    var p = 0; // pointer to current parse location in buffer.

    var era, year, month, day, hour, minute, second, millisecond;

    // parse date, if there is one.
    if (buffer.substr(p, 1) == '-')
    {
        era = -1;
        p++;
    } else {
        era = 1;
    }

    if (buffer.charAt(p + 2) != ':')
    {
        year = era * buffer.substr(p, 4);
        p += 5;
        month = buffer.substr(p, 2);
        p += 3;
        day = buffer.substr(p, 2);
        p += 3;
    } else {
        year = 1970;
        month = 1;
        day = 1;
    }

    // parse time, if there is one
    if (buffer.charAt(p) != '+' && buffer.charAt(p) != '-')
    {
        hour = buffer.substr(p, 2);
        p += 3;
        minute = buffer.substr(p, 2);
        p += 3;
        second = buffer.substr(p, 2);
        p += 2;
        if (buffer.charAt(p) == '.')
        {
            millisecond = parseFloat(buffer.substr(p)) * 1000;
            // Note that JS fractional seconds are significant to 3 places - xs:time is significant to more -
            // though implementations are only required to carry 3 places.
            p++;
            while (buffer.charCodeAt(p) >= 48 && buffer.charCodeAt(p) <= 57) p++;
        } else {
            millisecond = 0;
        }
    } else {
        hour = 0;
        minute = 0;
        second = 0;
        millisecond = 0;
    }

    var tzhour = 0;
    var tzminute = 0;
    // parse time zone
    if (buffer.charAt(p) != 'Z' && buffer.charAt(p) != '') {
        var sign = (buffer.charAt(p) == '-' ? -1 : +1);
        p++;
        tzhour = sign * buffer.substr(p, 2);
        p += 3;
        tzminute = sign * buffer.substr(p, 2);
    }

    var thisDate = new Date();
    thisDate.setUTCFullYear(year);
    thisDate.setUTCMonth(month - 1);
    thisDate.setUTCDate(day);
    thisDate.setUTCHours(hour);
    thisDate.setUTCMinutes(minute);
    thisDate.setUTCSeconds(second);
    thisDate.setUTCMilliseconds(millisecond);
    thisDate.setUTCHours(thisDate.getUTCHours() - tzhour);
    thisDate.setUTCMinutes(thisDate.getUTCMinutes() - tzminute);
    return thisDate;
}
xs_dateTime_to_date.visible = false;

function scheme(url) {
    var s = url.substring(0, url.indexOf(':'));
    return s;
}
scheme.visible = false;

function domain(url) {
    var d = url.substring(url.indexOf('://') + 3, url.indexOf('/', url.indexOf('://') + 3));
    return d;
}
domain.visible = false;

function domainNoPort(url) {
    var d = domain(url);
    if (d.indexOf(":") >= 0)
        d = d.substring(0, d.indexOf(':'));
    return d;
}
domainNoPort.visible = false;

try {
    var secureEndpoint = "";
    var pageUrl = document.URL;
    var pageScheme = scheme(pageUrl);
    // only attempt fixup if we're from an http/https domain ('file:' works fine on IE without fixup)
    if (pageScheme == "http" || pageScheme == "https") {
        var pageDomain = domain(pageUrl);
        var pageDomainNoPort = domainNoPort(pageUrl);
        var endpoints = ProcessManagementService._endpointDetails;
        // loop through each available endpoint
        for (var i in endpoints) {
            var address = endpoints[i].address;
            // if we're in a secure domain, set the endpoint to the first secure endpoint we come across
            if (secureEndpoint == "" && pageScheme == "https" && scheme(address) == "https") {
                secureEndpoint = i;
                ProcessManagementService.endpoint = secureEndpoint;
            }
            // if we're in a known localhost domain, rewrite the endpoint domain so that we won't get
            //  a bogus xss violation
            if (pageDomainNoPort.indexOf('localhost') == 0 || pageDomainNoPort.indexOf('127.0.0.1') == 0) {
                endpoints[i].address = address.replace(domainNoPort(address), pageDomainNoPort);
            }
        }
    }
} catch (e) {
}

