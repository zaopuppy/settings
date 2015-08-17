COLUMNS = 9
ROWS = 9
TILE_WIDTH = 64
TILE_HEIGHT = 64
WINDOW_WIDTH = COLUMNS * TILE_WIDTH
WINDOW_HEIGHT = ROWS * TILE_HEIGHT

TYPE_EMPTY = 0
TYPE_MOUSE = -1

COLOR_EMPTY = { 0, 0, 0 }
COLOR_MOUSE = { 255, 128, 0, 100 }

g_palette = {
    [0] = { 0, 0, 0 },
    { 255, 128, 0, 100 },
}

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

function create_ramdom_map()
    map = {}
    for r = 0, ROWS-1 do
        map[r] = {}
        for c = 0, COLUMNS-1 do
            map[r][c] = TYPE_EMPTY
        end
    end

    return map
end

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
    for r = 0, ROWS-1 do
        for c = 0, COLUMNS-1 do
            color = g_palette[g_map[r][c]]
            love.graphics.setColor(unpack(color))
            love.graphics.rectangle(
                "fill",
                TILE_WIDTH*c, TILE_HEIGHT*r,
                TILE_WIDTH, TILE_HEIGHT)
        end
    end

    local c, r = love.mouse.getPosition()
    c, r = math.floor(c/TILE_WIDTH), math.floor(r/TILE_HEIGHT)
    love.graphics.setColor(unpack(COLOR_MOUSE))
    love.graphics.rectangle(
        "fill",
        c*TILE_WIDTH, r*TILE_HEIGHT,
        TILE_WIDTH, TILE_HEIGHT)
end

function love.keypressed(key)
   if key == "q" then
      love.event.quit()
   end
end

function print_map(map)
    for r = 0, ROWS-1 do
        print(string.format("%d %d %d", map[r][0], map[r][1], map[r][2]))
    end
end

function in_map(r, c)
    return r >= 0 and r < ROWS and c >= 0 and c < COLUMNS
end

function love.mousepressed(x, y, button)
    if button == 'l' and g_game_over == TYPE_EMPTY then
        --
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



