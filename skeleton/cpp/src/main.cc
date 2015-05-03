#include <iostream>

#define log(_format, ...)   printf(__FILE__ ":%d|" _format "\n", __LINE__, ##__VA_ARGS__)

int main(int argc, char *argv[])
{
  printf("Just a test.\n");
  return 0;
}

