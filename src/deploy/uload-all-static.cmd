@rem set ipplan-host=kav@192.168.0.104
@set ipplan-host=root@188.225.33.147

@set source=..\..\war-ipplan
@set dest=..\..\tmp\stats

@rd %dest% /Q /S
@mkdir %dest%
@copy %source%\favicon.ico %dest%

@xcopy /E %source%\resources\*.* %dest%\ipplan\resources\
@xcopy /E %source%\ipplan\gwt\*.* %dest%\ipplan\ipplan\gwt\
@copy %source%\ipplan\*.gif %dest%\ipplan\ipplan\
@copy %source%\ipplan\*.js %dest%\ipplan\ipplan\
@copy %source%\ipplan\*.css %dest%\ipplan\ipplan\
@copy %source%\ipplan\*.html %dest%\ipplan\ipplan\
@copy %source%\Ipplan.html %dest%\ipplan\

@set source=..\..\war-main

@xcopy /E %source%\resources\*.* %dest%\main\resources\
@xcopy /E %source%\main\gwt\*.* %dest%\main\main\gwt\
@xcopy /E %source%\main\images\*.* %dest%\main\main\images\
@copy %source%\main\*.gif %dest%\main\main\
@copy %source%\main\*.js %dest%\main\main\
@copy %source%\main\*.css %dest%\main\main\
@copy %source%\main\*.html %dest%\main\main\
@copy %source%\Main.html %dest%\main\

@rem promo
@xcopy /E ..\..\promo\* %dest%\

@echo =================================================
@echo ===== Upload permutations
@echo =================================================
@"C:\Program Files\Putty\pscp.exe" -v -r -C %dest%\* %ipplan-host%:/var/www
