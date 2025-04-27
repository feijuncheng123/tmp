import java.util.Scanner;

public class lesson_3_test {
  public static void main(String[] args) {
    Scanner sc=new Scanner(System.in);
    System.out.println("请输入5个成绩");
    int[] scores=new int[5];
    for(int i=0;i<5;i++){
      int score=sc.nextInt();
      scores[i]=score;
    }

  }
}
