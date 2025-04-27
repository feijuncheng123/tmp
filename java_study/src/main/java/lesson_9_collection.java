

import org.junit.Test;

import java.io.FileInputStream;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 一、集合框架的概述
 * <p>
 * 1.集合、数组都是对多个数据进行存储操作的结构，简称Java容器。
 * 说明：此时的存储，主要指的是内存层面的存储，不涉及到持久化的存储（.txt,.jpg,.avi，数据库中）
 * <p>
 * 2.1 数组在存储多个数据方面的特点：
 * > 一旦初始化以后，其长度就确定了。
 * > 数组一旦定义好，其元素的类型也就确定了。我们也就只能操作指定类型的数据了。
 * 比如：String[] arr;int[] arr1;Object[] arr2;
 * 2.2 数组在存储多个数据方面的缺点：
 * > 一旦初始化以后，其长度就不可修改。
 * > 数组中提供的方法非常有限，对于添加、删除、插入数据等操作，非常不便，同时效率不高。
 * > 获取数组中实际元素的个数的需求，数组没有现成的属性或方法可用
 * > 数组存储数据的特点：有序、可重复。对于无序、不可重复的需求，不能满足。
 * <p>
 * 二、集合框架
 * 1、      |----Collection接口：单列集合，用来存储一个一个的对象
 * 1.1         |----List接口：存储有序的、可重复的数据。  -->“动态”数组
 * 1.1.1               |----ArrayList、LinkedList、Vector
 *
 * 1.2         |----Set接口：存储无序的、不可重复的数据   -->高中讲的“集合”
 * 1.2.1               |----HashSet、LinkedHashSet、TreeSet
 *
 * 2、      |----Map接口：双列集合，用来存储一对(key - value)一对的数据   -->高中函数：y = f(x)
 * 2.1.1               |----HashMap、LinkedHashMap、TreeMap、Hashtable、Properties
 * <p>
 * <p>
 * 三、Collection接口中的方法的使用
 */
public class lesson_9_collection {


    /**
     * 1、Collection接口下共有方法：向Collection接口的实现类的对象中添加数据obj时，要求obj所在类要重写equals().
     */
    public static void main(String[] args) {
        Collection c = new ArrayList();
        c.size();  //集合中元素的个数
        c.add(12); //添加一个元素。Collection中add为直接添加一个object。返回bool类型。原处修改
        Collection c1 = Arrays.asList(1, 2, 3);
        c.addAll(c1); //添加一个集合的全部元素，返回bool类型。原处修改
        c.isEmpty(); //是否为空
        c.clear(); //清空全部元素。原处修改
        c.contains(args);//是否包含某一元素，使用元素的equals方法判断
        c.containsAll(c1); //是否包含另一个集合的全部元素；
        c.retainAll(c1); //保留共同的元素（交集）.返回bool类型。原处修改。返回修改后的c
        c.remove("");//返回bool类型。原处修改
        c.removeAll(c1); //删除c1包含的元素。返回bool类型。原处修改
        c.equals(c1); //判断所有元素是否完全一样。注意！
        c.hashCode(); //计算集合的hash值，主要set中使用
        Object[] o = c.toArray(); //转为数组，object类型
        Iterator i = c.iterator(); //返回一个iterator接口类的对象,迭代器
        i.next();
        while (i.hasNext()) {
            i.next();
        }   //迭代器主要用法之一
        for (Object e : c) {
            System.out.println(e);
        }  //增强for循环
    }

    /**
     * collection遍历：
     * Iterator 接口：所有实现了Collection接口的集合类都有一个iterator()方法，用以返回一个实现了Iterator接口的对象
     * Iterator 仅用于遍历集合，Iterator 本身并不提供承装对象的能力。
     * 如果需要创建 Iterator 对象，则必须有一个被迭代的集合
     *
     * 1.内部的方法：hasNext() 和  next()
     * 2.集合对象每次调用iterator()方法都得到一个全新的迭代器对象，
     * 默认游标都在集合的第一个元素之前。
     * 3.内部定义了remove(),可以在遍历的时候，删除集合中的元素。此方法不同于集合直接调用remove()
     */
    public void Test01() {
        Collection coll = new ArrayList();
        coll.add(123);
        coll.add(456);
        coll.add(new Person("Jerry",20));
        coll.add(new String("Tom"));
        coll.add(false);

        Iterator iterator = coll.iterator();
        //方式一：
//        System.out.println(iterator.next());
//        System.out.println(iterator.next());
//        //报异常：NoSuchElementException
//        System.out.println(iterator.next());

        //方式二：不推荐
//        for(int i = 0;i < coll.size();i++){
//            System.out.println(iterator.next());
//        }

        //方式三：推荐
        ////hasNext():判断是否还有下一个元素
        while(iterator.hasNext()){
            //next():①指针下移 ②将下移以后集合位置上的元素返回
            System.out.println(iterator.next());
        }

        //错误方式一：
        while((iterator.next()) != null){   //已经占用了一位
            System.out.println(iterator.next());
        }

        //错误方式二：
        //集合对象每次调用iterator()方法都得到一个全新的迭代器对象，默认游标都在集合的第一个元素之前。
        while (coll.iterator().hasNext()){
            System.out.println(coll.iterator().next());
        }


        //remove方法：会导致集合中相应的元素也被删除。必须跟在next之后，而且只能有一次remove
        while (iterator.hasNext()){
//            iterator.remove();
            Object obj = iterator.next();
            if("Tom".equals(obj)){
                iterator.remove();
//                iterator.remove();
            }

        }

        for(Object o:coll){    //依然调用的是迭代器
            System.out.println(o);
        }
    }


