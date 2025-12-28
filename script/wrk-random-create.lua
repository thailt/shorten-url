-- wrk Lua script for creating aliases (POST requests)
-- Generates random URLs and aliases, then performs POST requests

-- Character set for random alias generation (alphanumeric)
local chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

-- Sample URLs to use (mix of different domains)
local urls = {
    "https://www.example.com/page1",
    "https://www.example.com/page2",
    "https://www.example.com/page3",
    "https://github.com/user/repo",
    "https://stackoverflow.com/questions/123",
    "https://www.google.com/search?q=test",
    "https://www.wikipedia.org/wiki/Test",
    "https://www.reddit.com/r/programming",
    "https://www.youtube.com/watch?v=test",
    "https://www.amazon.com/product/123",
    "https://www.netflix.com/watch/123",
    "https://www.twitter.com/user/status/123",
    "https://www.linkedin.com/in/user",
    "https://www.medium.com/@user/article",
    "https://www.dev.to/user/article"
}

-- Generate random alias (10-15 characters)
function generateRandomAlias()
    local length = math.random(10, 15)
    local alias = ""
    for i = 1, length do
        local rand = math.random(1, #chars)
        alias = alias .. string.sub(chars, rand, rand)
    end
    return alias
end

-- Generate random URL from pool or create dynamic one
function generateRandomUrl()
    local usePool = math.random() < 0.7 -- 70% use pool, 30% generate dynamic
    if usePool then
        return urls[math.random(1, #urls)]
    else
        -- Generate dynamic URL with random path
        local domains = {"example.com", "test.com", "demo.com", "sample.org"}
        local domain = domains[math.random(1, #domains)]
        local path = ""
        for i = 1, math.random(1, 3) do
            path = path .. "/" .. generateRandomAlias()
        end
        return "https://" .. domain .. path
    end
end

-- Initialize random seed
math.randomseed(os.time())

-- Request counter for unique aliases
local counter = 0

-- Request function - called for each request
request = function()
    counter = counter + 1
    local alias = generateRandomAlias()
    local url = generateRandomUrl()
    
    -- Create JSON payload
    local payload = string.format(
        '{"url":"%s","alias":"%s","expire":null}',
        url,
        alias
    )
    
    -- Format POST request with headers
    local headers = {}
    headers["Content-Type"] = "application/json"
    headers["Accept"] = "application/json"
    
    return wrk.format("POST", "/app/api/create", headers, payload)
end

