#!/usr/bin/python3
# -*- coding: utf-8 -*-

import urllib.request
import sys

PREFIX='qmaclone_'

def main():
    file = __file__
#     file = '/etc/munin/plugins/qmaclone_active_players'
    mode = file[file.index(PREFIX)+len(PREFIX):]
    
    if len(sys.argv) == 1:
#         with urllib.request.urlopen('http://gilgamesh:58080/QMAClone/stats?mode=' + mode) as res:
        with urllib.request.urlopen('http://127.0.0.1:58080/QMAClone/stats?mode=' + mode) as res:
            print(res.read().decode('utf-8'))
        
    elif len(sys.argv) == 2:
        if sys.argv[1] == 'config':
            if mode == 'current_players':
                print('graph_title Current Players')
                print('graph_category qmaclone')
            elif mode == 'total_players':
                print('graph_title Total Players')
                print('graph_category qmaclone')
            elif mode == 'current_sessions':
                print('graph_title Current Sessions')
                print('graph_category qmaclone')
            elif mode == 'total_sessions':
                print('graph_title Total Sessions')
                print('graph_category qmaclone')
            elif mode == 'page_view':
                print('graph_title Page View')
                print('graph_category qmaclone')
            elif mode == 'problems':
                print('graph_title Problems')
                print('graph_category qmaclone')
            elif mode == 'login_players':
                print('graph_title Login Players')
                print('graph_category qmaclone')
            elif mode == 'active_players':
                print('graph_title Active Players')
                print('graph_category qmaclone')
            elif mode == 'players_in_whole':
                print('graph_title Players in Whole')
                print('graph_category qmaclone')
        
        elif sys.argv[1] == 'autoconf':
            print('yes')


if __name__ == '__main__':
    main()
