local quotaKey = KEYS[1]
local studentKey = KEYS[2]

local studentTtlSeconds = tonumber(ARGV[1])

if redis.call('EXISTS', quotaKey) == 0 then
    return -1
end

if redis.call('EXISTS', studentKey) == 1 then
    return -2
end

local remain = tonumber(redis.call('GET', quotaKey))
if remain == nil or remain <= 0 then
    return 0
end

redis.call('DECR', quotaKey)
redis.call('SET', studentKey, '1', 'EX', studentTtlSeconds)
return 1
