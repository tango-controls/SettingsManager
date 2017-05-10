#=============================================================================
#
# file :        Makefile
#
# description : Makefile to generate a TANGO device server.
#
# project :     SettingsManager
#
# $Author:  $
#
# $Revision:  $
# $Date:  $
#
#=============================================================================
#                This file is generated by POGO
#        (Program Obviously used to Generate tango Object)
#=============================================================================
#
#

#=============================================================================
#
CLASS	   = SettingsManager
PACKAGE = org.tango.settingsmanager
SOURCE_FILES = src/main/java/org/tango/settingsmanager
MAJOR_VERS   = 1
MINOR_VERS   = 9
RELEASE      = $(MAJOR_VERS).$(MINOR_VERS)

#=============================================================================
# Generate documentation
#
DOC_DIR    = ./doc/classes
DOC_HEADER = "$(CLASS) API documention"
OVERVIEW   = ./overview.html
documentation:
	javadoc 				\
	-version -author		\
	-public					\
	-windowtitle "$(CLASS) API" \
	-header $(DOC_HEADER)	\
	-d      $(DOC_DIR)		\
	-link  .				\
	-group "SettingsManager client classes"    "org.tango.settingsmanager.client"   \
	-group "SettingsManager server classes"    "org.tango.settingsmanager"   \
	\
	$(SOURCE_FILES)/client/SettingsManagerClient.java \
	$(SOURCE_FILES)/client/SettingsManagedEvent.java \
	$(SOURCE_FILES)/client/SettingsManagedListener.java \
	\
	$(SOURCE_FILES)/SettingsManager.java




TANGO_JAVA=$(TANGO_HOME)/release/java
CLP=$(TANGO_JAVA)/appli/org.tango.pogo.jar
UPDATE_CLASS =  org.tango.pogo.pogo_gui.tools.UpdateRelease
version:
	@echo "-----------------------------------------"
	@echo "	Patching Version"
	@echo "-----------------------------------------"
	echo "Updating date and revision number..."
	java -cp $(CLP) $(UPDATE_CLASS) \
		-file $(SOURCE_FILES)/commons/ICommons.java \
		-release $(RELEASE) \
		-title   "$(CLASS) Release Notes" \
		-note_path $(SOURCE_FILES)/commons \
		-package $(PACKAGE).commons
		

JTANGO = $(TANGO_HOME)/release/java/lib/JTango.jar
run:
	java -cp target/classes/:$(JTANGO) $(PACKAGE).$(CLASS) test
