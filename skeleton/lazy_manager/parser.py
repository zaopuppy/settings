#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import codecs
import gzip
from functools import partial


# TODO: support `auto` encoding
def parse_file(file_name: str, delims='\n', encoding='utf-8', zip_type=None):
    """
    parse a file into pieces of strings
    :param file_name:
    :param delims:
    :param encoding:
    :param zip_type:
    :return: a iterator
    """
    try:
        decoder = codecs.getincrementaldecoder(encoding)()
    except LookupError as e:
        print(str(e))
        return

    read_buffer_size = 10*1024
    max_line_size = 100*1024

    if not zip_type:
        open_f = open
    elif zip_type == 'gz':
        open_f = gzip.open
    else:
        raise NotImplementedError()

    with open_f(file_name, 'rb') as fp:
        buffered_str = ''
        for bs in iter(partial(fp.read, read_buffer_size), b''):
            decoded_str = decoder.decode(bs)
            # FIXME: i don't think current implementation is efficient enough...
            for c in decoded_str:
                if c in delims:
                    if len(buffered_str) > 0:
                        yield buffered_str
                        buffered_str = ''
                else:
                    buffered_str += c
                    if len(buffered_str) >= max_line_size:
                        yield buffered_str
                        buffered_str = ''
        if len(buffered_str) > 0:
            yield buffered_str


def parse_str(text: str, delims='\n'):
    max_line_size = 100*1024
    buffered_str = ''
    for c in text:
        if c in delims:
            if len(buffered_str) > 0:
                yield buffered_str
                buffered_str = ''
        else:
            buffered_str += c
            if len(buffered_str) >= max_line_size:
                yield buffered_str
                buffered_str = ''
    if len(buffered_str) > 0:
        yield buffered_str


if __name__ == '__main__':
    # for line in parse_file('/Volumes/Data/Download/创生0-6.txt'):
    #     print(line)
    for line in parse_str('a\nb\n'):
        print(line)
