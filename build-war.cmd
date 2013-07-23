@echo off
set ANT=D:\Java\eclipse_ipplan_indigo\plugins\org.apache.ant_1.8.2.v20120109-1030\bin

call %ANT%\ant.bat -f build-main.xml
call %ANT%\ant.bat -f build-ipplan.xml

