COLUMNS = 9
ROWS = 9
TILE_WIDTH = 64
TILE_HEIGHT = 64
WINDOW_WIDTH = COLUMNS * TILE_WIDTH
WINDOW_HEIGHT = ROWS * TILE_HEIGHT

TYPE_EMPTY = 0
TYPE_O = 1
TYPE_X = 2
TYPE_DRAW = 99

MAX_CHECK_DEPTH = 2

MAXIMUM = 99999
MINIMUM = -99999

g_map = nil

function create_empty_map()
    map = {}
    for r = 0, ROWS-1 do
        map[r] = {}
        for c = 0, COLUMNS-1 do
            map[r][c] = TYPE_EMPTY
        end
    end

    return map
end

-- 'O', 'X'
-- g_current_player = 'O'
g_game_over = TYPE_EMPTY

function love.load()
    local screen_width, screen_height = love.window.getDesktopDimensions()
    love.window.setPosition(screen_width/2, screen_height/2)
    love.window.setMode(WINDOW_WIDTH, WINDOW_HEIGHT)

    g_map = create_empty_map()
end

function love.update(dt)
    --
end

function love.draw()
    local block_type = 0
    for r = 0, ROWS-1 do
        for c = 0, COLUMNS-1 do
            block_type = g_map[r][c]
            if block_type == TYPE_O then
                love.graphics.setColor(0, 255, 0)
            elseif block_type == TYPE_X then
                love.graphics.setColor(255, 255, 255)
            else
                love.graphics.setColor(0, 0, 0)
            end
            love.graphics.rectangle(
                "fill",
                TILE_WIDTH*c, TILE_HEIGHT*r,
                TILE_WIDTH, TILE_HEIGHT)
        end
    end

    local c, r = love.mouse.getPosition()
    c, r = math.floor(c/64), math.floor(r/64)
    love.graphics.setColor(255, 128, 0, 100)
    love.graphics.rectangle("fill", c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT)
end

function love.keypressed(key)
   if key == "q" then
      love.event.quit()
   end
end

function get_moves(map)
    moves = {}
    for r = 0, ROWS-1 do
        for c = 0, COLUMNS-1 do
            if map[r][c] == TYPE_EMPTY then
                table.insert(moves, {r=r, c=c})
            end
        end
    end
    return moves
end

function evaluate(map)
    complete = check_complete(map)
    if complete == TYPE_O then
        return 1000
    elseif complete == TYPE_X then
        return -1000
    elseif complete == TYPE_EMPTY then
        return 500
    else
        return 0
    end
end

-- 关于alpha-beta
-- 1) 对于max, 只需要考虑beta(调用方目前为止找到的最小值), 因为上层是min, 只会挑最小的.
--    所以, 只要当前最大子节点的值大于beta, 那么不需要再继续搜索了, 因为可能的情况只有
--    两种:
--    a) 可以搜到更小的, 我们不会要, 因为我们是max
--    b) 可以搜到更大的, 但是我们要了也没用, 因为上层的min已经拿到的beta比我们小
-- 2) 同理对于min, 只需要考虑alpha(调用方目前为止找到的最大值), 因为上层是max, 只会挑最
--    大的. 所以, 只要当前最小子节点的值小于alpha, 那么不需要再继续搜索了, 因为可能的
--    情况只有两种:
--    a) 可以搜到更小的, 我们要, 但是上层已经找到的alpha比我们的大, 会抛弃我们
--    b) 可以搜到更大的, 我们不要, 因为我们是min
--
-- 总结一下, 发现对于alpha和beta, 比较关系都是固定的 if alpha > beta
-- 不同的是
-- max更新的是alpha, 因为上层的alpha对他来说不重要, 需要考虑的是beta
-- 而min更新的是beta, 因为上层的beta对他来说不重要, 需要考虑的是alpha
function min_max(map, depth, ismax, alpha, beta)
    -- print(string.format("min_max(depth=%d, ismax=%s, alpha=%d, beta=%d)",
    --                     depth, tostring(ismax), alpha, beta))
    complete = check_complete(map)
    if complete ~= TYPE_EMPTY or depth <= 0 then
        -- print(string.format("stop complete=%d, depth=%d", complete, depth))
        return evaluate(map), nil
    end

    if ismax then
        local best = MINIMUM
        local best_move = nil
        for _, move in pairs(get_moves(map)) do
            local new_map = deepcopy(map)
            new_map[move.r][move.c] = TYPE_O
            -- print_map(new_map)
            local v = min_max(new_map, depth-1, false, alpha, beta)
            if v > best then
                best = v
                best_move = move
                alpha = best
            end

            if alpha > beta then
                print(string.format("beta(%d) < alpha(%d)", beta, alpha))
                return best, best_move
            end
        end
        -- print(string.format("best=%d, best-move(r=%d, c=%d)",
        --     best, best_move.r, best_move.c))
        return best, best_move
    else
        local best = MAXIMUM
        local best_move = nil
        for _, move in pairs(get_moves(map)) do
            local new_map = deepcopy(map)
            new_map[move.r][move.c] = TYPE_X
            -- print_map(new_map)
            local v = min_max(new_map, depth-1, true, alpha, beta)
            if v < best then
                best = v
                best_move = move
                beta = best
            end

            if beta < alpha then
                print(string.format("beta(%d) > alpha(%d)", beta, alpha))
                return best, best_move
            end
        end
        -- print(string.format("best=%d, best-move(r=%d, c=%d)",
        --     best, best_move.r, best_move.c))
        return best, best_move
    end
