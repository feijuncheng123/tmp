import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class lesson_18_collectors {
    public static void main(String[] args) {
        List<Student> menu = Arrays.asList(
                new Student("刘一", 721, true, Student.GradeType.THREE),
                new Student("陈二", 637, true, Student.GradeType.THREE),
                new Student("张三", 666, true, Student.GradeType.THREE),
                new Student("李四", 531, true, Student.GradeType.TWO),
                new Student("王五", 483, false, Student.GradeType.THREE),
                new Student("赵六", 367, true, Student.GradeType.THREE),
                new Student("孙七", 499, false, Student.GradeType.ONE));

        //1、Collectors.averagingDouble求取平均值，如果没有元素，则结果为0
        Double averagingDouble = menu.stream().collect(Collectors.averagingDouble(Student::getTotalScore));
        Optional.ofNullable(averagingDouble).ifPresent(System.out::println);

        //2、Collectors.collectingAndThen：先收集相应数据（第一个函数)；然后对收集后的数据应用第二个函数
        List<Student> studentList = menu.stream().collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        System.out.println(studentList);

        Optional.ofNullable(menu.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.averagingInt(Student::getTotalScore), a -> "The Average totalScore is->" + a)
        )).ifPresent(System.out::println);
        // The Average totalScore is->557.7142857142857

        //3、Collectors.counting：计数
        Optional.of(menu.stream().collect(Collectors.counting())).ifPresent(System.out::println);


        //4、Collectors.groupingBy(Function): 根据输入func返回的数进行group操作，收集到map中。
        Map<Student.GradeType, List<Student>> collect = menu.stream().collect(Collectors.groupingBy(Student::getGradeType));
        Optional.ofNullable(collect).ifPresent(System.out::println);
        //{TWO=[Student{name='李四', totalScore=531, local=true, gradeType=TWO}], THREE=[Student{name='刘一', totalScore=721, local=true, gradeType=THREE}, Student{name='陈二', totalScore=637, local=true, gradeType=THREE}, Student{name='张三', totalScore=666, local=true, gradeType=THREE}, Student{name='王五', totalScore=483, local=false, gradeType=THREE}, Student{name='赵六', totalScore=367, local=true, gradeType=THREE}], ONE=[Student{name='孙七', totalScore=499, local=false, gradeType=ONE}]}


        //5、Collectors.groupingBy(Function, Collector):根据输入func返回的数进行group操作，然后对收集到的集合采用Collector操作
        Map<Student.GradeType, Long> collect1 = menu.stream()
                .collect(Collectors.groupingBy(Student::getGradeType, Collectors.counting()));

        //6、Collectors.groupingBy(Function, Supplier, Collector):  使用Supplier作为最终map的实现
        TreeMap<Student.GradeType, Double> map = menu.stream().collect(Collectors.groupingBy(
                        Student::getGradeType,
                        TreeMap::new,
                        Collectors.averagingInt(Student::getTotalScore)));

        Optional.of(map.getClass()).ifPresent(System.out::println);
        Optional.of(map).ifPresent(System.out::println);


        //7、Collectors.groupingByConcurrent(Function)  ： 并发的groupby，不保证顺序
        //8、Collectors.groupingByConcurrent(Function, Collector)  ： 并发的groupby
        //9、Collectors.groupingByConcurrent(Function, Supplier, Collector)  ： 并发的groupby


        //10、Collectors.joining()：将输入元素连接成String
        Optional.of(menu.stream().map(Student::getName).collect(Collectors.joining())).ifPresent(System.out::println);
        //刘一陈二张三李四王五赵六孙七

        //11、Collectors.joining(delimiter)：用分割符连接
        Optional.of(menu.stream().map(Student::getName).collect(Collectors.joining(","))).ifPresent(System.out::println);
        // 刘一,陈二,张三,李四,王五,赵六,孙七

        //12、Collectors.joining(delimiter, prefix, suffix)：以指定的分隔符、前缀和后缀连接起来。

        //13、Collectors.map(func,collectors) : 使用func映射元素，使用collectors操作处理映射后元素
        Optional.of(menu.stream().collect(Collectors.mapping(Student::getName, Collectors.joining(","))))
                .ifPresent(System.out::println);


        //14、Collectors.maxBy()、Collectors.minBy():根据特定字段求最大值、最小值
        menu.stream().collect(Collectors.maxBy(Comparator.comparingInt(Student::getTotalScore)))
                .ifPresent(System.out::println);

        //15、Collectors.partitioningBy(): 类似groupingBy，但是是根据boolean条件划分分区，分为两部分
        Map<Boolean, List<Student>> collect12 = menu.stream()
                .collect(Collectors.partitioningBy(Student::isLocal));

        Optional.of(collect12).ifPresent(System.out::println);

        //16、Collectors.partitioningBy(Predicate, Collector)

        //17、Collectors.reducing()
        menu.stream().collect(Collectors
                .reducing(BinaryOperator.maxBy(Comparator
                                .comparingInt(Student::getTotalScore))))
                .ifPresent(System.out::println);




        //18、Collectors.summarizingDouble: 描述性统计，总数、总和、最高、最低、平均
        DoubleSummaryStatistics result = menu.stream()
                .collect(Collectors.summarizingDouble(Student::getTotalScore));

        //19、Collectors.toCollection
        LinkedList<Student> collect2 = menu.stream().filter(d -> d.getTotalScore() > 600)
                .collect(Collectors.toCollection(LinkedList::new));

        //20、Collectors.toConcurrentMap
        //21、Collectors.toMap
        //22、Collectors.toList
        //23、Collectors.toSet

    }
}


/**
 * 学生信息
 */
class Student {
    /**
     * 姓名
     */
    private String name;
    /**
     * 总分
     */
    private int totalScore;
    /**
     * 是否本地人
     */
    private boolean local;
    /**
     * 年级
     */
    private GradeType gradeType;

    /**
     * 年级类型
     */
    public enum GradeType {ONE, TWO, THREE}

    public Student(String name, int totalScore, boolean local, GradeType gradeType) {
        this.name = name;
        this.totalScore = totalScore;
        this.local = local;
        this.gradeType = gradeType;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", totalScore=" + totalScore +
                ", local=" + local +
                ", gradeType=" + gradeType +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public GradeType getGradeType() {
        return gradeType;
    }

    public void setGradeType(GradeType gradeType) {
        this.gradeType = gradeType;
    }
}
