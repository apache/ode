/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.utils;

import java.net.URL;


/**
 * TestResources
 */
public class TestResources {

    public static URL getResource(String s) {
        URL url = TestResources.class.getResource(s);
        if (url == null) url = TestResources.class.getResource("/" + s);
        return url;
    }

    public static URL getLoanApprovalProcess() {
        return getResource("loanApprovalProcess.xml");
    }

    public static URL getDummyXML() {
        return getResource("dummyXML.xml");
    }

    public static URL getRetailerSchema() {
        return getResource("schema.xsd");
    }

    public static URL getRetailerWSDL() {
        return getResource("retailer.wsdl");
    }

    public static URL getBpelExampleWsdl1() {
        return getResource("bpel-example-wsdl-1.wsdl");
    }

    public static URL getBpelExampleWsdl1BadPLink() {
        return getResource("bpel-example-wsdl-1-bad-plink.wsdl");
    }

    public static URL getBpelPartnerLinkSchema() {
        return getResource("bpel-partner-link.xsd");
    }

    public static URL getBpelPropertySchema() {
        return getResource("bpel-property.xsd");
    }

    public static String[] getRetailerResources() {

        return new String[] {
                getWarehouseWSDL().toExternalForm(),
                getRetailerWSDL().toExternalForm(),
                getWsiConfigurationWsdl().toExternalForm(),
                getWsiWarehouseWsdl().toExternalForm(),
                "http://www.ws-i.org/SampleApplications/SupplyChainManagement/2002-08/RetailOrder.xsd",
                "http://www.ws-i.org/SampleApplications/SupplyChainManagement/2002-08/Configuration.xsd",
                "http://schemas.xmlsoap.org/soap/envelope/",
                "http://www.ws-i.org/SampleApplications/SupplyChainManagement/2002-08/Warehouse.xsd",
                "http://www.ws-i.org/SampleApplications/SupplyChainManagement/2002-08/RetailCatalog.xsd"};
    }

    public static String[] getWsiWarehouseResources() {
        return new String[] {
                "http://www.ws-i.org/SampleApplications/SupplyChainManagement/2002-08/Configuration.xsd",
                "http://schemas.xmlsoap.org/soap/envelope/",
                "http://www.ws-i.org/SampleApplications/SupplyChainManagement/2002-08/Warehouse.xsd",
                getWsiWarehouseWsdl().toExternalForm(),
                getWsiConfigurationWsdl().toExternalForm()
        };
    }
    public static URL getWarehouseWSDL() {
        return getResource("WarehouseImpl.wsdl");
    }

    public static URL getWsiWarehouseWsdl() {
        return getResource("Warehouse.wsdl");
    }

    public static URL getWsiConfigurationWsdl() {
        return getResource("Configuration.wsdl");
    }

    public static URL getBadXML() {
        return getResource("bad_xml.wsdl");
    }

    public static URL getNonWsdlGoodXml() {
        return getResource("good_xml_not_wsdl.wsdl");
    }

    public static URL getNonWsdlImport() {
        return getResource("good_wsdl_bad_import.wsdl");
    }

    public static URL getDeepWsdl() {
        return getResource("deep_wsdl_outer.wsdl");
    }

    public static URL getBadDeepWsdl() {
        return getResource("deep_bad_wsdl_outer.wsdl");
    }

    public static URL getBadDeepWsdlR2005() {
        return getResource("deep_wsdl_R2005_outer.wsdl");
    }

    public static String[] getDeepWsdlResources() {
        return new String[] {
                getDeepWsdl().toExternalForm(),
                getResource("deep_wsdl_inner1.wsdl").toExternalForm(),
                getResource("deep_wsdl_inner2.wsdl").toExternalForm(),
                getResource("empty.wsdl").toExternalForm()
        };
    }

    public static String[] getDeepSchemaImportWsdlResources() {
        return new String[] {
                getDeepSchemaImportWsdl().toExternalForm(),
                "bar://baz/qux",
                "http://schemas.xmlsoap.org/soap/envelope/",
                "http://www.ws-i.org/SampleApplications/SupplyChainManagement/2002-08/Configuration.xsd"
        };
    }

    public static URL getPlainOldXmlDocument() {
        return getResource("plain_old_xml_document.xml");
    }

    public static URL getInvalidButWellFormedWsdl() {
        return getResource("invalid_but_well_formed.wsdl");
    }

    public static URL getMissingDeepWsdl() {
        return getResource("deep_missing_import_wsdl_outer.wsdl");
    }

    public static URL getMissingSchemaImport() {
        return getResource("missing_schema_import.wsdl");
    }

    public static URL getMissingImportedSchemaImport() {
        return getResource("missing_imported_schema_import.wsdl");
    }

    public static URL getCircularWsdl(int i) {
        return getResource("circular" + i + ".wsdl");
    }

    public static URL getNonWsiSchemaImport() {
        return getResource("non_ws-i_schema_import.wsdl");
    }

    public static URL getDeepMissingSchemaImport() {
        return getResource("missing_schema_import_wrapper.wsdl");
    }

    public static URL getDeepNonWsiSchemaImport() {
        return getResource("non_ws-i_schema_import_wrapper.wsdl");
    }

    public static URL getBadXmlWsiSchemaImport() {
        return getResource("bad_xml_schema_import.wsdl");
    }

    public static URL getNotSchemaWsiSchemaImport() {
        return getResource("not_schema_schema_import.wsdl");
    }

    public static URL getDeepSchemaImportWsdl() {
        return getResource("wsdl_deep_schema_import.wsdl");
    }

    public static String[] getCircularWsdlResources() {
        String[] s = new String[4];
        for (int i=0; i < 4; ++i) {
            s[i] = getCircularWsdl(i+1).toExternalForm();
        }
        return s;
    }

    public static URL getNamespaceFromWsdlToXsd() {
        return getResource("namespace_from_wsdl_to_xsd.wsdl");
    }

    public static URL getWsdlSchema() {
        return SchemaBucket.getBp1_0WsdlSchema();
    }
}
