import xlrd
import sqlite3
#----------------------------------------------------------------------
def open_file(path):
    """
    Open and read an Excel file
    """
    book = xlrd.open_workbook(path)
    dbconn=sqlite3.connect('nuclides.db')
    with dbconn:
        cur = dbconn.cursor()
        cur.execute("drop table if exists line")
        cur.execute("drop table if exists lines")
        cur.execute("create table line(id integer primary key, \
        nuclide varchar not null,\
        energy float not null,\
        energyunc varchar,\
        prob float not null,\
        probunc varchar,\
        comment varchar)")
    # get the first worksheet
    sheet = book.sheet_by_index(0)
 
    firstrow=9
    for row in range(firstrow,sheet.nrows):
    # read a row
        if sheet.cell(row,1).value != '':
            data=sheet.row_values(row)
            if sheet.cell(row,0).value != '':
                nuc=sheet.cell(row,0).value
                print nuc
            else:
                sheet.cell(row,0).value=nuc
                data[0]=nuc
            print data
            cur.execute("INSERT into line (nuclide,energy,energyunc,prob,probunc,comment) values(?,?,?,?,?,?)",data)
    print "So far so good..."
    cur.execute("SELECT * FROM LINE where energy > 200 and energy < 210")
    rows=cur.fetchall()
    for row in rows:
        print row
 
    
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