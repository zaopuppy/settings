
require('player')

COLUMNS = 9
ROWS = 9
TILE_WIDTH = 64
TILE_HEIGHT = 64
WINDOW_WIDTH = COLUMNS * TILE_WIDTH
WINDOW_HEIGHT = ROWS * TILE_HEIGHT

TYPE_EMPTY = 0
TYPE_MOUSE = 1
TYPE_PLAYER1 = 2
TYPE_PLAYER2 = 3
TYPE_PLAYER3 = 4
TYPE_PLAYER4 = 5

g_palette = {
    [0] = { 0, 0, 0 },      -- empty
    { 255, 128, 0, 100 },   -- mouse

    { 255, 128, 0, 100 },   -- player1
    { 128, 128, 0, 100 },   -- player2
    { 0,   128, 0, 100 },     -- player3
    { 255, 0,   0, 100 },     -- player4
}

-- g_mouse_x, g_mouse_y = 0, 0


g_player_table = {}

g_client_thread = love.thread.newThread('server_thread.lua')
g_client_channel = love.thread.newChannel()

g_nex_player_id = 0
function gen_id()
    id = g_nex_player_id
    g_nex_player_id = g_nex_player_id + 1
    return id
end

function love.load()
    local screen_width, screen_height = love.window.getDesktopDimensions()
    love.window.setPosition(screen_width/2, screen_height/2)
    love.window.setMode(WINDOW_WIDTH, WINDOW_HEIGHT)

    g_client_thread:start(g_client_channel)

    add_new_player()
end

function add_new_player()
    id = gen_id()
    player = Player:new()
    player.size = 30
    g_player_table[id] = player
end

function love.update(dt)
    for idx, player in pairs(g_player_table) do
        player:update(dt)
    end
end

function love.draw()
    for idx, player in pairs(g_player_table) do
        love.graphics.rectangle(
            "fill",
            player.x, player.y, player.size, player.size)
    end
    --
    mouse_x, mouse_y = love.mouse.getPosition()
    love.graphics.printf('x=' .. mouse_x .. ', y=' .. mouse_y, 0, 0, WINDOW_WIDTH, 'left')
end

function quit()
    -- g_client_thread.stop()?
    love.event.quit()
end

function love.keypressed(key)
    -- g_client_channel:push(key)
    if key == 'q' then
        print('quit')
        quit()
    end
    for idx, player in pairs(g_player_table) do
        player:handle_input(key)
    end
end

function love.mousepressed(x, y, button)
    if button == 'l' and g_game_over == TYPE_EMPTY then
        --
    end
end



