redis.replicate_commands()
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local period = tonumber(ARGV[2])
local quantity = tonumber(ARGV[3]) or 1
local timestamp = tonumber(redis.call(‘time’)[1])

if (redis.call(‘exists’, key) == 0) then
redis.call(‘hmset’, key, ‘remain’, capacity, ‘timestamp’, timestamp)
else
local remain = tonumber(redis.call(‘hget’, key, ‘remain’))
local last_reset = tonumber(redis.call(‘hget’, key, ‘timestamp’))
local delta_quota = math.floor(((timestamp - last_reset) / period) * capacity)
if (delta_quota > 0) then
remain = remain + delta_quota
if (remain > capacity) then
remain = capacity
end
redis.call(‘hmset’, key, ‘remain’, remain, ‘timestamp’, timestamp)
end
end

local result = 0
local remain = tonumber(redis.call(‘hget’, key, ‘remain’))
if (remain < quantity) then
result = 0
else
result = 1
redis.call(‘hincrby’, key, ‘remain’, -quantity)
end

return result