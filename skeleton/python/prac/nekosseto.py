#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# https://paiza.jp/poh/ando/challenge/06dab33f/ando20

# print(''.join('RW'[divmod(divmod(_, n*2)[1], n)[0]] for _ in range(m)))

# 24 = 6 + 8 + zan + zan/3 + aki


# def min2str(n):
#     MOD = 24 * 60
#     n += MOD
#     n = divmod(n, MOD)[1]
#     h, m = divmod(n, 60)
#     return '{:02}:{:02}'.format(h, m)
#
# days = int(input())
#
# print('\n'.join(min2str(60-divmod(int(input()), 3)[0]) for _ in range(days)))
#

# https://paiza.jp/challenges/117

h, w = [int(_) for _ in input().split()]

m = [input() for _ in range(h)]

x, y = [int(_) for _ in input().split()]

n = int(input())


def valid(x1, y1):
    if x1 <= 0 or x1 > w:
        return False
    if y1 <= 0 or y1 > h:
        return False
    return True


def mov(x1, y1, act1):
    # m1[y][x]
    nx, ny = x1, y1
    if act == 'U':
        ny -= 1
    elif act == 'R':
        nx += 1
    elif act == 'D':
        ny += 1
    else:
        nx -= 1
    if not valid(nx, ny):
        return x1, y1
    if m[ny-1][nx-1] == '.':
        return nx, ny
    else:
        return mov(nx, ny, act1)


for i in range(n):
    act = input()
    x, y = mov(x, y, act)

print(x, y)

# END