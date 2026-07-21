local used = redis.call('GET', KEYS[1]) or '0'
if tonumber(used) >= tonumber(ARGV[1]) then return -1 end
local newUsed = redis.call('INCR', KEYS[1])
if tonumber(newUsed) > tonumber(ARGV[1]) then redis.call('DECR', KEYS[1]); return -1 end
return newUsed
