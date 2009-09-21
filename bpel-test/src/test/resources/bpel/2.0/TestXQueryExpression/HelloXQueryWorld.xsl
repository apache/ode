<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.example.org/ProcessData"
	xmlns:tns="http://www.example.org/ProcessData"
	version="1.0">
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/">
    <root>
       <xsl:text>Hello World</xsl:text>
    </root>
  </xsl:template>

</xsl:stylesheet>