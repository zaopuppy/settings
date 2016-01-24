#!/usr/bin/env python3
# -*- coding: utf-8 -*-


class Document:
    """
    pass
    """

    def __str__(self, *args, **kwargs):
        return "file-path='{}', doc-id={}, class-label={}".format(
                self._file_path, self._doc_id, self._class_label)

    def __init__(self, file_path: str, doc_id: int, class_label: str):
        self._file_path = file_path
        self._doc_id = doc_id
        self._class_label = class_label
        self.content = ''


if __name__ == '__main__':
    pass
