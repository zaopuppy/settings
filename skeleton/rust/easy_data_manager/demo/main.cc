#include <iostream>
#include <boost/filesystem.hpp>

#define log(format, args...) printf(format "\n", ##args)

using namespace boost::filesystem;

int walk(const path &check_path)
{
  if (check_path.generic_string().compare(0, 5, "/proc") == 0) {
    log("/proc system");
    return -1;
  }

  if (is_regular_file(check_path)) {
    // log("file size: %lu", file_size(check_path));
    return -1;
  }

  if (!is_directory(check_path)) {
    log("not a regular file or directory");
    return -1;
  }

  // log("directory");

  std::deque<path> dir_checklist;

  dir_checklist.push_back(check_path);


  path cur_path;
  while (dir_checklist.size() > 0) {

    cur_path = dir_checklist.front();

    std::deque<path> file_in_dir;

    try {
      copy(
          directory_iterator(cur_path),
          directory_iterator(),
          back_inserter(file_in_dir));

      for (const path &p: file_in_dir) {
        // log("file: [%s]", p.generic_string().c_str());
        if (is_regular(p)) {
          // log("file size: %lu", file_size(p));
          continue;
        }

        if (is_directory(p)) {
          dir_checklist.push_back(p);
          continue;
        }
      }
    } catch (const filesystem_error &e) {
      // log("exception: [%s]", e.what());
      // ignore
    }

    dir_checklist.pop_front();
  }

  return 0;
}

int main(int argc, char *argv[])
{
  if (argc < 2) {
    log("bad usage");
    return -1;
  }
  path p(argv[1]);
  walk(p);
  return 0;
}


