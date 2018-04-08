TARGET=ndisc
JAR=$(TARGET).jar

LIBSOCKET=deps/libGRNVS.so
LIBRAWDIR=deps/libraw/
LIBRAW=$(LIBRAWDIR)libraw.a
LDFLAGS=$(LIBRAW)
CFLAGS+=-I$(LIBRAWDIR)include -I/usr/lib/jvm/java-8-openjdk-amd64/include
CFLAGS+=-I/usr/lib/jvm/java-8-openjdk-amd64/include/linux -Ideps -fPIC
CFLAGS+=-fdiagnostics-color=always


CLASSES=Arguments.class GRNVS_RAW.class Assignment3.class Timeout.class
LFILES=$(addprefix -C src , $(CLASSES))
OBJECTS=$(addprefix src/, $(CLASSES))
SOURCES=$(OBJECTS:.class=.java)

LIBSOCKETOBJECTS= deps/GRNVS_RAW.o

all: $(JAR) $(LIBSOCKET) $(TARGET)

.PHONY: all clean fresh

$(JAR): $(OBJECTS)
	jar cfm $(JAR) deps/manifest.txt $(LFILES)

%.class: %.java
	javac -encoding UTF-8 $(SOURCES)

#sed replaces CRLF with just LF for windows + java users
$(TARGET):
	cp deps/run $(TARGET)
	sed 's/\r$$//' -i $(TARGET)
	chmod +x $(TARGET)

$(LIBSOCKET): $(LIBSOCKETOBJECTS) $(LIBRAW)
	$(CC) $(LIBSOCKETOBJECTS) $(LDFLAGS) -shared -o $(LIBSOCKET)

$(LIBRAW):
	$(MAKE) -C $(LIBRAWDIR)

clean:
	-$(MAKE) -C $(LIBRAWDIR) clean
	-rm -f $(OBJECTS)
	-rm -f $(TARGET)
	-rm -f $(LIBSOCKETOBJECTS)
	-rm -f $(LIBSOCKET)
	-rm -f $(JAR)

fresh: clean
	$(MAKE) all
