for /f "tokens=5" %a in ('netstat -ano ^| findstr :8080') do taskkill /pid %a /f;
for /f "tokens=5" %a in ('netstat -ano ^| findstr :8081') do taskkill /pid %a /f;

for /f "tokens=5" %a in ('netstat -ano ^| findstr :8081') do taskkill /pid %a /f