end

function print_map(map)
    for r = 0, ROWS-1 do
        print(string.format("%d %d %d", map[r][0], map[r][1], map[r][2]))
    end
end

function next_move(map)
    print("====================================================================")
    print_map(map)
    local best, best_move = min_max(map, MAX_CHECK_DEPTH, false, MINIMUM, MAXIMUM)
    print("best: " .. best)
    print(string.format("best move(r=%d, c=%d)", best_move.r, best_move.c))
    return best_move
end

function in_map(r, c)
    return r >= 0 and r < ROWS and c >= 0 and c < COLUMNS
end

function check_single(map, r, c)
    local curr_type = map[r][c]
    if curr_type == TYPE_EMPTY then
        return 0, 0, 0, 0
    end

    local new_row, new_col = 0, 0

    -- horizontal
    local len_h = 1
    new_row, new_col = r, c-1
    while in_map(new_row, new_col) and curr_type == map[new_row][new_col] do
        len_h = len_h + 1
        new_col = new_col - 1
    end
    new_row, new_col = r, c+1
    while in_map(new_row, new_col) and curr_type == map[new_row][new_col] do
        len_h = len_h + 1
        new_col = new_col + 1
    end
    -- vertical
    local len_v = 1
    new_row, new_col = r-1, c
    while in_map(new_row, new_col) and curr_type == map[new_row][new_col] do
        len_v = len_v + 1
        new_row = new_row - 1
    end
    new_row, new_col = r+1, c
    while in_map(new_row, new_col) and curr_type == map[new_row][new_col] do
        len_v = len_v + 1
        new_row = new_row + 1
    end
    -- left-up
    local len_l = 1
    new_row, new_col = r-1, c-1
    while in_map(new_row, new_col) and curr_type == map[new_row][new_col] do
        len_l = len_l + 1
        new_row = new_row - 1
        new_col = new_col - 1
    end
    new_row, new_col = r+1, c+1
    while in_map(new_row, new_col) and curr_type == map[new_row][new_col] do
        len_l = len_l + 1
        new_row = new_row + 1
        new_col = new_col + 1
    end
    -- right-up
    local len_r = 1
    new_row, new_col = r-1, c+1
    while in_map(new_row, new_col) and curr_type == map[new_row][new_col] do
        len_r = len_r + 1
        new_row = new_row - 1
        new_col = new_col + 1
    end
    new_row, new_col = r+1, c-1
    while in_map(new_row, new_col) and curr_type == map[new_row][new_col] do
        len_r = len_r + 1
        new_row = new_row + 1
        new_col = new_col - 1
    end

    return len_h, len_v, len_l, len_r
end

function check_complete(map)
    local has_empty = false
    for r = 0, ROWS-1 do
        for c = 0, COLUMNS-1 do
            -- print(string.format("r=%d, c=%d, type=%d", r, c, map[r][c]))
            if map[r][c] ~= TYPE_EMPTY then
                local len_h, len_v, len_l, len_r = check_single(map, r, c)
                if len_h >= 3 or len_v >= 3 or len_l >= 3 or len_r >= 3 then
                    -- print(r, c, len_h, len_v, len_l, len_r)
                    return map[r][c]
                end
            else
                has_empty = true
            end
        end
    end

    if has_empty then
        return TYPE_EMPTY
    else
        return TYPE_DRAW
    end
end

function love.mousepressed(x, y, button)
    if button == 'l' and g_game_over == TYPE_EMPTY then
        c, r = math.floor(x/64), math.floor(y/64)
        if g_map[r][c] ~= TYPE_EMPTY then
            return
        end
        g_map[r][c] = TYPE_O
        print(string.format('click: (%d, %d)', c, r))
        g_game_over = check_complete(g_map)
        if g_game_over == TYPE_EMPTY then
            local move = next_move(g_map)
            g_map[move.r][move.c] = TYPE_X
            check_complete(g_map)
        end
    end
end

function deepcopy(orig)
    local orig_type = type(orig)
    local copy
    if orig_type == 'table' then
        copy = {}
        for orig_key, orig_value in next, orig, nil do
            copy[deepcopy(orig_key)] = deepcopy(orig_value)
        end
        setmetatable(copy, deepcopy(getmetatable(orig)))
    else -- number, string, boolean, etc
        copy = orig
    end
    return copy
end



