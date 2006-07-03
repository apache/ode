<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- top level parameters -->
  <xsl:param name="title" select="'Build Results'"/>
  <xsl:param name="basedir" select="'..'"/>
  <xsl:param name="ia5dir" select="concat($basedir,'/ia5.tmp~')" />
  <xsl:param name="logdir" select="concat($basedir,'/logs~')" />
  <xsl:param name="installdir" select="concat($basedir,'/install~')" />

  <xsl:output method="html"/>

  <xsl:variable name="height" expr="5" />
  <xsl:variable name="LM" expr="80" />

  <xsl:variable name="total_projects" select="count(/buildresult/project)"/>
  <xsl:variable name="compile_error" select="/buildresult/project[@compile='error.compile']"/>
  <xsl:variable name="depend_error" select="/buildresult/project[@compile='error.depend']"/>
  <xsl:variable name="ok_warn" select="/buildresult/project[@compile='ok' and descendant::message[@priority='warning' or @priority='error']]"/>
  <xsl:variable name="ok_clean" select="/buildresult/project[@compile='ok' and not(descendant::message[@priority='warning' or @priority='error'])]"/>

  <!-- Total projects taht built and had errors or failures with their unit tests. -->
  <xsl:variable name="test_error_project" select="/buildresult/project[@compile='ok' and descendant::test[@status='errors']]" />
  <!-- Total projects that built and had OK unit tests. -->
  <xsl:variable name="test_ok_project" select="/buildresult/project[@compile='ok' and descendant::test[@status='ok' and @tests&gt;0]]" />
  <!-- Total projects that built but didn't have any units tests. -->
  <xsl:variable name="no_test_project" select="/buildresult/project[@compile='ok' and (not(descendant::test) or descendant::test[@tests=0])]" />

  <xsl:template match="/">
    <html>
      <head>
        <link href="primary.css" rel="stylesheet" type="text/css" />
        <title><xsl:value-of select="$title" /></title>
      </head>
      <body>
        <a name="#___TOP" />
        <xsl:call-template name="release-header" />
        <br /><hr /><br />
        <!-- build header view -->
        <xsl:call-template name="build-header"/>
        <br /><br />
        <xsl:call-template name="test-header" />
        <br /><hr /><br />
        <xsl:for-each select="buildresult/project">
          <xsl:sort select="@id" />
          <xsl:apply-templates select="."/>
        </xsl:for-each>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="release-header">
    <table width="100%" border="0" cellspacing="1" cellpadding="0" bgcolor="#C0C0C0">
      <tr>
        <td>
          <table width="100%" border="0" cellpadding="3" cellspacing="1">
            <tr>
              <td bgcolor="#E0E0E0"><b><font size="+2">Release Status: <xsl:value-of select="$title" /></font></b></td>
            </tr>
            <tr>
              <td bgcolor="#FFFFFF">
                <table width="100%" border="0">
                  <tr>
                    <td align="center">
                      <table width="95%" border="0" cellspacing="3" cellpadding="0">
                        <tr>
                          <td><b>WORKSPACE ID: &quot;<font color="#666666"><xsl:value-of select="buildresult/workspace/@id" /></font>&quot;</b></td>
                        </tr>
                        <tr>
                          <td><b>BUILD STATUS:</b><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><a href="#build">[detail]</a></td>
                        </tr>
                        <tr>
                          <td><table width="100%" border="0" cellpadding="3"><tr>
                          <xsl:call-template name="bar">
                            <xsl:with-param name="total" select="1" />
                            <xsl:with-param name="this" select="1" />
                            <xsl:with-param name="color">
                              <xsl:choose>
                                <xsl:when test="count($compile_error) &gt; 0">#CC3333</xsl:when>
                                <xsl:when test="count($depend_error) &gt; 0">#CCCC33</xsl:when>
                                <xsl:when test="count($ok_warn) &gt; 0">#006600</xsl:when>
                                <xsl:otherwise>#33CC33</xsl:otherwise>
                              </xsl:choose>
                            </xsl:with-param>
                          </xsl:call-template></tr></table></td>
                        </tr>
                        <tr>
                          <td><b>UNIT TEST STATUS:</b><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><a href="#test">[detail]</a></td>
                        </tr>
                        <tr>
                          <td><table width="100%" border="0" cellpadding="3"><tr>
                          <xsl:call-template name="bar">
                            <xsl:with-param name="total" select="1" />
                            <xsl:with-param name="this" select="1" />
                            <xsl:with-param name="color">
                              <xsl:choose>
                                <xsl:when test="/buildresult/project[not(@compile='ok')]">#660000</xsl:when>
                                <xsl:when test="count($test_error_project) &gt; 0">#CC3333</xsl:when>
                                <xsl:when test="count($no_test_project) &gt; 0">#CCCC33</xsl:when>
                                <xsl:otherwise>#33CC33</xsl:otherwise>
                              </xsl:choose>
                            </xsl:with-param>
                          </xsl:call-template>
                        </tr></table></td></tr>
                        <tr>
                          <td><b>BUILD RESULTS:</b><br />
                            <ul>
                              <li>Build Log <a href="{$basedir}/buildlog.txt">[view]</a></li>
                              <li>Installers <a href="{$ia5dir}/Web_Installers/install.htm">[view]</a></li>
                              <li>JAR files <a href="{$installdir}">[view]</a></li>
                            </ul>
                          </td>
                        </tr>
                        <tr>
                          <td><b>PROJECTS:</b></td>
                        </tr>
                        <tr>
                          <td><xsl:call-template name="project-link-list">
                            <xsl:with-param name="nodes" select="buildresult/project" />
                          </xsl:call-template></td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </xsl:template>


  <xsl:template name="build-header">
    <a name="build"></a>
    <table width="100%" border="0" cellspacing="1" cellpadding="0" bgcolor="#C0C0C0">
      <tr>
        <td>
          <table width="100%" border="0" cellpadding="3" cellspacing="1">
            <tr>
              <td bgcolor="#E0E0E0"><b><font size="+1">Build Status</font></b></td>
            </tr>
            <tr>
              <td bgcolor="#FFFFFF">
                <table width="100%" border="0">
                  <tr>
                    <td align="center">
                      <xsl:call-template name="build-project-bar" />
                      <table width="95%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                          <td colspan="{$total_projects}">
                            <table width="100%" border="0">
                              <tr>
                                <td align="left">
                                  <b>TOTAL PROJECTS:</b>
                                </td>
                                <td align="right" bgcolor="E0E0E0">
                                  <b>
                                    <xsl:value-of select="$total_projects"/>
                                  </b>
                                </td>
                              </tr>
                              <xsl:call-template name="component-display">
                                <xsl:with-param name="title" select="'Projects&amp;nbsp;with&amp;nbsp;Compilation&amp;nbsp;Errors:'"/>
                                <xsl:with-param name="bgcolor" select="'#CC3333'" />
                                <xsl:with-param name="fgcolor" select="'#FFFFFF'" />
                                <xsl:with-param name="nodes" select="$compile_error" />
                              </xsl:call-template>
                              <xsl:call-template name="component-display">
                                <xsl:with-param name="title" select="'Projects&amp;nbsp;with&amp;nbsp;Missing&amp;nbsp;Dependencies:'"/>
                                <xsl:with-param name="bgcolor" select="'#CCCC33'" />
                                <xsl:with-param name="fgcolor" select="'#FFFFFF'" />
                                <xsl:with-param name="nodes" select="$depend_error" />
                              </xsl:call-template>
                              <xsl:call-template name="component-display">
                                <xsl:with-param name="title" select="'Projects&amp;nbsp;with&amp;nbsp;Warnings:'"/>
                                <xsl:with-param name="bgcolor" select="'#006600'" />
                                <xsl:with-param name="fgcolor" select="'#FFFFFF'" />
                                <xsl:with-param name="nodes" select="$ok_warn" />
                              </xsl:call-template>
                              <xsl:call-template name="component-display">
                                <xsl:with-param name="title" select="'Projects&amp;nbsp;with&amp;nbsp;Warnings:'"/>
                                <xsl:with-param name="bgcolor" select="'#33CC33'" />
                                <xsl:with-param name="fgcolor" select="'#FFFFFF'" />
                                <xsl:with-param name="nodes" select="$ok_clean" />
                              </xsl:call-template>
                            </table>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template name="project-link-list">
    <xsl:param name="nodes" />
    <xsl:for-each select="$nodes">
      <xsl:sort select="@module" />
      <a href="#{@module}">[<xsl:value-of select="@module" />]</a>
      <xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp; </xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="test-header">
    <a name="test"></a>
    <table width="100%" border="0" cellspacing="1" cellpadding="0" bgcolor="#C0C0C0">
      <tr>
        <td>
          <table width="100%" border="0" cellpadding="3" cellspacing="1">
            <tr>
              <td bgcolor="#E0E0E0"><b><font size="+1">Unit Test Status</font></b></td>
            </tr>
            <tr>
              <td bgcolor="#FFFFFF">
                <table width="100%" border="0">
                  <tr>
                    <td align="center">
                      <xsl:call-template name="test-project-bar" />
                      <table width="95%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                          <td>
                            <table width="100%" border="0">
                              <tr>
                                <td align="left">
                                  <b>TOTAL PROJECTS:</b>
                                </td>
                                <td align="right" bgcolor="E0E0E0">
                                  <b>
                                    <xsl:value-of select="$total_projects"/>
                                  </b>
                                </td>
                              </tr>
                              <xsl:call-template name="component-display">
                                <xsl:with-param name="title" select="'Unbuilt&amp;nbsp;Projects:'"/>
                                <xsl:with-param name="bgcolor" select="'#660000'" />
                                <xsl:with-param name="fgcolor" select="'#FFFFFF'" />
                                <xsl:with-param name="nodes" select="/buildresult/project[not(@compile='ok')]" />
                              </xsl:call-template>
                              <xsl:call-template name="component-display">
                                <xsl:with-param name="title" select="'Projects&amp;nbsp;with&amp;nbsp;Test&amp;nbsp;Errors&amp;nbsp;or&amp;nbsp;Failures:'"/>
                                <xsl:with-param name="bgcolor" select="'#CC3333'" />
                                <xsl:with-param name="fgcolor" select="'#FFFFFF'" />
                                <xsl:with-param name="nodes" select="/buildresult/project[@compile='ok' and descendant::test[@status='errors']]" />
                              </xsl:call-template>
                              <xsl:call-template name="component-display">
                                <xsl:with-param name="title" select="'Projects&amp;nbsp;without&amp;nbsp;Tests:'"/>
                                <xsl:with-param name="bgcolor" select="'#CCCC33'" />
                                <xsl:with-param name="fgcolor" select="'#FFFFFF'" />
                                <xsl:with-param name="nodes" select="$no_test_project" />
                              </xsl:call-template>
                              <xsl:call-template name="component-display">
                                <xsl:with-param name="title" select="'Projects&amp;nbsp;with&amp;nbsp;All&amp;nbsp;Tests&amp;nbsp;Passed:'"/>
                                <xsl:with-param name="bgcolor" select="'#33CC33'" />
                                <xsl:with-param name="fgcolor" select="'#FFFFFF'" />
                                <xsl:with-param name="nodes" select="$test_ok_project" />
                              </xsl:call-template>
                            </table>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template name="project-list">
    <xsl:param name="state" />                              
    <tr>
      <td colspan="2">
        <xsl:for-each select="/buildresult/project[@compile=$state]">
          <xsl:sort select="@module" />
          <a href="#{@module}">[<xsl:value-of select="@module" />]</a>
          <xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp; </xsl:text>
        </xsl:for-each>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="project">
  	<a name="{@id}"></a>
    <table width="100%" border="0" cellspacing="1" cellpadding="0" bgcolor="#C0C0C0">
      <tr>
        <td>
          <table width="100%" border="0" cellpadding="3" cellspacing="1">
            <tr>
              <td bgcolor="#E0E0E0">
                <table width="100%" cellspacing="0px" cellpadding="0px" border="0">
                <tr>
                  <td><b><xsl:value-of select="@id" /></b></td>
                  <td align="right"><a href="#___TOP"><xsl:text disable-output-escaping="yes">&amp;</xsl:text>laquo;TOP</a></td>
                </tr>
              </table></td>
            </tr>
            <tr>
              <td bgcolor="#FFFFFF">
                <table width="100%" border="0">
                  <xsl:call-template name="build" />
                  <xsl:call-template name="tests" />
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <br />
    <br />
  </xsl:template>

  <xsl:template name="trunc">
    <xsl:param name="text" />
    <xsl:value-of select="substring($text,1,$LM)" />
    <xsl:if test="string-length($text) &gt; $LM">...</xsl:if>
  </xsl:template>

  <xsl:template name="build">
    <tr>
      <td align="center">
        <table width="95%" border="0" cellspacing="3" cellpadding="2">
          <tr>
            <td><b>Build Status:</b><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><xsl:choose>
              <xsl:when test="@compile='ok' and count(descendant::message[@priority='error' or @priority='warning'])&gt;0"><b><font color="#006600">COMPILED WITH WARNINGS</font></b></xsl:when>
              <xsl:when test="@compile='ok'"><b><font color="#33CC33">COMPILED WITHOUT WARNINGS</font></b></xsl:when>
              <xsl:when test="@compile='error.depend'"><b><font color="#CCCC33">FAILED DUE TO MISSING DEPENDENCIES</font></b></xsl:when>
              <xsl:when test="@compile='error.compile'"><b><font color="#CC3333">FAILED DUE TO ERRORS</font></b></xsl:when>
            </xsl:choose><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><a href="{$logdir}/{@module}.xml">[view log]</a></td>
          </tr>
          <xsl:if test="descendant::message[@priority='warning' or @priority='error']">
            <tr>
              <td><b>Messages:</b></td>
            </tr>
            <tr>
              <td align="right"><table width="95%" cellspacing="3" cellpadding="2">
            <xsl:for-each select="descendant::task[message[@priority='warning' or @priority='error']]">
              <tr><td>
                <font size="-1"><b>Target:</b><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><xsl:value-of select="../@name" /></font><br />
                <font size="-1"><b>Task:</b><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><xsl:value-of select="@name" /></font><br />
                <pre class="message"><xsl:for-each select="message[@priority='warning' or @priority='error']">
                    <xsl:choose>
                      <xsl:when test="contains(text(),'work~')"><xsl:call-template name="trunc">
                        <xsl:with-param name="text"><xsl:value-of select="substring-before(substring-after(text(),'work~'),' ')" /></xsl:with-param>
                      </xsl:call-template><xsl:text>
