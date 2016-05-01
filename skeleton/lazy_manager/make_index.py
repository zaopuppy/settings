#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import os
import os.path

import toml


def make_index(config_file: str, index_type, **kwargs):
    config = toml.load(config_file)

    forward_name = config.get('forward-index')
    inverted_name = config.get('inverted-index')

    if forward_name is None or inverted_name is None or forward_name == inverted_name:
        raise Exception('bad configure item')

    index = index_type(config, kwargs)

    if os.path.isdir(index.index_name()) and index.is_valid():
        os.mkdir(index.index_name(), 0o755)
        index.load_index()
    else:
        index.create_index(config)


if __name__ == '__main__':
    pass
