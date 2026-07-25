local stock = redis.call('GET', KEYS[1])
if not stock then
    return -2
end

local stockNumber = tonumber(stock)
local quantity = tonumber(ARGV[1])
if not stockNumber or stockNumber < 0 or not quantity or quantity <= 0 then
    return -3
end

return redis.call('INCRBY', KEYS[1], quantity)
