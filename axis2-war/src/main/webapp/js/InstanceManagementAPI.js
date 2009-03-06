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
var address = baseURL + baseDirectoryName + "/processes/InstanceManagement";
//var address = baseURL + "ode/processes/InstanceManagement";

var InstanceManagementService = new WebService("InstanceManagementPort");

InstanceManagementService.listInstances =
function listInstances(/* string */ _filter, /* int */ _limit, /* string */ _order)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.listInstances.callback != null && typeof(this.listInstances.callback) == 'function');
    request =
    '<p:listInstances xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_filter == null ? '' : '<filter>' + this._encodeXML(_filter) + '</filter>') +
    (_limit == null ? '' : '<limit>' + this._encodeXML(_limit) + '</limit>') +
    (_order == null ? '' : '<order>' + this._encodeXML(_order) + '</order>') +
    '</p:listInstances>';

    if (isAsync) {
        try {
            this._call(
                    "listInstances",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tInstanceInfoList */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.listInstances.callback, this.listInstances.onError)
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
            this.listInstances.onError(error);
        }
    } else {
        try {
            response = this._call("listInstances", request);
            resultValue = /* tInstanceInfoList */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.listInstances.callback = null;

InstanceManagementService.resume =
function resume(/* long */ _iid)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.resume.callback != null && typeof(this.resume.callback) == 'function');
    request =
    '<p:resume xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_iid == null ? '' : '<iid>' + this._encodeXML(_iid) + '</iid>') +
    '</p:resume>';

    if (isAsync) {
        try {
            this._call(
                    "resume",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tInstanceInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.resume.callback, this.resume.onError)
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
            this.resume.onError(error);
        }
    } else {
        try {
            response = this._call("resume", request);
            resultValue = /* tInstanceInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.resume.callback = null;

InstanceManagementService.terminate =
function terminate(/* long */ _iid)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.terminate.callback != null && typeof(this.terminate.callback) == 'function');
    request =
    '<p:terminate xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_iid == null ? '' : '<iid>' + this._encodeXML(_iid) + '</iid>') +
    '</p:terminate>';

    if (isAsync) {
        try {
            this._call(
                    "terminate",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tInstanceInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.terminate.callback, this.terminate.onError)
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
            this.terminate.onError(error);
        }
    } else {
        try {
            response = this._call("terminate", request);
            resultValue = /* tInstanceInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.terminate.callback = null;

InstanceManagementService.listAllInstances =
function listAllInstances()
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.listAllInstances.callback != null && typeof(this.listAllInstances.callback) == 'function');
    request =
    '<p:listAllInstances xmlns:p="http://www.apache.org/ode/pmapi">' +
    '</p:listAllInstances>';

    if (isAsync) {
        try {
            this._call(
                    "listAllInstances",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tInstanceInfoList */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.listAllInstances.callback, this.listAllInstances.onError)
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
            this.listAllInstances.onError(error);
        }
    } else {
        try {
            response = this._call("listAllInstances", request);
            resultValue = /* tInstanceInfoList */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.listAllInstances.callback = null;

InstanceManagementService.suspend =
function suspend(/* long */ _iid)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.suspend.callback != null && typeof(this.suspend.callback) == 'function');
    request =
    '<p:suspend xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_iid == null ? '' : '<iid>' + this._encodeXML(_iid) + '</iid>') +
    '</p:suspend>';

    if (isAsync) {
        try {
            this._call(
                    "suspend",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tInstanceInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.suspend.callback, this.suspend.onError)
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
            this.suspend.onError(error);
        }
    } else {
        try {
            response = this._call("suspend", request);
            resultValue = /* tInstanceInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.suspend.callback = null;

InstanceManagementService.listAllInstancesWithLimit =
function listAllInstancesWithLimit(/* int */ _payload)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.listAllInstancesWithLimit.callback != null && typeof(this.listAllInstancesWithLimit.callback) == 'function');
    request =
    '<p:listAllInstancesWithLimit xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_payload == null ? '' : '<payload>' + this._encodeXML(_payload) + '</payload>') +
    '</p:listAllInstancesWithLimit>';

    if (isAsync) {
        try {
            this._call(
                    "listAllInstancesWithLimit",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tInstanceInfoList */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.listAllInstancesWithLimit.callback, this.listAllInstancesWithLimit.onError)
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
            this.listAllInstancesWithLimit.onError(error);
        }
    } else {
        try {
            response = this._call("listAllInstancesWithLimit", request);
            resultValue = /* tInstanceInfoList */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.listAllInstancesWithLimit.callback = null;

InstanceManagementService.getScopeInfo =
function getScopeInfo(/* long */ _siid)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.getScopeInfo.callback != null && typeof(this.getScopeInfo.callback) == 'function');
    request =
    '<p:getScopeInfo xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_siid == null ? '' : '<siid>' + this._encodeXML(_siid) + '</siid>') +
    '</p:getScopeInfo>';

    if (isAsync) {
        try {
            this._call(
                    "getScopeInfo",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tScopeInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getScopeInfo.callback, this.getScopeInfo.onError)
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
            this.getScopeInfo.onError(error);
        }
    } else {
        try {
            response = this._call("getScopeInfo", request);
            resultValue = /* tScopeInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.getScopeInfo.callback = null;

InstanceManagementService.recoverActivity =
function recoverActivity(/* string */ _action, /* long */ _aid, /* long */ _iid)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.recoverActivity.callback != null && typeof(this.recoverActivity.callback) == 'function');
    request =
    '<p:recoverActivity xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_action == null ? '' : '<action>' + this._encodeXML(_action) + '</action>') +
    (_aid == null ? '' : '<aid>' + this._encodeXML(_aid) + '</aid>') +
    (_iid == null ? '' : '<iid>' + this._encodeXML(_iid) + '</iid>') +
    '</p:recoverActivity>';

    if (isAsync) {
        try {
            this._call(
                    "recoverActivity",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tInstanceInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.recoverActivity.callback, this.recoverActivity.onError)
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
            this.recoverActivity.onError(error);
        }
    } else {
        try {
            response = this._call("recoverActivity", request);
            resultValue = /* tInstanceInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.recoverActivity.callback = null;

InstanceManagementService.queryInstances =
function queryInstances(/* string */ _payload)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.queryInstances.callback != null && typeof(this.queryInstances.callback) == 'function');
    request =
    '<p:queryInstances xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_payload == null ? '' : '<payload>' + this._encodeXML(_payload) + '</payload>') +
    '</p:queryInstances>';

    if (isAsync) {
        try {
            this._call(
                    "queryInstances",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tInstanceInfoList */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.queryInstances.callback, this.queryInstances.onError)
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
            this.queryInstances.onError(error);
        }
    } else {
        try {
            response = this._call("queryInstances", request);
            resultValue = /* tInstanceInfoList */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.queryInstances.callback = null;

InstanceManagementService.getInstanceInfo =
function getInstanceInfo(/* long */ _iid)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.getInstanceInfo.callback != null && typeof(this.getInstanceInfo.callback) == 'function');
    request =
    '<p:getInstanceInfo xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_iid == null ? '' : '<iid>' + this._encodeXML(_iid) + '</iid>') +
    '</p:getInstanceInfo>';

    if (isAsync) {
        try {
            this._call(
                    "getInstanceInfo",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tInstanceInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getInstanceInfo.callback, this.getInstanceInfo.onError)
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
            this.getInstanceInfo.onError(error);
        }
    } else {
        try {
            response = this._call("getInstanceInfo", request);
            resultValue = /* tInstanceInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.getInstanceInfo.callback = null;

InstanceManagementService.listEvents =
function listEvents(/* string */ _eventFilter, /* int */ _maxCount, /* string */ _instanceFilter)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.listEvents.callback != null && typeof(this.listEvents.callback) == 'function');
    request =
    '<p:listEvents xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_eventFilter == null ? '' : '<eventFilter>' + this._encodeXML(_eventFilter) + '</eventFilter>') +
    (_maxCount == null ? '' : '<maxCount>' + this._encodeXML(_maxCount) + '</maxCount>') +
    (_instanceFilter == null ? '' : '<instanceFilter>' + this._encodeXML(_instanceFilter) + '</instanceFilter>') +
    '</p:listEvents>';

    if (isAsync) {
        try {
            this._call(
                    "listEvents",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tEventInfoList */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.listEvents.callback, this.listEvents.onError)
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
            this.listEvents.onError(error);
        }
    } else {
        try {
            response = this._call("listEvents", request);
            resultValue = /* tEventInfoList */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.listEvents.callback = null;

InstanceManagementService.deleteInstance =
function deleteInstance(/* string */ _filter)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.deleteInstance.callback != null && typeof(this.deleteInstance.callback) == 'function');
    request =
    '<p:delete xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_filter == null ? '' : '<filter>' + this._encodeXML(_filter) + '</filter>') +
    '</p:delete>';

    if (isAsync) {
        try {
            this._call(
                    "delete",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* iidsType */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.deleteInstance.callback, this.deleteInstance.onError)
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
            this.deleteInstance.onError(error);
        }
    } else {
        try {
            response = this._call("delete", request);
            resultValue = /* iidsType */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.deleteInstance.callback = null;

InstanceManagementService.getEventTimeline =
function getEventTimeline(/* string */ _eventFilter, /* string */ _instanceFilter)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.getEventTimeline.callback != null && typeof(this.getEventTimeline.callback) == 'function');
    request =
    '<p:getEventTimeline xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_eventFilter == null ? '' : '<eventFilter>' + this._encodeXML(_eventFilter) + '</eventFilter>') +
    (_instanceFilter == null ? '' : '<instanceFilter>' + this._encodeXML(_instanceFilter) + '</instanceFilter>') +
    '</p:getEventTimeline>';

    if (isAsync) {
        try {
            this._call(
                    "getEventTimeline",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* listType */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getEventTimeline.callback, this.getEventTimeline.onError)
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
            this.getEventTimeline.onError(error);
        }
    } else {
        try {
            response = this._call("getEventTimeline", request);
            resultValue = /* listType */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.getEventTimeline.callback = null;

InstanceManagementService.fault =
function fault(/* long */ _iid)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.fault.callback != null && typeof(this.fault.callback) == 'function');
    request =
    '<p:fault xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_iid == null ? '' : '<iid>' + this._encodeXML(_iid) + '</iid>') +
    '</p:fault>';

    if (isAsync) {
        try {
            this._call(
                    "fault",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tInstanceInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.fault.callback, this.fault.onError)
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
            this.fault.onError(error);
        }
    } else {
        try {
            response = this._call("fault", request);
            resultValue = /* tInstanceInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.fault.callback = null;

InstanceManagementService.getScopeInfoWithActivity =
function getScopeInfoWithActivity(/* long */ _sid, /* boolean */ _activityInfo)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.getScopeInfoWithActivity.callback != null && typeof(this.getScopeInfoWithActivity.callback) == 'function');
    request =
    '<p:getScopeInfoWithActivity xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_sid == null ? '' : '<sid>' + this._encodeXML(_sid) + '</sid>') +
    (_activityInfo == null ? '' : '<activityInfo>' + this._encodeXML(_activityInfo) + '</activityInfo>') +
    '</p:getScopeInfoWithActivity>';

    if (isAsync) {
        try {
            this._call(
                    "getScopeInfoWithActivity",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tScopeInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getScopeInfoWithActivity.callback, this.getScopeInfoWithActivity.onError)
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
            this.getScopeInfoWithActivity.onError(error);
        }
    } else {
        try {
            response = this._call("getScopeInfoWithActivity", request);
            resultValue = /* tScopeInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.getScopeInfoWithActivity.callback = null;

InstanceManagementService.getVariableInfo =
function getVariableInfo(/* string */ _sid, /* string */ _varName)
{
    var isAsync, request, response, resultValue;

    this._options = new Array();
    isAsync = (this.getVariableInfo.callback != null && typeof(this.getVariableInfo.callback) == 'function');
    request =
    '<p:getVariableInfo xmlns:p="http://www.apache.org/ode/pmapi">' +
    (_sid == null ? '' : '<sid>' + this._encodeXML(_sid) + '</sid>') +
    (_varName == null ? '' : '<varName>' + this._encodeXML(_varName) + '</varName>') +
    '</p:getVariableInfo>';

    if (isAsync) {
        try {
            this._call(
                    "getVariableInfo",
                    request,
                    function(thisRequest, callbacks) {
                        if (thisRequest.error != null) {
                            callbacks[1](thisRequest.error);
                        } else {
                            response = thisRequest.responseXML;
                            if (response == null) {
                                resultValue = null;
                            } else {
                                resultValue = /* tVariableInfo */ response.documentElement;
                            }
                            callbacks[0](resultValue);
                        }
                    },
                    new Array(this.getVariableInfo.callback, this.getVariableInfo.onError)
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
            this.getVariableInfo.onError(error);
        }
    } else {
        try {
            response = this._call("getVariableInfo", request);
            resultValue = /* tVariableInfo */ response.documentElement;
            return resultValue;
        } catch (e) {
            if (typeof(e) == "string") throw(e);
            if (e.message) throw(e.message);
            throw (e.reason + e.detail);
        }
    }
    return null; // Suppress warnings when there is no return.
}
InstanceManagementService.getVariableInfo.callback = null;



// WebService object.
function WebService(endpointName)
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
                    throw new WebServiceError("WSRequest object not defined.", "WebService._getWSRequest() cannot instantiate WSRequest object.");
                }
            }
        }
        return wsrequest;
    }

    this._endpointDetails =
    {
        "InstanceManagementPort": {
            "type" : "SOAP11",
            "address" : address,
            "action" : {
                "listInstances" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/listInstancesRequest",
                "resume" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/resumeRequest",
                "terminate" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/terminateRequest",
                "listAllInstances" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/listAllInstancesRequest",
                "suspend" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/suspendRequest",
                "listAllInstancesWithLimit" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/listAllInstancesWithLimitRequest",
                "getScopeInfo" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/getScopeInfoRequest",
                "recoverActivity" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/recoverActivityRequest",
                "queryInstances" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/queryInstancesRequest",
                "getInstanceInfo" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/getInstanceInfoRequest",
                "listEvents" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/listEventsRequest",
                "delete" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/deleteRequest",
                "getEventTimeline" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/getEventTimelineRequest",
                "fault" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/faultRequest",
                "getScopeInfoWithActivity" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/getScopeInfoWithActivityRequest",
                "getVariableInfo" : "http://www.apache.org/ode/pmapi/InstanceManagementPortType/getVariableInfoRequest"
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
                if (resultContent == "") {
                    throw new WebServiceError("No response", "WebService._call() did not recieve a response to a synchronous request.");
                }
                var resultXML = thisRequest.responseXML;
            } catch (e) {
                throw new WebServiceError(e);
            }
            return resultXML;
        }
    }
}
WebService.visible = false;

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
        var endpoints = InstanceManagementService._endpointDetails;
        // loop through each available endpoint
        for (var i in endpoints) {
            var address = endpoints[i].address;
            // if we're in a secure domain, set the endpoint to the first secure endpoint we come across
            if (secureEndpoint == "" && pageScheme == "https" && scheme(address) == "https") {
                secureEndpoint = i;
                InstanceManagementService.endpoint = secureEndpoint;
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
