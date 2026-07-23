local used = redis.call('GET', KEYS[1])
if not used then
    return -2
end

local usedNumber = tonumber(used)
if not usedNumber or usedNumber < 0 then
    return -3
end
if usedNumber == 0 then
    return 0
end

return redis.call('DECR', KEYS[1])