    /**
     * 2.1、 List接口框架
     * <p>
     * 2.1.1   |----Collection接口：单列集合，用来存储一个一个的对象
     * 2.1.2         |----List接口：存储有序的、可重复的数据。  -->“动态”数组,替换原有的数组
     * 2.1.2.1             |----ArrayList：作为List接口的主要实现类；线程不安全的，效率高；底层使用Object[] elementData存储
     * 2.1.2.2             |----LinkedList：对于频繁的插入、删除操作，使用此类效率比ArrayList高；底层使用双向链表存储
     * 2.1.2.3             |----Vector：作为List接口的古老实现类；线程安全的，效率低；底层使用Object[] elementData存储
     * <p>
     * <p>
     * 2. ArrayList的源码分析：
     * 2.1 jdk 7情况下
     *    ArrayList list = new ArrayList();//底层创建了长度是10的Object[]数组elementData
     *    list.add(123);//elementData[0] = new Integer(123);
     *    ...
     *    list.add(11);//如果此次的添加导致底层elementData数组容量不够，则扩容。
     *    默认情况下，扩容为原来的容量的1.5倍，同时需要将原有数组中的数据复制到新的数组中。
     *    <p>
     *    结论：建议开发中使用带参的构造器：ArrayList list = new ArrayList(int capacity)
     *    <p>
     * 2.2 jdk 8中ArrayList的变化：
     *    ArrayList list = new ArrayList();//底层Object[] elementData初始化为{}.并没有创建长度为10的数组
     *    <p>
     *    list.add(123);//第一次调用add()时，底层才创建了长度10的数组，并将数据123添加到elementData[0]
     *    ...
     *    后续的添加和扩容操作与jdk 7 无异。
     * 2.3 小结：jdk7中的ArrayList的对象的创建类似于单例的饿汉式，而jdk8中的ArrayList的对象
     *    的创建类似于单例的懒汉式，延迟了数组的创建，节省内存。
     * <p>
     * 3. LinkedList的源码分析：
     *   LinkedList list = new LinkedList(); 内部声明了Node类型的first和last属性，默认值为null
     *   list.add(123);//将123封装到Node中，创建了Node对象。
     *   <p>
     *    其中，Node定义为：体现了LinkedList的双向链表的说法
     *    private static class Node<E> {
     *       E item;
     *       Node<E> next;
     *       Node<E> prev;
     *    <p>
     * Node(Node<E> prev, E element, Node<E> next) {
     * this.item = element;
     * this.next = next;
     * this.prev = prev;
     * }
     * }
     * <p>
     * 4. Vector的源码分析：jdk7和jdk8中通过Vector()构造器创建对象时，底层都创建了长度为10的数组。
     * 在扩容方面，默认扩容为原来的数组长度的2倍。
     * <p>
     * 面试题：ArrayList、LinkedList、Vector三者的异同？
     * 同：三个类都是实现了List接口，存储数据的特点相同：存储有序的、可重复的数据
     * 不同：见上
     * <p>
     * <p>
     * <p>
     * 5. List接口中的常用方法
     *   void add(int index, Object ele):在index位置插入ele元素
     *   boolean addAll(int index, Collection eles):从index位置开始将eles中的所有元素添加进来
     *   Object get(int index):获取指定index位置的元素
     *   int indexOf(Object obj):返回obj在集合中首次出现的位置
     *   int lastIndexOf(Object obj):返回obj在当前集合中末次出现的位置
     *   Object remove(int index):移除指定index位置的元素，并返回此元素
     *   Object set(int index, Object ele):设置指定index位置的元素为ele
     *   List subList(int fromIndex, int toIndex):返回从fromIndex到toIndex位置的子集合
     *
     * 总结：常用方法
     * 增：add(Object obj)
     * 删：remove(int index) / remove(Object obj)
     * 改：set(int index, Object ele)
     * 查：get(int index)
     * 插：add(int index, Object ele)
     * 长度：size()
     * 遍历：① Iterator迭代器方式
     *      ② 增强for循环
     *      ③ 普通的循环
     */

