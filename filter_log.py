#!/usr/bin/python
# -*- coding: utf-8 -*-

import io
import sys

def main():
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    
    out = True
    last_line = ''
    with io.open(sys.stdin.fileno(),'r',encoding='utf-8') as sin:
        for line in sin:
            line = line.strip()
            if (' 警告 ' in line or ' 重大 ' in line) and 'ダウンロードに失敗しました' not in line:
                out = True
            elif ' 情報 ' in line:
                out = False
                
            if out and last_line:
                print(last_line)
                
            last_line = line

if __name__ == '__main__':
    main()
