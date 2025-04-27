a=0
def fun():
    global a
    a +=1;print(a)
    fun()
    print(a)

fun()
