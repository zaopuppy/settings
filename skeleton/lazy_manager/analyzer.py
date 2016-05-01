#!/usr/bin/env python3
# -*- coding: utf-8 -*-


_method_map = {}


def register_analyzer(identifier: str, analyzer):
    if identifier in _method_map.keys():
        raise Exception('analyser is already exist')


def _create_analyzer(config: dict, ana: dict):
    method = ana.get('method')
    if not method:
        raise Exception('missing `method`')
    _method_map.get(method)(config, ana)


class Analyzer:
    """
    An class that provides a framework to produce token counts from documents.
    All analyzers inherit from this class and (possibly) implement tokenize().
    """

    @classmethod
    def load(cls, config: dict):
        return [_create_analyzer(config, ana) for ana in config.get('analyzers', [])]

    @classmethod
    def default_filter_chain(cls, config: dict):
        raise NotImplementedError

    @classmethod
    def default_unigram_chain(cls, config: dict):
        raise NotImplementedError

    @classmethod
    def load_filters_from_config(cls, global_config: dict, config: dict):
        raise NotImplementedError

    @classmethod
    def load_filters_from_stream(cls, token_stream, config: dict):
        raise NotImplementedError

    @classmethod
    def create_parser(cls, doc, extension: str, delims: str):
        raise NotImplementedError

    @classmethod
    def get_content(cls, doc) -> str:
        raise NotImplementedError

    def __init__(self, config: dict):
        pass

    def tokenize(self, doc):
        raise NotImplementedError


if __name__ == '__main__':
    pass
