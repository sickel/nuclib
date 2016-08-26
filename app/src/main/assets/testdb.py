import xlrd
import sqlite3
#----------------------------------------------------------------------
def open_file(sql):
    dbconn=sqlite3.connect('nuclides.db')
    with dbconn:
        cur = dbconn.cursor()
    print "So far so good..."
    cur.execute(sql)
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
    sql="select count(*) from line"
    open_file(sql)
    sql="SELECT * FROM LINE where energy > 200 and energy < 210"
    open_file(sql)
    sql="select * from settings"
    open_file(sql)