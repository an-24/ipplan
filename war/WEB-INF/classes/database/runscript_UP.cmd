copy /b UP_createDB.sql + UP_create.sql final.sql
D:\DBMS\FB25\bin\isql.exe -i final.sql
rem del -y final.sql