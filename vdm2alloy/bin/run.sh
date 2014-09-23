#!/bin/sh

jre6="java"
args='-vdm telephone.vdmsl -o telephone -v -test2 m2.als'

#setup for single run
cp generated/Telephone/specifications/Telephone/vdm/telephone.vdmsl .  
cp generated/Telephone/vdmutil.als .
cp generated/Telephone/m2.als . 
# ready to run
echo running translation and model finder on telephone
$jre6 -cp "*:lib/*" org.overture.alloy.Main $args
echo moving output into generated/single run
out=generated/singlerun
mkdir -p $out
mv telephone.vdmsl $out
mv telephone.als $out
mv m2.als $out
mv vdmutil.als $out 
echo telephone output is in $out

$jre6 -cp "*:lib/*" junit.textui.TestRunner org.overture.alloy.test.TelephoneTest
$jre6 -cp "*:lib/*" junit.textui.TestRunner org.overture.alloy.test.TelephoneOriginalTest
$jre6 -cp "*:lib/*" junit.textui.TestRunner org.overture.alloy.test.TrafficTest
$jre6 -cp "*:lib/*" junit.textui.TestRunner org.overture.alloy.test.CountryColouringTest
#rem %jre6% -cp "*:vdm2alloy/lib/*" junit.textui.TestRunner org.overture.alloy.test.OthersTest
