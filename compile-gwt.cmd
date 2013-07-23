@set GWTPATH=D:\Java\eclipse_ipplan_indigo\plugins\com.google.gwt.eclipse.sdkbundle_2.5.1\gwt-2.5.1
@set CP=
@for %%I in (%GWTPATH%\*.jar) do @call cpappend.cmd %%I
@for %%I in (lib\*.jar) do @call cpappend.cmd %%I
@set CP=%CP%;bin;src;

@echo ================= Ipplan compile ======================
@java -Xmx512M -cp %CP% com.google.gwt.dev.Compiler -logLevel INFO -style OBF -war war-ipplan com.cantor.ipplan.Ipplan
@echo ================= Main compile ======================
@java -Xmx512M -cp %CP% com.google.gwt.dev.Compiler -logLevel INFO -style OBF -war war-main com.cantor.ipplan.Main