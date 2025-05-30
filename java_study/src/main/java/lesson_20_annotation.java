import java.lang.annotation.*;
import java.util.ArrayList;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;

/**
 * 注解的使用
 * <p>
 * 1. 理解Annotation:
 * ① jdk 5.0 新增的功能
 * <p>
 * ② Annotation 其实就是代码里的特殊标记, 这些标记可以在编译, 类加载, 运行时被读取, 并执行相应的处理。通过使用 Annotation,
 * 程序员可以在不改变原有逻辑的情况下, 在源文件中嵌入一些补充信息。
 * <p>
 * ③在JavaSE中，注解的使用目的比较简单，例如标记过时的功能，忽略警告等。在JavaEE/Android
 * 中注解占据了更重要的角色，例如用来配置应用程序的任何切面，代替JavaEE旧版中所遗留的繁冗
 * 代码和XML配置等。
 * <p>
 * 2. Annocation的使用示例
 * 示例一：生成文档相关的注解
 * 示例二：在编译时进行格式检查(JDK内置的三个基本注解)
 *
 * @Override: 限定重写父类方法, 该注解只能用于方法
 * @Deprecated: 用于表示所修饰的元素(类, 方法等)已过时。通常是因为所修饰的结构危险或存在更好的选择
 * @SuppressWarnings: 抑制编译器警告
 * <p>
 * 示例三：跟踪代码依赖性，实现替代配置文件功能
 * <p>
 * 3. 如何自定义注解：参照@SuppressWarnings定义
 * ① 注解声明为：@interface
 * ② 内部定义成员，通常使用value表示
 * ③ 可以指定成员的默认值，使用default定义
 * ④ 如果自定义注解没有成员，表明是一个标识作用。
 * <p>
 * 如果注解有成员，在使用注解时，需要指明成员的值。
 * 自定义注解必须配上注解的信息处理流程(使用反射)才有意义。
 * 自定义注解通过都会指明两个元注解：Retention、Target
 * <p>
 * 4. jdk 提供的4种元注解
 * 元注解：对现有的注解进行解释说明的注解
 * Retention：指定所修饰的 Annotation 的生命周期：SOURCE\CLASS（默认行为）\RUNTIME
 * 只有声明为RUNTIME生命周期的注解，才能通过反射获取。
 * Target:用于指定被修饰的 Annotation 能用于修饰哪些程序元素
 * ******出现的频率较低*******
 * Documented:表示所修饰的注解在被javadoc解析时，保留下来。
 * Inherited:被它修饰的 Annotation 将具有继承性。被注解的类的子类也同样有该注解的效果
 * <p>
 * 5.通过反射获取注解信息 ---到反射内容时系统讲解
 * <p>
 * 6. jdk 8 中注解的新特性：可重复注解、类型注解
 * <p>
 * 6.1 可重复注解：① 在MyAnnotation上声明@Repeatable，成员值为MyAnnotations.class(另一个以MyAnnotation类为value方法数组的竹节)
 * ② MyAnnotation的Target和Retention等元注解与MyAnnotations相同。
 * <p>
 * 6.2 类型注解：
 * ElementType.TYPE_PARAMETER 表示该注解能写在类型变量的声明语句中（如：泛型声明）。
 * ElementType.TYPE_USE 表示该注解能写在使用类型的任何语句中。
 */
public class lesson_20_annotation {
}



@Inherited
@Repeatable(lesson_20_example_s.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, TYPE_PARAMETER, TYPE_USE})
@interface lesson_20_example {
    String value() default "hello";

}


@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, TYPE_PARAMETER, TYPE_USE})
@interface lesson_20_example_s{
    lesson_20_example[] value();
}


class Generic<@lesson_20_example T>{

    public void show() throws @lesson_20_example RuntimeException{

        ArrayList<@lesson_20_example String> list = new ArrayList<>();

        int num = (@lesson_20_example int) 10L;
    }

}
