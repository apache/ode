<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
  <xsl:output method="xml"/>
  <xsl:param name="middle"/>

 <xsl:template match="node()|@*">
   <xsl:copy>
       <xsl:apply-templates select="@*"/>
       <xsl:apply-templates/>
   </xsl:copy>
 </xsl:template>

 <xsl:template match="TestPart">
    <!-- The root element is the one that will be used as a base for the assignment rvalue -->
    <xsl:element name="root">
      <xsl:element name="hello">
           <xsl:apply-templates />
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="content">
        <xsl:value-of select="concat(text(), $middle, ' World')"/>
  </xsl:template>

  <!-- The nilled() function does not work if your Saxon parser is not schema-aware -->
  <!--<xsl:template match="*[nilled(current())]"/>-->
  <!-- As a workaround, you may use boolean(@xsi:nil) instead of nilled(current()) -->
  <xsl:template match="*[boolean(@xsi:nil)]"/> 
</xsl:stylesheet>