local stock = redis.call('GET', KEYS[1]) or '0'
if tonumber(stock) < tonumber(ARGV[1]) then return -1 end
local newStock = redis.call('DECRBY', KEYS[1], tonumber(ARGV[1]))
if tonumber(newStock) < 0 then redis.call('INCRBY', KEYS[1], tonumber(ARGV[1])); return -1 end
return newStock
