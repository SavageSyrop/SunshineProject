@echo off
chcp 1251 > nul
set /p pswd="¬ведите ваш пароль от MYSQL: "
powershell -command "(Get-Content ./src/main/resources/application.properties) -replace 'MYSQL_PASS', '"%pswd%"' | Out-File -encoding ASCII ./src/main/resources/application.properties"
mysql -u root -p%pswd% < fill_sunshine.sql
if "%ERRORLEVEL%" == "0" (
echo CONNECTION SUCCESSFUL
) else (
echo CONNECTION FAILED
pause
exit
)
CALL mvnw.cmd clean install -f pom.xml
pause
