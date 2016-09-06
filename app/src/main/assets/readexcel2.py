# coding=utf-8
import xlrd
import sqlite3
import sys
reload(sys)
sys.setdefaultencoding('utf8')
#----------------------------------------------------------------------
def open_file(path):
    """
    Open and read an Excel file
    """
    book = xlrd.open_workbook(path)
    dbconn=sqlite3.connect('nuclides.db')
    with dbconn:
        cur = dbconn.cursor()
    sheet = book.sheet_by_index(0)
    timef={"seconds":24*3600,"minutes":24*60,"hours":24,"days":1,"years":1/365.2422}
    elements={"Er":68,"Mg":12,"Br":35,"Rh":75,"Gd":64,"Dy":66,"Ta":73,"Ti":22,"Pt":78,"Xe":54,"Pd":46,"Lu":71,"Nd":60,"Os":76,"Hf":72,"Re":75,"U":92,"Ge":32,"As":33,"Pm":61,"Tb":65,"Ni":28,"Be":4,"W":74,"La":57,"Pr":59,"Zr":40,"Rb":37,"Ar":18,"Ca":20,"V":23,"Cl":17,"Al":13,"N":7}
    firstrow=0
    for row in range(firstrow,sheet.nrows):
        energy=sheet.cell(row,0).value
    # read a row
        if energy != '' and energy !='Gamma Energy (KeV)':
            #print sheet.cell(row,0)
            
            data=sheet.row_values(row)
            nuc=sheet.cell(row,1).value
            t_12=sheet.cell(row,2).value.split(" ")
            t_12_unit=t_12[1]
            #print t_12_unit
            thalf=float(t_12[0])/timef[t_12_unit]
            #nucpart=nuc.split("-")
            longname=nuc
            if( "/" in nuc):
               set=nuc.split("/")
               nuc=set[0]
            print nuc
            set=nuc.split('-')
            A=set[1]
            meta=''
            if(A[-1]=="m"):
               meta='m' 
               A=A[:-1]
            #N=int(A)-int(Z)
            #print set
            #print Z,A,N
            name=set[0]+set[1]
            #print name
            elem=set[0]
            sql="select name,halflife from nuclide where name='"+name+"'"
            with dbconn:
                cur = dbconn.cursor()
                cur.execute(sql)
                rows=cur.fetchall()
            nreg=len(rows)
            if nreg==0:
                sql="select distinct Z from nuclide where element='"+elem+"'"
                cur.execute(sql)
                rows=cur.fetchall()
                if len(rows)>0:
                    Z=rows[0][0]
                else:
                #    print(elem)
                    Z=elements[elem]
                longname=str(Z)+"-"+elem+"-"+str(set[1])
                N=int(A)-int(Z)
                print longname
                cur.execute("insert into nuclide (longname,name,A,Z,N,element,halflife) values(?,?,?,?,?,?,?)",[longname,name,A,Z,N,elem,thalf])
            #print data
                prob=float(sheet.cell(row,3).value)/100
                cur.execute("INSERT into line (nuclide,energy,prob,comment) values(?,?,?,?)",[longname,energy,prob,sheet.cell(row,1).value])
            else:
                if(rows[0][1]==None):
                    sql="update nuclide set halflife="+str(thalf)+" where name='"+name+"'"
                    print sql
                    cur.execute(sql)
    dbconn.commit()
        
    
#----------------------------------------------------------------------
if __name__ == "__main__":
    path = "cpp_edu_pbsiegel_bio431_genergies.xlsx"
    open_file(path)
