int main () {
    //Declarations
    int integer;
    bool boolN;
    char charChar;
    float floatY;
    int intR;
    bool boolTruth;
    float floater;
    char laterInit;
    float largeValue;
    
    //Initializations
    integer = 200;
    boolN = false;
    charChar = 'N';
    floatY = 12.12;
    intR = 25;
    boolTruth = true;
    floater = 0.12;
    largeValue = 4879.76;
    //Statements
    while (floatY < integer)
        floatY = floatY + intR;
    
    print floatY;
    
    if (floatY == 212 + floater)
        print 'Y';
    else
        print charChar;
    
    laterInit = charChar;
    
    if( charChar == laterInit && boolN || floatY*23 + 1 == largeValue/2 + largeValue/2 )
    print boolTruth;
    else
    print boolN;
    
    if (floatY < intR < 1)
        print floatY;
    else
        while (floatY > 50)
            floatY = floatY - intR;
    print floatY;
}