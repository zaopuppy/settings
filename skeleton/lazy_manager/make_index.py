#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import toml


def make_index(config_file: str, index_type, **kwargs):
    config = toml.load(config_file)

    forward_name = config.get('forward-index')
    inverted_name = config.get('inverted-index')

    if forward_name is None or inverted_name is None or forward_name == inverted_name:
        raise Exception('missing configure item')

    index = index_type(config, kwargs)

    if index.is_valid():
        index.load_index()
    else:
        index.create_index(config)


if __name__ == '__main__':
    pass
