
Player = {}

--
-- function Block:new(p, x, y, v, a, rot, size)
function Player:new(p, id)
    local obj = p
    if obj == nil then
        obj = { id=id,
                x=0, y=0,
                v_x=0, v_y = 0,
                a_x=0, a_y = 0,
                rot=0, size=32 }
    end
    self.__index = self
    return setmetatable(obj, self)
end

function Player:update(dt)
    self.x = self.x + dt*self.v_x
    self.y = self.y + dt*self.v_y
    self.v_x = self.v_x + dt*self.a_x
    self.v_y = self.v_y + dt*self.a_y
end

function Player:handle_input(k)
    if k == 'e' then
        self.v_y = self.v_y - 10
    elseif k == 'd' then
        self.v_y = self.v_y + 10
    elseif k == 's' then
        self.v_x = self.v_x - 10
    elseif k == 'f' then
        self.v_x = self.v_x + 10
    end
end


