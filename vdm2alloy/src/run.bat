@echo off
cls
set jre6="java"
rem "c:\Program Files (x86)\Java\jre6\bin\java.exe"

rem setup for single run
copy generated\Telephone\specifications\Telephone\vdm\telephone.vdmsl .  
copy generated\Telephone\vdmutil.als .
copy generated\Telephone\m2.als . 
rem ready to run
echo running translation and model finder on telephone

set args=-vdm "telephone.vdmsl" -o telephone -v -test2 m2.als

%jre6% -cp "*;lib/*" org.overture.alloy.Main %args%

echo moving output into generated/single run
set out=generated\singlerun
mkdir %out%
move telephone.vdmsl %out%
move telephone.als %out%
move m2.als %out%
move vdmutil.als %out% 
echo telephone output is in $out

%jre6% -cp "*;lib/*" junit.textui.TestRunner org.overture.alloy.test.TelephoneTest
%jre6% -cp "*;lib/*" junit.textui.TestRunner org.overture.alloy.test.TelephoneOriginalTest
%jre6% -cp "*;lib/*" junit.textui.TestRunner org.overture.alloy.test.TrafficTest
%jre6% -cp "*;lib/*" junit.textui.TestRunner org.overture.alloy.test.CountryColouringTest
rem %jre6% -cp "*;vdm2alloy/lib/*" junit.textui.TestRunner org.overture.alloy.test.OthersTest

