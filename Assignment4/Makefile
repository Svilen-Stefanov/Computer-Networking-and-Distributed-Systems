TARGET=router
JAR=$(TARGET).jar

CLASSES=Arguments.class Assignment4.class FileReader.class

#THIS LINE
CLASSES+=Entry.class

LFILES=$(addprefix -C src , $(CLASSES))
OBJECTS=$(addprefix src/, $(CLASSES))
SOURCES=$(OBJECTS:.class=.java)

all: $(JAR) $(TARGET)

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

clean:
	-$(MAKE) -C $(LIBRAWDIR) clean
	-rm -f $(OBJECTS)
	-rm -f $(TARGET)
	-rm -f $(JAR)

fresh: clean
	$(MAKE) all
