import json

def get_data():

    #find the number of line and define an offset to find the 300 lasts lines
    lineNumber = sum(1 for line in open("data.txt", "r"))
    print "number of lines %d"%lineNumber
    offset = lineNumber-300

    #initialize all variable we need to perfom the computation
    file = open("data.txt", 'r')
    tMax = 0
    dateTMax = ""
    tMin = 300
    dateTMin = ""
    pMax = 0
    datePMax = ""
    pMin = 2000
    datePMin= ""
    i = 0
    tAverage = 0
    pAverage = 0
    total = []

    data = file.readline()
    while data!="":
        dataParsed = json.loads(data)

        p = dataParsed['Pressure']
        t = dataParsed['Temperature']
        if t>tMax:
        tMax=t
        dateTMax = dataParsed['Date']
        if t<tMin:
        tMin=t
        dateTMin = dataParsed['Date']
        if p>pMax:
        pMax=p
        datePMax = dataParsed['Date']
        if p<pMin:
        pMin=p
        datePMin = dataParsed['Date']

        tAverage = tAverage + t
        pAverage = pAverage + p

        i = i+1
        if(i>offset):
            total.append(dataParsed)

        data = file.readline()

    tAverage = tAverage / i
    pAverage = pAverage / i

    final_data = {}
    final_data['tAverage']=tAverage
    final_data['pAverage']=pAverage

    final_data['pMax'] = pMax
    final_data['tMax'] = tMax
    final_data['pMin'] = pMin
    final_data['tMin'] = tMin

    final_data['DatePMax'] = datePMax
    final_data['DateTMax'] = dateTMax
    final_data['DatePMin'] = datePMin
    final_data['DateTMin'] = dateTMin

    final_data['lastMeasures'] = total

    final_data = json.dumps(final_data)
    #print final_data

    #print "at :"+dateTMax+" tMax=%.2f"%tMax
    #print "at :"+dateTMin+" tMin=%.2f"%tMin
    #print " average temp : %.2f"%tAverage
    #print "at :"+datePMax+" pMax=%.2f"%pMax
    #print "at :"+datePMin+" pMin=%.2f"%pMin
    #print " average pressure : %.2f"%pAverage

    #print "size of total:%d"%len(total)

    return final_data
