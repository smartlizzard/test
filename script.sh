if ! grep -E "RestDispstcher" inventory.ini; then 
 sed -i "/[Dispatcher]/a[RestDispstcher]" inventory.ini
fi
export lines=$(grep PUB tags.properties | wc -l)
export line=1
echo "$line $lines"
while [[ $line -le $lines ]] 
do  
    DISPIP=$(grep DISP$line= disp.properties | sed "s/DISP$line=//g" | sed 's/"//g' )
    if [ "$1" == "PUB$line" ]; then
        sed -i "/\[Dispatcher\]/a$DISPIP" inventory.ini
    else 
        sed -i "/\[RestDispstcher\]/a$DISPIP" inventory.ini
    fi
    export line=$(expr $line + 1)
done 
