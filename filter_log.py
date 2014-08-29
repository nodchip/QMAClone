#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys

def main():
    out = True
    last_line = ''
    for line in sys.stdin:
        if line.startswith("警告:") or line.startswith("重大:") or line.startswith("WARNING:") or line.startswith("SEVERE:"):
            out = True
        elif line.startswith("情報:") or line.startswith("INFO:"):
            out = False
        if out:
            print last_line.strip()
        last_line = line

if __name__ == '__main__':
    main()