    public void listTest() {
        ArrayList list = new ArrayList();
        list.add(123);
        list.add(456);
        list.add("AA");



        int[] ints = {2, 34, 55, 22, 11};
        long[] longs = {1, 2, 3};
        double[] doubles = {1, 2, 3};
        char[] chars={'1','c','d'};
        Arrays.stream(ints).boxed().collect(Collectors.toList());
        Arrays.stream(longs).boxed().collect(Collectors.toList());
        Arrays.stream(doubles).boxed().collect(Collectors.toList());

        List<Character> collect = new String(chars).chars().mapToObj(c -> (char) c).collect(Collectors.toList());


        //方式一：Iterator迭代器方式
        Iterator iterator = list.iterator();
        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }

        System.out.println("***************");

        //方式二：增强for循环
        for(Object obj : list){
            System.out.println(obj);
        }

        System.out.println("***************");

        //方式三：普通for循环
        for(int i = 0;i < list.size();i++){
            System.out.println(list.get(i));
        }

        //int indexOf(Object obj):返回obj在集合中首次出现的位置。如果不存在，返回-1.
        int index = list.indexOf(4567);
        System.out.println(index);

        //int lastIndexOf(Object obj):返回obj在当前集合中末次出现的位置。如果不存在，返回-1.
        System.out.println(list.lastIndexOf(456));

        //Object remove(int index):移除指定index位置的元素，并返回此元素
        Object obj = list.remove(0);
        System.out.println(obj);
        System.out.println(list);

        //Object set(int index, Object ele):设置指定index位置的元素为ele
        list.set(1,"CC");
        System.out.println(list);

        //List subList(int fromIndex, int toIndex):返回从fromIndex到toIndex位置的左闭右开区间的子集合
        List subList = list.subList(2, 4);
        System.out.println(subList);
        System.out.println(list);


        //boolean addAll(int index, Collection eles):从index位置开始将eles中的所有元素添加进来
        List<Integer> list1 = Arrays.asList(1, 2, 3);
        list.addAll(list1);
//        list.add(list1);
        System.out.println(list.size());//9

