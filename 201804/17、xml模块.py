import xml.etree.ElementTree as et
#类似于json.作为数据传输的媒介，出现时间很早，范围广但古老，逐渐被json代替
#格式类似于html


tree=et.parse("文件名.xml")
root=tree.getroot()  #获取跟标签（根节点）
root.tag  #根节点的tag名

for child in root:
    print(child.tag)   #子标签的tag号
    child.attrib   #取出标签的属性值，以字典形式返回
    for i in child:
        print(i.tag)  #子标签的子标签
        i.text  #标签内容

for i in root.iter('tag名'):  #返回特定标签。无论标签的深度。从外往里找
    i.text='新值'  #对标签重新赋值
    i.set('属性名','属性值')    #修改标签属性值
    #但没有修改源文件，只对内存内进行修改

root.remove('标签名')  #删除标签

tree.write("新文件名.xml") #将内存内的tree对象写入新文件，包括修改后的内容

#创建xml
new_xml=et.Element("root")  #创建根节点
name=et.SubElement(new_xml,'name',attrib={'k1':'v1'}) #在根节点下创建子节点
name.text="abc"




