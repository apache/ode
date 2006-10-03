<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml"/>
  <xsl:param name="middle"/>

  <xsl:template match="/">
    <!-- Per the WS-BPEL spec, only the single child of the root of the 
         result tree is considered (so the root element here is "suppressed"
         in the engine) -->
    <xsl:element name="root">
      <xsl:element name="hello">
        <xsl:value-of select="concat(*/text(), $middle/*/text(), ' World')"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>