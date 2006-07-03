<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output indent="yes"/>

<xsl:template match="buildresult">
  <html>
  <head>
    <title>Build Results</title>
  </head>
    <link href="primary.css" rel="stylesheet" type="text/css" />

  <body>

  <div>
  <h1>Workspace "<xsl:value-of select="workspace/@id"/>" Build Results </h1>
  <xsl:copy-of select="workspace/description"/>
  <table width="100%" class="blue">  
  <tbody>
    <tr><td class="header">Build Summary</td></tr>
    <tr><td>
     <table width="100%" class="summary">
    
    <tr>
      <td wdith="25%" class="label">Project</td>
      <td widht="50%" class="label">Compile</td>
      <td width="20%" class="label">Test</td>
      <td width="5%" class="label">Coverage</td>
    </tr>
    
  <xsl:for-each select="project">
    <tr>
       <td><a href="#{@id}"><xsl:value-of select="@id"/></a></td>
       <td><xsl:call-template name="status"/></td>
       <td><xsl:call-template name="test"/></td>
       <td bgcolor="black">0%</td>
     </tr>
     
  </xsl:for-each>
  </table>
  </td></tr>
  <tr><td class="header">Build Details</td></tr>
     <xsl:for-each select="project"> 
     <a name="{@id}"/>
     <table width="100%" cols="4" class="detail">
     <tr>
       <td width="25%" colspan="1"><b><xsl:value-of select="@id"/></b> </td>
       <td width="75%" colspan="3"><xsl:call-template name="status"/> </td>
     </tr>
     <tr>
       <td/>
       <td width="25%" colspan="1"><a href="{@id}.xml">Full Log</a></td>
       <td>
         <xsl:choose>
            <xsl:when test="./test"><a href="{@id}/junit-noframes.html">Test Results</a></xsl:when>
            <xsl:otherwise>No Test Results</xsl:otherwise>
         </xsl:choose>
       </td>
       <td>No Coverage Report</td>

     </tr>
     <tr>
     <td colspan="4" class="errors">
     	<pre>
	<xsl:for-each select=".//message[@priority='warn' or @priority='error']">
	   <xsl:value-of select="text()" />
	   <br/> 
	</xsl:for-each>
	</pre>
     </td>
     </tr>
     <tr><td width="100%" colspan="3" class="seperator">&#10;</td></tr>
     </table>
  </xsl:for-each>
  </tbody>
  </table>
  </div>
  </body>
  </html>

</xsl:template>

<xsl:template name="test">
  <xsl:attribute name="bgcolor">
     <xsl:choose>
        <xsl:when test="./test[@status='ok']">green</xsl:when>
        <xsl:when test="./test[@status='errors']">red</xsl:when>
        <xsl:when test="./test">yellow</xsl:when>
        <xsl:otherwise>black</xsl:otherwise>
     </xsl:choose>
  </xsl:attribute>
  <xsl:choose>
    <xsl:when test="./test[@status='errors']">
       <xsl:value-of select="./test/@errors"/> errors,
       <xsl:value-of select="./test/@failures"/> failures (
       <xsl:value-of select="./test/@tests"/> tests)
    </xsl:when>
    <xsl:when test="./test[@status='ok']">ok (<xsl:value-of select="./test/@tests"/> tests)</xsl:when>
    <xsl:otherwise>none</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="status">
          <xsl:attribute name="bgcolor">
             <xsl:call-template name="status-color"/>
          </xsl:attribute>
          <xsl:call-template name="status-text"/>

</xsl:template>

<xsl:template name="status-color">
     <xsl:choose>
	<xsl:when test="@compile='error.depend'">yellow</xsl:when>
	<xsl:when test="@compile='error.compile'">red</xsl:when>
	<xsl:when test="@compile='ok'">green</xsl:when>
	<xsl:otherwise>white</xsl:otherwise>
     </xsl:choose>
</xsl:template>

<xsl:template name="status-text">
          <xsl:choose>
             <xsl:when test="@compile='error.depend'">Missing Dependencies: <xsl:value-of select="deperror/text()"/></xsl:when>
             <xsl:when test="@compile='error.compile'">Build Error:<br/><xsl:value-of select="builderror/text()"/></xsl:when>
             <xsl:when test="@compile='ok'">Success</xsl:when>
             <xsl:otherwise>UNKNOWN</xsl:otherwise>
          </xsl:choose>
</xsl:template>
</xsl:stylesheet>
