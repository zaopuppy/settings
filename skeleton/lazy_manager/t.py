#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import itertools
import functools


def read_file():
    print('begin')
    with open('/etc/hosts', 'rb') as fp:
        for bs in iter(functools.partial(fp.read, 1), b''):
            print('hi')
            yield bs.decode('utf-8')
    print('done')


if __name__ == '__main__':
    g = itertools.islice(read_file(), 3)
    for c in g:
        print(c)
    print(dir(g))
