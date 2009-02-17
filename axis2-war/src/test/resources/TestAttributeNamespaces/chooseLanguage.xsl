<xsl:stylesheet version="2.0"
  xmlns:inspection="http://schemas.xmlsoap.org/ws/2001/10/inspection/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema">

  <xsl:output omit-xml-declaration="yes"/>

  <xsl:template match="/">
    <xsl:value-of select="document('language.xml')//languages/language [@name='french']/text()"/>   
  </xsl:template>  
</xsl:stylesheet>