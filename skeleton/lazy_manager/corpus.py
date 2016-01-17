#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os.path
import toml


class Corpus:
    """
    pass
    """
    @classmethod
    def load_from_file(cls, config_file):
        return cls.load(toml.load(config_file))

    @classmethod
    def load(cls, config: dict):
        type = config.get('corpus-type')
        prefix = config.get('prefix')
        dataset = config.get('dataset')

        if type is None or prefix is None or dataset is None:
            raise Exception('missing configure item')

        encoding = 'utf-8' if not config.get('encoding') else config.get('encoding')

        if type == 'file-corpus':
            file_list = config.get('list')
            if not file_list:
                raise Exception('missing `list`')
            file_name = os.path.join(prefix, dataset, file_list+'-full-corpus.txt')
            return FileCorpus(os.path.join(prefix, dataset), file_name, encoding)
        elif type == 'line-corpus':
            file_name = os.path.join(prefix, dataset, dataset+'.dat')
            lines = config.get('num-lines', 0)
            return LineCorpus(file_name, encoding, lines)
        elif type == 'gz-corpus':
            file_name = os.path.join(prefix, dataset, dataset+'.dat')
            return GzCorpus(file_name, encoding)
        else:
            raise Exception('corpus type was not able to be determined')

    def __init__(self):
        pass

    def has_next(self) -> bool:
        return



if __name__ == '__main__':
    pass

