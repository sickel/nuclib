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
        cur.execute("drop table if exists settings")
        cur.execute("create table settings(key varchar not null,\
        value varchar not null)")
        cur.execute("insert into settings (key,value) values(\"Version\" , \"4\") ")
        cur.execute("drop table if exists line")
        cur.execute("drop table if exists lines")
        cur.execute("create table line(id integer primary key, \
        nuclide varchar not null,\
        energy float not null,\
        energyunc varchar,\
        prob float not null,\
        probunc varchar,\
        comment varchar)")
	cur.execute("create unique index nucenergy on line(nuclide,energy)")
        cur.execute("drop table if exists nuclide")
        cur.execute("create table nuclide (id integer primary key, \
        longname varchar not null,\
        name varchar not null,\
        Z integer not null,\
        A integer not null,\
        N integer not null,\
        halflife float,\
        halflifeunc float,\
        meta varchar default '',\
        element varchar not null)")
	cur.execute("create unique index longname on nuclide(longname)")
    # get the first worksheet
    sheet = book.sheet_by_index(0)
 
    firstrow=9
    nucs=[]
    for row in range(firstrow,sheet.nrows):
    # read a row
        if sheet.cell(row,1).value != '':
            data=sheet.row_values(row)
            if sheet.cell(row,0).value != '':
                nuc=sheet.cell(row,0).value.replace(' ','')
                #longname=nuc
                #if( "/" in nuc):
                nucs=nuc.split("/")
                for nuc in nucs:
		  print nuc
		  set=nuc.split('-')
		  Z=set[0]
		  A=set[2]
		  meta=''
		  if(A[-1]=="m"):
		    A=A[:-1]
		    meta="m"
		  N=int(A)-int(Z)
		  print set
                  print Z,A,N
                  name=set[1]+set[2]
                  print name
                  elem=set[1]
                  try:
		    cur.execute("insert into nuclide (longname,name,A,Z,N,element,meta) values(?,?,?,?,?,?,?)",[nuc,name,A,Z,N,elem,meta])
		  except sqlite3.IntegrityError:
		    print '{}, alredy exists'.format(nuc)
            #else:
            #    sheet.cell(row,0).value=nuc
            #    data[0]=nuc
            for nuc in nucs:
	      data[0]=nuc
              print data
              try:
                 cur.execute("INSERT into line (nuclide,energy,energyunc,prob,probunc,comment) values(?,?,?,?,?,?)",data)
              except sqlite3.IntegrityError:
		print '{}, {} alredy exists'.format(nuc,data[1])
    dbconn.commit()
    print "So far so good..."
    cur.execute("SELECT * FROM LINE where energy > 200 and energy < 210")
    rows=cur.fetchall()
    for row in rows:
        print row
        
    book = xlrd.open_workbook('halflives.xlsx')
    sheet = book.sheet_by_index(0)
    firstrow=10
    for row in range(firstrow,sheet.nrows):
        data=sheet.row_values(row)
        nuc=data[0].replace(' ','')
        th=data[1].split("±")
        print th
        if("(" in th[0]):
            th[0]=th[0].replace('(','')
            th[1]=th[1].replace(')','')
            exp=th[1].split(" 10+")
            th[0]=float(th[0])*10**float(exp[1])
            th[1]=float(exp[0])*10**float(exp[1])
        print nuc,th[0],th[1]
        cur.execute("update nuclide set halflife=?,halflifeunc=? where longname=?",[th[0],th[1],nuc])
    dbconn.commit()
   # cell = first_sheet.cell(0,0)
   # print cell
   # print cell.value
 
    # read a row slice
    #print first_sheet.row_slice(rowx=0,
     #                           start_colx=0,
     #                           end_colx=2)
 
#----------------------------------------------------------------------
if __name__ == "__main__":
    path = "iaeadata.xlsx"
    open_file(path)