</xsl:text><xsl:call-template name="trunc">
                        <xsl:with-param name="text"><xsl:value-of select="substring-after(substring-after(text(),'work~'),' ')" /></xsl:with-param></xsl:call-template></xsl:when>
                      <xsl:otherwise>
                        <xsl:choose>
                          <xsl:when test="string-length(text()) &gt; $LM"><xsl:value-of select="substring(text(),1,$LM)" />...</xsl:when>
                          <xsl:otherwise><xsl:value-of select="text()" /></xsl:otherwise>
                        </xsl:choose>
                      </xsl:otherwise>
                    </xsl:choose>
                  <xsl:if test="not(position()=last())"><xsl:text disable-output-escaping="yes">
</xsl:text></xsl:if>
              </xsl:for-each></pre></td></tr>
            </xsl:for-each>
            </table></td></tr>
          </xsl:if>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="tests">
    <tr>
      <td align="center">
        <table width="95%" border="0" cellspacing="3" cellpadding="2">
          <tr>
            <td><b>Unit Test Status:</b><xsl:text> </xsl:text>
              <xsl:choose>
                <xsl:when test="starts-with(@compile,'error')"><b><font color="#660000">PROJECT NOT BUILT.</font></b></xsl:when>
                <xsl:when test="test[@tests&gt;0]"><b><font color="#660000"><xsl:value-of select="number(test/@errors)" /> error<xsl:if test="not(number(test/@errors) = 1)">s</xsl:if></font> / <font color="#CC3333"><xsl:value-of select="number(test/@failures)" /> failure<xsl:if test="not(number(test/@failure) = 1)">s</xsl:if></font> / <font color="#33CC33"><xsl:value-of select="number(test/@tests) - number(test/@errors) - number(test/@failures)" /> OK</font> / <xsl:value-of select="test/@tests" /> total</b><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><a href="{$logdir}/{@module}/junit-noframes.html">[view report]</a></xsl:when>
                <xsl:when test="test[@tests=0] or not(test)"><b><font color="#CCCC33">NO UNIT TESTS</font></b></xsl:when>
              </xsl:choose></td>
          </tr>
        </table>
      </td>
    </tr>
    <tr>
      <td align="center">
        <table width="95%" border="0" cellspacing="3" cellpadding="2">
          <tr>
            <xsl:choose>
              <xsl:when test="@compile='ok' and test/@tests &gt; 0">
                <xsl:call-template name="bar">
                  <xsl:with-param name="total" select="number(test/@tests)" />
                  <xsl:with-param name="this" select="number(test/@errors)" />
                  <xsl:with-param name="color" select="'#660000'" />
                </xsl:call-template>
                <xsl:call-template name="bar">
                  <xsl:with-param name="total" select="number(test/@tests)" />
                  <xsl:with-param name="this" select="number(test/@failures)" />
                  <xsl:with-param name="color" select="'#CC3333'" />
                </xsl:call-template>
                <xsl:call-template name="bar">
                  <xsl:with-param name="total" select="number(test/@tests)" />
                  <xsl:with-param name="this" select="number(test/@tests) - number(test/@errors) - number(test/@failures)" />
                  <xsl:with-param name="color" select="'#33CC33'" />
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="@compile='ok'">
                <xsl:call-template name="bar">
                  <xsl:with-param name="total" select="1" />
                  <xsl:with-param name="this" select="1" />
                  <xsl:with-param name="color" select="'#CCCC33'" />
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="not(@compile='ok')">\
                <xsl:call-template name="bar">
                  <xsl:with-param name="total" select="1" />
                  <xsl:with-param name="this" select="1" />
                  <xsl:with-param name="color" select="'#999999'" />
                </xsl:call-template>
              </xsl:when>
            </xsl:choose>
          </tr>  
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="deperror">
    <tr>
      <td width="10%"><b>Missing<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>Dependencies: </b></td>
      <td><xsl:call-template name="unravel-and-link">
        <xsl:with-param name="text" select="concat(normalize-space(text()),' ')" />
      </xsl:call-template></td>
    </tr>
  </xsl:template>

  <xsl:template match="task">
    <tr>
      <td valign="top"><b>Messages:</b></td>
      <td>
        <xsl:if test="message[@priority='error']">
          <pre class="error"><xsl:for-each select="message[@priority='error']"><xsl:value-of select="text()" /><xsl:text>
