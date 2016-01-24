#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os.path
import itertools

import toml

from parser import parse_file
from document import Document


def corpus_file(prefix: str, doc_list: str, encoding='utf-8'):
    for doc_id, line in zip(itertools.count(0), parse_file(doc_list, encoding=encoding)):
        line = line.strip()
        if len(line) <= 0 or line.startswith('#'):
            continue
        idx = line.index(' ')
        class_label, file_name = line[:idx], line[idx+1:]
        yield Document(os.path.join(prefix, file_name), doc_id, class_label)


# TODO: parameter `lines` has not been supported yet, for potential resource leakage.
def corpus_line(file_path: str, encoding='utf-8', zip_type=None):
    suffix = '' if not zip_type else '.' + zip_type
    # class label
    class_label_file = os.path.join(file_path, '.labels' + suffix)
    if os.path.isfile(class_label_file):
        class_label_iter = parse_file(class_label_file, encoding=encoding, zip_type=zip_type)
    else:
        class_label_iter = itertools.repeat('[none]')
    # document name
    doc_name_file = os.path.join(file_path, '.names' + suffix)
    if os.path.isfile(doc_name_file):
        doc_name_iter = parse_file(doc_name_file, encoding=encoding, zip_type=zip_type)
    else:
        doc_name_iter = itertools.repeat('[none]')
    # doc content
    content_iter = parse_file(file_path + suffix, encoding=encoding, zip_type=zip_type)
    for doc_id, class_label, name, content in\
            zip(itertools.count(0), class_label_iter, doc_name_iter, content_iter):
        doc = Document(name, doc_id, class_label)
        doc.content = content
        yield doc


def load(config: dict):
    corpus_type = config.get('corpus-type')
    prefix = config.get('prefix')
    dataset = config.get('dataset')

    if corpus_type is None or prefix is None or dataset is None:
        raise Exception('missing configure item')

    encoding = 'utf-8' if not config.get('encoding') else config.get('encoding')

    if corpus_type == 'file-corpus':
        file_list = config.get('list')
        if not file_list:
            raise Exception('missing `list`')
        file_name = os.path.join(prefix, dataset, file_list+'-full-corpus.txt')
        return corpus_file(os.path.join(prefix, dataset), file_name, encoding=encoding)
    elif corpus_type == 'line-corpus':
        file_name = os.path.join(prefix, dataset, dataset+'.dat')
        # lines = config.get('num-lines', 0)
        return corpus_line(file_name, encoding)
    elif corpus_type == 'gz-corpus':
        file_name = os.path.join(prefix, dataset, dataset+'.dat')
        return corpus_line(file_name, encoding=encoding, zip_type='gz')
    else:
        raise Exception('corpus type was not able to be determined')


def load_from_file(config_file: str):
    return load(toml.load(config_file))


def test_file():
    for doc in corpus_file('', 'test-data/t.lst'):
        print(str(doc))


def test_line():
    for doc in corpus_line('test-data/contents'):
        print(str(doc))


def test_line_gz():
    for doc in corpus_line('test-data/contents', zip_type='gz'):
        print(str(doc))


if __name__ == '__main__':
    # test_file()
    # test_line()
    test_line_gz()
