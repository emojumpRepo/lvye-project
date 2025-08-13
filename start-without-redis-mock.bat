@echo off
echo Starting Yudao Server without Redis Mock...
echo Make sure you have a real Redis server running on localhost:6379
echo.

java -Dyudao.redis.mock.enabled=false -jar yudao-server/target/yudao-server.jar

pause
