#!/usr/bin/env python3
# -*- coding: utf-8 -*-


class DiskIndex:
    """
    Holds generic data structures and functions that inverted_index and
    forward_index both use. Provides common interface for both and is implemented
    using the pointer-to-implementation method.
    """
    INVALID_DOC_ID = -1

    def __init__(self):
        pass

    def index_name(self):
        return ''

    def num_docs(self):
        return 1

    def doc_name(self, doc_id):
        return ''

    def doc_path(self, doc_id):
        return ''

    def docs(self):
        return []

    def doc_size(self, doc_id):
        return 1024

    def label(self, doc_id):
        """
        :param doc_id:
        :return: the label of the class that the document belongs to, or an empty string if a label was not assigned
        """
        return ''

    def label_id(self, doc_id):
        """
        :param doc_id:
        :return: the label_id of the class that to document belongs to
        """
        return 3

    def id(self, label):
        return 24

    def class_label_from_id(self, label_id):
        return ''

    def num_labels(self):
        return 3

    def class_labels(self):
        """
        :return: the distinct class labels possible for documents in this index
        """
        return ''

    def unique_terms(self, doc_id=INVALID_DOC_ID):
        pass

    def get_term_id(self, term):
        pass

    def term_text(self, term_id):
        pass


if __name__ == '__main__':
    pass
