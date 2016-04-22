#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# https://paiza.jp/challenges/50
# A009:ビームの反射

h, w = [int(_) for _ in input().split()]
m = [input() for _ in range(h)]


def valid(x1, y1):
    if x1 < 0 or x1 >= w:
        return False
    if y1 < 0 or y1 >= h:
        return False
    return True


def get_delta(x1, y1, dx1, dy1):
    if m[y1][x1] == '_':
        return dx1, dy1
    elif m[y1][x1] == '/':
        return -dy1, -dx1
    else:  # \
        return dy1, dx1


def count(x, y, dx, dy, total):
    if not valid(x, y):
        return total
    dx, dy = get_delta(x, y, dx, dy)
    x, y = x + dx, y + dy
    return count(x, y, dx, dy, total+1)


print(count(0, 0, 1, 0, 0))


