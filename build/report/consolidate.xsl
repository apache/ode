<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output indent="yes"/>

<xsl:template match="workspace">
  <buildresult>

  <xsl:copy-of select="."/>

  <xsl:for-each select="project">
     <xsl:variable name="log" select="document(concat('logs~/',@id,'.xml'),.)"/>
     <xsl:variable name="test" select="document(concat('logs~/',@id,'/TESTS-TestSuites.xml'),.)"/>
     <project>
       <xsl:for-each select="@*">
          <xsl:copy/>
       </xsl:for-each>

       <xsl:attribute name="compile">
          <xsl:choose>
             <xsl:when test="$log/build[contains(@error,'@@DEPFAILURE')]">error.depend</xsl:when>
             <xsl:when test="$log/build[@error]">error.compile</xsl:when>
             <xsl:when test="$log/build">ok</xsl:when>
             <xsl:otherwise>error.int</xsl:otherwise>
          </xsl:choose>
       </xsl:attribute>
       <xsl:choose>
         <xsl:when test="$log/build[contains(@error,'@@DEPFAILURE@@')]">
            <deperror><xsl:value-of select="substring-after($log/build/@error,'@@DEPFAILURE@@=')"/></deperror>
         </xsl:when>
         <xsl:when test="$log/build[@error]">
            <builderror><xsl:value-of select="$log/build/@error"/></builderror>
         </xsl:when>
       </xsl:choose>

       <xsl:if test="$test">
          <xsl:variable name="tests" select="sum($test/testsuites/testsuite/@tests)"/>
          <xsl:variable name="terrs" select="sum($test/testsuites/testsuite/@errors)"/>
          <xsl:variable name="tfail" select="sum($test/testsuites/testsuite/@failures)"/>
          <xsl:variable name="tferr" select="$terrs+$tfail"/>
          
          <test>
          <xsl:attribute name="status">
            <xsl:choose>
               <xsl:when test="$tferr=0">ok</xsl:when>
               <xsl:otherwise>errors</xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:attribute name="tests"><xsl:value-of select="$tests"/></xsl:attribute>
          <xsl:attribute name="errors"><xsl:value-of select="$terrs"/></xsl:attribute>
          <xsl:attribute name="failures"><xsl:value-of select="$tfail"/></xsl:attribute>
          </test>
       </xsl:if>


       <xsl:for-each select="$log/build">
          <xsl:call-template name="copyfilter"/>
       </xsl:for-each>
     </project>
  </xsl:for-each>
  </buildresult>
</xsl:template>

<xsl:template name="copyfilter">
    <xsl:for-each select="task|target">
      <xsl:copy>
        <xsl:for-each select="@*">
          <xsl:copy/>
        </xsl:for-each>
        <xsl:call-template name="copyfilter"/>
      </xsl:copy>
    </xsl:for-each>

    <xsl:for-each select="message[@priority!='debug' and not(starts-with(./text(),'Overriding previous'))]"> 
       <xsl:copy-of select="."/>
    </xsl:for-each>

</xsl:template>

</xsl:stylesheet>
