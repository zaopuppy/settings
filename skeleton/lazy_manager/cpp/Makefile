
CPP := gcc
CXX := g++
CC  := gcc
AR  := ar
LD  := ld
LEX := flex
YACC := bison

# vpath %.c src1:src2:...
# vpath %.c src
# vpath %.cc src
# vpath % src

SRCDIR := src

CFILES := $(wildcard $(SRCDIR)/*.c)
CXXFILES := $(wildcard $(SRCDIR)/*.cc)

OBJFILES := $(CFILES:%.c=%.o) $(CXXFILES:%.cc=%.o)
DEPFILES := $(OBJFILES:%.o=%.d)

CFLAGS := -g -fPIC -Wall
CXXFLAGS := $(CFLAGS)
LDFLAGS :=

TARGET := ts

$(TARGET): $(OBJFILES)
	$(CXX) -o $@ $^ $(LDFLAGS)

%.d: %.c
	$(CC) $(CFLAGS) -MT $(@:%.d=%.o) -MM -o $@ $<

%.d: %.cc
	$(CXX) $(CFLAGS) -MT $(@:%.d=%.o) -MM -o $@ $<

%.o: %.c
	$(CC) $(CFLAGS) -c -o $@ $<

%.o: %.cc
	$(CXX) $(CFLAGS) -c -o $@ $<


include $(DEPFILES)

.PHONY: clean

clean:
	rm -f $(OBJFILES) $(DEPFILES) $(TARGET)


# END

