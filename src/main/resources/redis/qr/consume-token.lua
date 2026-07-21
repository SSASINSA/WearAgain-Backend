local payload = redis.call('GET', KEYS[1])
if not payload then return nil end
redis.call('DEL', KEYS[1])
return payload
