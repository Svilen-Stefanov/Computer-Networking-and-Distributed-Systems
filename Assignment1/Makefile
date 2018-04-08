TARGET=fizzbuzz.jar
RUNFILE=fizzbuzz

CLASSES=Fizzbuzz.class
LFILES=$(addprefix -C src , $(CLASSES))
OBJECTS=$(addprefix src/, $(CLASSES))
SOURCES=$(OBJECTS:.class=.java)

all: $(TARGET) $(RUNFILE)

.PHONY: all clean fresh

$(TARGET): $(OBJECTS)
	jar cfm $(TARGET) deps/manifest.txt $(LFILES)

%.class: %.java
	javac $(SOURCES)

$(RUNFILE):
	cp deps/run $(RUNFILE)
	chmod +x $(RUNFILE)


clean:
	-rm -f $(OBJECTS)
	-rm -f $(TARGET)
	-rm -f $(RUNFILE)

fresh: clean all
