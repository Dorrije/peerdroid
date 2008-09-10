#
# $Id: Makefile,v 1.1 2005/06/07 22:54:58 hamada Exp $
#


# if need to use a particular JDK set JAVA_HOME in your env.
# if you refrence additional libraries they need to be set in the
# CLASSPATH
# 

ifneq ($(JAVA_HOME),)
 JAVAHOMEBIN      = $(JAVA_HOME)/bin/
else
 JAVAHOMEBIN      =
endif

JAVA          = $(JAVAHOMEBIN)java
JAVAC         = $(JAVAHOMEBIN)javac
JAR           = $(JAVAHOMEBIN)jar
CP	      = cp -f
TOP           = $(shell pwd)
CLASSDIR      = $(TOP)/classes

ifeq ($(PLATFORM),)
 JXME = $(TOP)/../..
 JXMECLASSES = $(JXME)/classes_cdc
 JXMEDIST = $(JXME)/dist
 JXMELIB = $(JXMEDIST)/jxme-cdc.jar:$(JXME)/lib/log4j.jar:
endif

ifneq ($(CLASSPATH),)
 JXTACLASSPATH=$(CLASSPATH):$(CLASSDIR):$(JXMELIB):
else
 JXTACLASSPATH=$(CLASSDIR):$(JXMELIB):
endif


ifeq (true,$(OPTIMIZE))
 JAVACOPT=-O -g:none
else
 JAVACOPT=
endif

.PHONY: compile clobber help

compile: application 

application:
	@echo building Demos using $(JAVAC)
	@echo CLASSPATH = $(JXTACLASSPATH)
	@if [ '!' -d $(CLASSDIR) ]; then mkdir $(CLASSDIR); fi;
	@$(JAVAC) $(JAVACOPT) -d $(CLASSDIR) -deprecation -classpath $(JXTACLASSPATH) *.java
	@echo Done building

run: 
	$(JAVA) -classpath $(JXMELIB):$(CLASSDIR) ChatDemo
rung:
	$(JAVA) -classpath $(JXMELIB):$(CLASSDIR) GroupDemo
clean:
	@rm -fr $(CLASSDIR)

clobber: clean

help:
	@echo "# Usage : gnumake [all] [compile] [run] [clean] [clobber] [help]"
