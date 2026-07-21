local stock = redis.call('GET', KEYS[1]) or '0'
local newStock = redis.call('INCRBY', KEYS[1], tonumber(ARGV[1]))
return newStock
