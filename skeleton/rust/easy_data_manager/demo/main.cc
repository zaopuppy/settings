#include <stdio.h>

#define log(format, args...) printf(format "\n", ##args)

int main(int argc, char *argv[])
{
  log("just a test.");
  return 0;
}


