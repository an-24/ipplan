set ipplan-host=kav@192.168.0.104
set dbpath=D:\DATABASE
set dbdest=..\..\tmp\database

rd %dbdest% /Q /S
mkdir %dbdest%
copy %dbpath%\ipplan_up.fbk %dbdest%
mkdir %dbdest%\store
copy %dbpath%\store\pattern.fbk %dbdest%\store
copy %dbpath%\store\config.xml %dbdest%\store
copy restore_up.sh %dbdest%

"C:\Program Files\Putty\pscp.exe" -v -r -C %dbdest% %ipplan-host%:/var/local/ipplan