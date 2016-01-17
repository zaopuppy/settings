#!/usr/bin/env python3
# -*- coding: utf-8 -*-


class Analyzer:
    """
    An class that provides a framework to produce token counts from documents.
    All analyzers inherit from this class and (possibly) implement tokenize().
    """

    @classmethod
    def load(cls, config: dict):
        pass

    @classmethod
    def default_filter_chain(cls, config: dict):
        pass

    @classmethod
    def default_unigram_chain(cls, config: dict):
        pass

    @classmethod
    def load_filters_from_config(cls, global_config: dict, config: dict):
        pass

    @classmethod
    def load_filters_from_stream(cls, token_stream, config: dict):
        pass

    @classmethod
    def create_parser(cls, doc, extension: str, delims: str):
        pass

    @classmethod
    def get_content(cls, doc) -> str:
        pass

    def __init__(self, config: dict):
        pass

    def tokenize(self, doc):
        pass


if __name__ == '__main__':
    pass
