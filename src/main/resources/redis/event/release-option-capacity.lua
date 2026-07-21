local used = redis.call('GET', KEYS[1]) or '0'
if tonumber(used) <= 0 then return 0 end
return redis.call('DECR', KEYS[1])