        list1.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    }

    /**
     * List接口（主要实现类ArrayList）主要方法：
     * 1、本质上，ArrayList是对象引用的一个变长数组。ArrayList 是线程不安全的
     *          void add(int index, Object ele)         在特定下标添加元素
     *          boolean addAll(int index, Collection eles)     在特定下标添加多个元素
     *          Object get(int index)    通过下标取数
     *          int indexOf(Object obj)   获取特定元素下标
     *          int lastIndexOf(Object obj)   重复时最后一个元素的索引
     *          Object remove(int index)    删除指定索引元素，并返回该元素
     *          Object set(int index, Object ele)   更改特定索引元素
     *          List subList(int fromIndex, int toIndex)   切片，切片元素包含首位不含末位：[)
     *          Arrays.asList(…) 方法返回的 List 集合既不是 ArrayList 实例，也不是 Vector 实例。 Arrays.asList(…)  返回值是一个固定长度的List集合
     *
     *2、LinkedList类（链表）：对于频繁的插入或删除元素的操作建议使用
     *          新增方法
     *          void addFirst(Object obj)
     *          void addLast(Object obj)
     *          Object getFirst()
     *          Object getLast()
     *          Object removeFirst()
     *          Object removeLast()
     *
     *3、Vector：Vector是线程安全的。Vector总是比ArrayList慢，尽量避免使用
     *          新增方法：
     *          void addElement(Object obj)
     *          void insertElementAt(Object obj,int index)
     *          void setElementAt(Object obj,int index)
     *          void removeElement(Object obj)
     *          void removeAllElements()
     */
    public void listMethodTest() {

    }


    /**
     * 3. Set接口的框架：
     *
     *3.1 |----Collection接口：单列集合，用来存储一个一个的对象
     *3.1.1          |----Set接口：存储无序的、不可重复的数据   -->高中讲的“集合”
     *3.1.1.1              |----HashSet：作为Set接口的主要实现类；线程不安全的；可以存储null值
     *                            |----LinkedHashSet：作为HashSet的子类；遍历其内部数据时，可以按照添加的顺序遍历
     *                                      对于频繁的遍历操作，LinkedHashSet效率高于HashSet.
     *3.1.1.2              |----TreeSet：可以按照添加对象的指定属性，进行排序。
     *
     *
     *  1. Set接口中没有额外定义新的方法，使用的都是Collection中声明过的方法。
     *
     *  2. 要求：向Set(主要指：HashSet、LinkedHashSet)中添加的数据，其所在的类一定要重写hashCode()和equals()
     *     要求：重写的hashCode()和equals()尽可能保持一致性：相等的对象必须具有相等的散列码
     *      重写两个方法的小技巧：对象中用作 equals() 方法比较的 Field，都应该用来计算 hashCode 值。
     *
     *
     *
     */
    public void setTest() { }

    /**
     * 3.1 HashSet：
     *     1. 无序性：不等于随机性。存储的数据在底层数组中并非按照数组索引的顺序添加，而是根据数据的哈希值(hash)决定的。
     *
     *     2. 不可重复性：保证添加的元素按照equals()判断时，不能返回true.即：相同的元素只能添加一个。
     *
     *     二、添加元素的过程：以HashSet为例：
     *         我们向HashSet中添加元素a,首先调用元素a所在类的hashCode()方法，计算元素a的哈希值，
     *         此哈希值接着通过某种算法计算出在HashSet底层数组中的存放位置（即为：索引位置），判断
     *         数组此位置上是否已经有元素：
     *             如果此位置上没有其他元素，则元素a添加成功。 --->情况1
     *             如果此位置上有其他元素b(或以链表形式存在的多个元素），则比较元素a与元素b的hash值：
     *                 如果hash值不相同，则元素a添加成功。--->情况2
     *                 如果hash值相同，进而需要调用元素a所在类的equals()方法：
     *                        equals()返回true,元素a添加失败
     *                        equals()返回false,则元素a添加成功。--->情况2
     *
     *         对于添加成功的情况2和情况3而言：元素a 与已经存在指定索引位置上数据以链表的方式存储。
     *         jdk 7 :元素a放到数组中，指向原来的元素。
     *         jdk 8 :原来的元素在数组中，指向元素a
     *         总结：七上八下
     *
     *         HashSet底层：数组+链表的结构。
     */
    @Test
    public void setTest1(){
        Set set = new HashSet();
        set.add(456);
        set.add(123);
        set.add(123);
        set.add("AA");
        set.add("CC");
        set.add(129);

        Iterator iterator = set.iterator();
        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }

    /**
     *3.2 LinkedHashSet：是 HashSet 的子类,可以按照添加的顺序遍历。但不允许集合元素重复
     *     LinkedHashSet作为HashSet的子类，在添加数据的同时，每个数据还维护了两个引用，记录此数据前一个
     *     数据和后一个数据。
     *     优点：对于频繁的遍历操作，LinkedHashSet效率高于HashSet
     */
    @Test
    public void setTest2(){
        Set set = new LinkedHashSet();
        set.add(456);
        set.add(123);
        set.add(123);
        set.add("AA");
        set.add("CC");
        set.add(129);

        Iterator iterator = set.iterator();
        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }


    /**
     *3.3  TreeSet：是 SortedSet（set子类） 接口的实现类， 可以确保集合元素处于排序状态。
     *    1.向TreeSet中添加的数据，要求是相同类的对象。
     *    2.两种排序方式：自然排序（实现Comparable接口） 和 定制排序（Comparator）
     *    3.自然排序中，比较两个对象是否相同的标准为：compareTo()返回0.不再是equals().
     *    4.定制排序中，比较两个对象是否相同的标准为：compare()返回0.不再是equals().
     *
     *    注意：不能添加不同类的对象, 只能向TreeSet中添加Comparator中定义的类型相同的对象。否则发生ClassCastException异常。
     *     使用定制排序判断两个元素相等的标准是：通过Comparator比较两个元素返回了0。
     *
     *    添加自定义对象时，需要实现compareTo方法。
     *     Comparator comparator()
     *     Object first()
     *     Object last()
     *     Object lower(Object e)
     *     Object higher(Object e)
     *     SortedSet subSet(fromElement, toElement)
     *     SortedSet headSet(toElement)
     *     SortedSet tailSet(fromElement)
     *
     *     TreeSet两种排序方法：
     *     自然排序：TreeSet 会调用集合元素的 compareTo(Object obj) 方法来比较元素之间的大小关系，然后将集合元素按升序排列。添加的对象的类必须实现 Comparable 接口。
     *     TreeSet判断两个对象是否相等的唯一标准是：两个对象通过 compareTo(Object obj) 方法比较返回值。如果compareTo返回值为0，则视为重复
     *     只有第一个元素无须比较compareTo()方法，后面添加的所有元素都会调用compareTo()方法进行比较。
     *
     *     定制排序：
     *     需要定制排序，比如降序排列，需要重写compare(T o1,T o2)方法
     *     利用int compare(T o1,T o2)方法，比较o1和o2的大小：如果方法返回正整数，则表示o1大于o2；如果返回0，表示相等；返回负整数，表示o1小于o2。
     *     要实现定制排序，需要将实现Comparator接口的实例作为形参传递给TreeSet的构造器。
     */
    @Test
    public void setTest3(){
        TreeSet ts = new TreeSet();
        ts.add(123);
        ts.add("asd");

        Comparator com = new Comparator() {
            //按照年龄从小到大排列
            @Override
            public int compare(Object o1, Object o2) {
                if(o1 instanceof Person && o2 instanceof Person){
                    Person u1 = (Person)o1;
                    Person u2 = (Person)o2;
                    return Integer.compare(u1.getAge(),u2.getAge());
                }else{
                    throw new RuntimeException("输入的数据类型不匹配");
                }
            }
        };

        TreeSet set = new TreeSet(com);
        set.add(new Person("Tom",12));
        set.add(new Person("Jerry",32));
        set.add(new Person("Jim",2));
        set.add(new Person("Mike",65));
        set.add(new Person("Mary",33));
        set.add(new Person("Jack",33));
        set.add(new Person("Jack",56));


        Iterator iterator = set.iterator();
        while(iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }


    //样例：
    class comp implements Comparable {
        private String name;
        private int id;

        public comp(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "comp{" + "name='" + name + '\'' + ", id=" + id + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            comp comp = (comp) o;
            return id == comp.id &&
                    name.equals(comp.name);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }


        @Override
        public int compareTo(Object o) {
            if (o instanceof comp) {
                comp p = (comp) o;
                int i = Integer.compare(this.id, p.id);
                if (i == 0) {
                    return this.name.compareTo(p.name);
                } else {
                    return i;
                }
            }
            throw new ClassCastException("类型不一致");
        }
    }


    /**
     * collections工具类常用方法：操作Collection、Map(??)的工具类
     * reverse(List)：反转 List 中元素的顺序
     * shuffle(List)：对 List 集合元素进行随机排序
     * sort(List)：根据元素的自然顺序对指定 List 集合元素按升序排序
     * sort(List，Comparator)：根据指定的 Comparator 产生的顺序对 List 集合元素进行排序
     * swap(List，int， int)：将指定 list 集合中的 i 处元素和 j 处元素进行交换
     *
     * Object max(Collection)：根据元素的自然顺序，返回给定集合中的最大元素
     * Object max(Collection，Comparator)：根据 Comparator 指定的顺序，返回给定集合中的最大元素
     * Object min(Collection)
     * Object min(Collection，Comparator)
     * int frequency(Collection，Object)：返回指定集合中指定元素的出现次数
     * void copy(List dest,List src)：将src中的内容复制到dest中
     * boolean replaceAll(List list，Object oldVal，Object newVal)：使用新值替换 List 对象的所有旧值
     *
     * 同步控制：
     * Collections 类中提供了多个 synchronizedXxx() 方法，该方法可使将指定集合包装成线程同步的集合，从而可以解决多线程并发访问集合时的线程安全问题
     */
    @Test
    public void collectionsTest(){
        List list = new ArrayList();
        list.add(123);
        list.add(43);
        list.add(765);
        list.add(-97);
        list.add(0);

        //报异常：IndexOutOfBoundsException("Source does not fit in dest")
//        List dest = new ArrayList();
//        Collections.copy(dest,list);
        //正确的：
        List dest = Arrays.asList(new Object[list.size()]);
        System.out.println(dest.size());//list.size();
        Collections.copy(dest,list);

        System.out.println(dest);

        /*
        Collections 类中提供了多个 synchronizedXxx() 方法，该方法可使将指定集合包装成线程同步的集合，
        从而可以解决多线程并发访问集合时的线程安全问题
         */
        //返回的list1即为线程安全的List
        List list1 = Collections.synchronizedList(list);

        //unmodifiableXxx方法，返回的是不可更改的集合。
        List list2 = Collections.unmodifiableList(list);


        Collections.reverse(list);
        Collections.shuffle(list);
        Collections.sort(list);
        Collections.swap(list,1,2);
        int frequency = Collections.frequency(list, 123);

        System.out.println(list);
        System.out.println(frequency);

    }

}
