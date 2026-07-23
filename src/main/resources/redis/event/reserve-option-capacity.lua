local used = redis.call('GET', KEYS[1])
if not used then
    redis.call('SET', KEYS[1], '0')
    used = '0'
end

local usedNumber = tonumber(used)
local capacity = tonumber(ARGV[1])
if not usedNumber or usedNumber < 0 or not capacity or capacity <= 0 then
    return -3
end
if usedNumber >= capacity then
    return -1
end

return redis.call('INCR', KEYS[1])
