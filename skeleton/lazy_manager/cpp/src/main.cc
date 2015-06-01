#include <iostream>
#include <unistd.h>

#define log(_format, ...)   printf(__FILE__ ":%d|" _format "\n", __LINE__, ##__VA_ARGS__)


// TODO: should modify for unicode-characters

class Trie {
public:
  struct node {
  public:
    bool end;
    struct node* next[256];
  };

  typedef struct node node_t;

public:
  Trie() {
    memset(&data_, 0x00, sizeof(node_t));
  }

public:
  void add(const char *str) {
    node_t *next = &data_;
    for (unsigned char c = (unsigned char) *str;
         c != 0x00;
         ++str, c = (unsigned char) *str) {
      if (next->next[c] == nullptr) {
        next->next[c] = new node_t();
        memset(next->next[c], 0x00, sizeof(node_t));
      }
      next = next->next[c];
    }
    next->end = true;
  }

  bool contains(const char *str) {
    node_t *next = &data_;
    for (unsigned char c = (unsigned char) *str;
         c != 0x00;
         ++str, c = (unsigned char) *str) {
      if (next->next[c] == nullptr) {
        return false;
      }
      next = next->next[c];
    }
    return next->end;
  }

  // TODO: make it non-cursive
  void printAll() {
    char buf[2];
    printAllInternal(&data_, buf, sizeof(buf), 0);
  }

private:
  void printAllInternal(node_t *node, char *buf, int buf_len, int str_len) {
    if (node->end) {
      buf[str_len] = 0x00;
      log("[%s]", buf);
    }

    if (str_len >= buf_len-1) {
      log("no enough buffer");
      return;
    }

    for (unsigned int c = 0; c < 256; ++c) {
      if (node->next[c] != nullptr) {
        buf[str_len] = (char) c;
        printAllInternal(node->next[c], buf, buf_len, str_len+1);
      }
    }
  }

private:
  node_t data_;
};

int main(int argc, char *argv[])
{
  printf("Just a test.\n");

  Trie trie;

  char buf[2];

  memset(buf, 'A', sizeof(buf));
  buf[sizeof(buf)-1] = 0x00;
  log("now add");
  trie.add(buf);

  // sleep(20);

  //trie.add("");
  //trie.add("1");
  //trie.add("2");


  log("%d", trie.contains("Just a test."));

  trie.printAll();

  // sleep(20);

  return 0;
}

