copy /b UD_createDB.sql + UD_create.sql final.sql
D:\DBMS\FB25\bin\isql.exe -i final.sql
rem del -y UD_create.sql