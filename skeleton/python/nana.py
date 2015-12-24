#!/usr/bin/env python3
# -*- coding: utf-8 -*-


import sys


if sys.version_info.major != 3:
    print('Python 3.x is needed')
    raise Exception('Python 3.x is needed')


# g_best_table = {}
# g_vertex_count_table = {}
# g_edge_list = []
g_vertex_count = 0
MAX = 99999


class Node:
    def __init__(self, p, v):
        self.p, self.v = p, v
        self.children = []

    def __str__(self):
        return "(p={}, v={})".format(str(self.p), self.v)

    def __repr__(self):
        return self.__str__()


def get_tree_node_num():
    return g_vertex_count


def get_edge_list():
    # return (1, 2), (2, 3), (3, 4)
    # return (1, 2), (2, 3), (2, 4)
    return (1, 2), (1, 3), (1, 4), (1, 5), (1, 8), (1, 13), (1, 14), (2, 23), (3, 6), (3, 33), (4, 7), (4, 18),\
           (4, 22), (4, 26), (5, 80), (6, 21), (7, 9), (7, 10), (7, 15), (7, 28), (8, 16), (8, 19), (9, 12), (9, 32),\
           (9, 47), (9, 55), (10, 11), (10, 24), (11, 40), (13, 99), (15, 31), (15, 41), (15, 77), (16, 17), (16, 36),\
           (17, 20), (18, 39), (19, 29), (20, 74), (22, 76), (24, 25), (25, 27), (25, 34), (25, 35), (26, 49),\
           (26, 54), (27, 30), (27, 43), (27, 64), (27, 85), (28, 60), (29, 57), (29, 72), (29, 98), (30, 38),\
           (30, 42), (31, 65), (32, 44), (33, 50), (33, 51), (34, 37), (34, 46), (34, 62), (35, 71), (36, 53),\
           (36, 58), (37, 73), (39, 52), (39, 59), (40, 63), (41, 45), (41, 67), (41, 68), (42, 48), (42, 88),\
           (46, 61), (49, 56), (49, 70), (49, 75), (49, 81), (49, 82), (51, 97), (52, 100), (53, 66), (53, 89),\
           (54, 79), (55, 92), (57, 96), (59, 83), (59, 87), (59, 90), (63, 69), (66, 84), (66, 86), (70, 94),\
           (71, 78), (74, 93), (79, 91), (82, 95)


def node_num(root):
    if root is None:
        return 0
    return sum(node_num(child) for child in root.children) + 1


# 对于每一条边, 我只有两个选择: 1) 切; 2) 不切
# 假设delta为切完后, 分成两堆的节点数之差, 那么
# 1) 切
#    delta1 = abs(count(v1) - count(v2))
# 2) 不切
#    delta2 = best(next)
# 我们需要的结果是
# best = min(delta1, delta2)
# 算法是从根节点开始(树的话, 任意一个节点都可以是根节点), 分别考察其和每个子节点的连线(即edge),
# 应用上面讨论的过程即可
def best(root, best_table, cur_best=MAX):
    if root is None or len(root.children) == 0:
        return MAX

    delta_list = []
    for child in root.children:
        delta = abs((2*node_num(child)) - get_tree_node_num())
        best_table[(root.v, child.v)] = delta
        delta_list.append(delta)
        if delta <= cur_best:
            cur_best = delta
            child_best = best(child, best_table, cur_best)
            delta_list.append(child_best)
            if child_best <= cur_best:
                cur_best = child_best

    return min(delta_list)


def find_node(n, root):
    if not root:
        return None
    if root.v == n:
        return root
    for child in root.children:
        node = find_node(n, child)
        if node is not None:
            return node
    return None


def construct_tree(edge_list):
    root = Node(p=None, v=edge_list[0][0])
    root.children.append(Node(p=root, v=edge_list[0][1]))
    edge_set = set(edge_list[1:])
    tmp_set = set()
    while len(edge_set) > 0:
        for e in sorted(edge_set):
            node = find_node(e[0], root)
            if not node:
                continue
            else:
                node.children.append(Node(node, e[1]))
                tmp_set.add(e)
        edge_set -= tmp_set
    return root


def main():
    edge_list = get_edge_list()
    tree = construct_tree(edge_list)

    global g_edge_list, g_vertex_count
    g_edge_list = edge_list
    g_vertex_count = node_num(tree)

    t = {}
    bv = best(tree, t)
    l = list(map(lambda _: _[0], filter(lambda _: _[1] == bv, t.items())))
    l.sort()
    # print(t)
    print(l[0])


if __name__ == '__main__':
    main()
