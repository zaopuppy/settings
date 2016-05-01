#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os.path
import shutil

from analyzer import Analyzer


class InvertedIndex:
    """
    The inverted_index class stores information on a corpus indexed by term_ids.
    Each term_id key is associated with a per-document frequency (by doc_id).
    It is assumed all this information will not fit in memory, so a large
    postings file containing the (term_id -> each doc_id) information is saved on
    disk. A lexicon (or "dictionary") contains pointers into the large postings
    file. It is assumed that the lexicon will fit in memory.
    """
    def __init__(self, config: dict):
        self._index_name = config.get('inverted-index')
        self._analyzer = Analyzer.load(config)
        self._total_corpus_terms = 0

    def index_name(self):
        return self._index_name

    def is_valid(self):
        return False

    def create_index(self, config_file: str):
        shutil.copyfile(config_file, os.path.join(self.index_name(), 'config.toml'))
        print('creating index: ' + self.index_name())


if __name__ == '__main__':
    pass