</xsl:text></xsl:for-each></pre>
        </xsl:if>
        <xsl:if test="message[@priority='warning']">
          <pre class="warning"><xsl:for-each select="message[@priority='warning']"><xsl:value-of select="text()" /><xsl:text>
</xsl:text></xsl:for-each></pre>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="message">
    <pre class="{@priority}"><xsl:value-of select="text()" /></pre>
  </xsl:template>

  <xsl:template name="unravel-and-link">
    <xsl:param name="text" />
    <a href="#{substring-before($text,' ')}">[<xsl:value-of select="substring-before($text,' ')" />]</a>
    <xsl:text disable-output-escaping="yes">&amp;nbsp;&amp;nbsp; </xsl:text>
    <xsl:if test="contains($text,' ') and string-length(translate(substring-after($text,' '),' ',''))&gt;0">
      <xsl:call-template name="unravel-and-link">
        <xsl:with-param name="text" select="substring-after($text,' ')" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="bar">
    <xsl:param name="total" />
    <xsl:param name="this" />
    <xsl:param name="color" />
    <xsl:if test="$this &gt; 0">
      <td
          width="{round(100*($this div $total))}%" bgcolor="{$color}">
        <img src="one_pixel.gif" height="{$height}px"/>
      </td>
    </xsl:if>
  </xsl:template>

  <xsl:template name="test-project-bar">
    <table width="95%" border="0" cellspacing="2" cellpadding="3">
      <tr>
        <xsl:call-template name="bar">
          <xsl:with-param name="total" select="$total_projects" />
          <xsl:with-param name="this" select="count($compile_error) + count($depend_error)" />
          <xsl:with-param name="color" select="'#660000'" />
        </xsl:call-template>
        <xsl:call-template name="bar">
          <xsl:with-param name="total" select="$total_projects" />
          <xsl:with-param name="this" select="count($test_error_project)" />
          <xsl:with-param name="color" select="'#CC3333'" />
        </xsl:call-template>
        <xsl:call-template name="bar">
          <xsl:with-param name="total" select="$total_projects" />
          <xsl:with-param name="this" select="count($no_test_project)" />
          <xsl:with-param name="color" select="'#CCCC33'" />
        </xsl:call-template>
        <xsl:call-template name="bar">
          <xsl:with-param name="total" select="$total_projects" />
          <xsl:with-param name="this" select="count($test_ok_project)" />
          <xsl:with-param name="color" select="'#33CC33'" />
        </xsl:call-template>
      </tr>
    </table>
  </xsl:template>
  
  <xsl:template name="build-project-bar">
    <table width="95%" border="0" cellspacing="2" cellpadding="3">
      <tr>
        <xsl:call-template name="bar">
          <xsl:with-param name="total" select="$total_projects" />
          <xsl:with-param name="this" select="count($compile_error)" />
          <xsl:with-param name="color" select="'#CC3333'" />
        </xsl:call-template>
        <xsl:call-template name="bar">
          <xsl:with-param name="total" select="$total_projects" />
          <xsl:with-param name="this" select="count($depend_error)" />
          <xsl:with-param name="color" select="'#CCCC33'" />
        </xsl:call-template>
        <xsl:call-template name="bar">
          <xsl:with-param name="total" select="$total_projects" />
          <xsl:with-param name="this" select="count($ok_warn)" />
          <xsl:with-param name="color" select="'#006600'" />
        </xsl:call-template>
        <xsl:call-template name="bar">
          <xsl:with-param name="total" select="$total_projects" />
          <xsl:with-param name="this" select="count($ok_clean)" />
          <xsl:with-param name="color" select="'#33CC33'" />
        </xsl:call-template>
      </tr>
    </table>
  </xsl:template>

  <xsl:template name="component-display">
    <xsl:param name="fgcolor" />
    <xsl:param name="bgcolor" />
    <xsl:param name="title" />
    <xsl:param name="nodes" />

    <tr>
      <td align="left">
        <b><xsl:value-of disable-output-escaping="yes" select="$title" /></b><xsl:text disable-output-escaping="yes">&amp;nbsp;(</xsl:text><xsl:value-of disable-output-escaping="yes" select="round(100*(count($nodes) div $total_projects))"/>%)</td>
      <td width="{$height}px" bgcolor="{$bgcolor}" align="right">
        <b>
          <font color="{$fgcolor}">
            <xsl:value-of select="count($nodes)"/>
          </font>
        </b>
      </td>
    </tr>
    <xsl:if test="count($nodes) &gt; 0">
      <tr>
        <td colspan="2">
          <xsl:call-template name="project-link-list">
            <xsl:with-param name="nodes" select="$nodes" />
          </xsl:call-template>
        </td>
      </tr>
    </xsl:if>

  </xsl:template>

</xsl:stylesheet>
