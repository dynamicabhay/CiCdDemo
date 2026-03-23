-- KEYS[1] = current window key (e.g., "rl:ip:1.2.3.4:20250302:1435")
-- KEYS[2] = previous window key (e.g., "rl:ip:1.2.3.4:20250302:1434")
-- ARGV[1] = current timestamp (seconds)
-- ARGV[2] = window size (60)
-- ARGV[3] = limit (100)

local now = tonumber(ARGV[1])
local windowSize = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])
local current = tonumber(redis.call('GET', KEYS[1]) or '0')
local previous = tonumber(redis.call('GET', KEYS[2]) or '0')
local elapsed = now % windowSize
local weight = elapsed / windowSize  -- fraction of current window passed
local estimated = previous * (1 - weight) + current

if estimated < limit then
    local count = redis.call('INCR', KEYS[1])
    if count == 1 then
        redis.call('EXPIRE', KEYS[1], windowSize * 2)
    end
    return "1"  -- allow
else
    return "0" -- deny
end