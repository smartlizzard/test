publisher=$1
if ! grep -E "RestDispstcher" inventory.ini; then 
 sed -i "/[Dispatcher]/a[RestDispstcher]" inventory.ini
fi
lines=$(grep PUB tags.properties | wc -l)
line=1
while (( $line <= $lines ))
do  
    DISPIP=$(grep DISP$line= disp.properties | sed "s/DISP$line=//g" | sed 's/"//g' )
    if [ "$publisher" == "PUB$line" ]; then
        sed -i "/\[Dispatcher\]/a$DISPIP" inventory.ini
    else 
        sed -i "/\[RestDispstcher\]/a$DISPIP" inventory.ini
    fi
    export line=$(expr $line + 1)
done 
