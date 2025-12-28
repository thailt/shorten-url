-- wrk Lua script for random GET requests
-- Generates random aliases and performs GET requests

-- Character set for random alias generation (alphanumeric)
local chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

-- Generate random alias (10 characters)
function generateRandomAlias()
    local alias = ""
    for i = 1, 10 do
        local rand = math.random(1, #chars)
        alias = alias .. string.sub(chars, rand, rand)
    end
    return alias
end

-- Initialize random seed
math.randomseed(os.time())

-- Request function - called for each request
request = function()
    local alias = generateRandomAlias()
    local path = "/" .. alias
    return wrk.format("GET", path)
end

