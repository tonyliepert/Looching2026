# Makefile for jlooch applet by Brad Garton, using JSyn SDK for Linux
# JSyn Copyright 2001 Phil Burk, SoftSynth.com

# change the following line to reflect your own installation of java
JAVA_HOME=/usr/java/jdk1.3.1_01

JAVAC=$(JAVA_HOME)/bin/javac
JAVA=$(JAVA_HOME)/bin/java
JVCOM=$(JAVAC) -nowarn -classpath $(MYCLASSPATH)


# change the following line to reflect your own installation of JSyn
JSYNSDKDIR     = /usr/local/src/jsyn142a_linux386_sdk/

CLASSDIR       = $(JSYNSDKDIR)/classes
JSYNDIR        = $(JSYNSDKDIR)/jsyn
JSRCDIR        = $(JSYNSDKDIR)/jsrc
JSYNLIBDIR     = $(JSYNSDKDIR)/lib
MYCLASSPATH    = .:$(CLASSDIR):$(CLASSDIR)/JSynClasses.jar:$(CLASSDIR)/SoftSynthTools.jar

jlooch:
	$(JVCOM) jlooch.java BradUtils.java BradVerb1.java

go:
	$(JAVA) -Djava.library.path=$(JSYNLIBDIR) -classpath $(MYCLASSPATH) jlooch
