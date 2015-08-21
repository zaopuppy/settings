#!/usr/bin/lua

local socket = require('socket')

channel = ...

while true do
    print('in thread')
    c = channel:pop()
    while c do
        print('got: ' .. c)
        c = channel:pop()
    end
    socket.select(nil, nil, 0.1)
end


