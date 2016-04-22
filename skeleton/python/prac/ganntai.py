#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# https://paiza.jp/poh/ando/challenge/06dab33f/ando16


n = int(input())
my_n = int(input())
my_list = set(int(_) for _ in input().split())
shop_n = int(input())
shop_list = set(int(_) for _ in input().split())

delta = shop_list.difference(my_list)
if not delta:
    print('None')
else:
    print(' '.join(str(_) for _ in sorted(delta)))
