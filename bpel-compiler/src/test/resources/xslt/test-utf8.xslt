<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema"
	xmlns:tns="http://example.org/test"
	>
	<xsl:output method="xml" indent="no" />

	<xsl:template match="/">
		<tns:testxslt>
			<tns:from-xslt>
				<xsl:value-of select="'Prova lettere accentate: à è ì ò ù'"/>
			</tns:from-xslt>
			<tns:from-document>
				<xsl:value-of select="document('test.xml')"/>
			</tns:from-document>
		</tns:testxslt>
	</xsl:template>

	<xsl:template match="text()|@*"/>

</xsl:stylesheet>
